"""
api/routes/bookings.py
======================
Booking management endpoints: create, cancel, reschedule, history.
Triggers WebSocket broadcasts and FCM notifications after booking events.
"""

from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from sqlalchemy.orm import selectinload
from typing import Optional
from uuid import UUID

from app.core.database import get_db
from app.schemas.booking import BookingCreate, BookingUpdate, BookingCancel, BookingResponse
from app.services import booking_service
from app.services.notification_service import (
    notify_booking_confirmed,
    notify_booking_cancelled,
    notify_booking_rescheduled,
)
from app.websocket.slot_broadcaster import broadcast_slot_booked, broadcast_slot_released
from app.api.dependencies.auth import get_current_user
from app.models.user import User
from app.models.expert import Expert

router = APIRouter(prefix="/api/bookings", tags=["Bookings"])


@router.post("", response_model=BookingResponse, status_code=201)
async def create_booking(
    data: BookingCreate,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """
    Book a session with an expert.
    - Validates slot availability (double-booking prevention).
    - Broadcasts real-time slot update via WebSocket.
    - Sends FCM push notification to the user.
    """
    booking = await booking_service.create_booking(current_user.id, data, db)

    # Fetch expert with user for notification and broadcast
    expert_result = await db.execute(
        select(Expert).options(selectinload(Expert.user)).where(Expert.id == booking.expert_id)
    )
    expert = expert_result.scalar_one_or_none()

    # ── Real-time broadcast ────────────────────────────────────────────────────
    await broadcast_slot_booked(
        expert_id=str(booking.expert_id),
        booking_date=str(booking.booking_date),
        slot=booking.slot,
        booking_id=str(booking.id),
    )

    # ── FCM Notification ───────────────────────────────────────────────────────
    expert_name = expert.user.name if expert and expert.user else "Your Expert"
    await notify_booking_confirmed(
        fcm_token=current_user.fcm_token,
        expert_name=expert_name,
        date=str(booking.booking_date),
        slot=booking.slot,
    )

    return BookingResponse(
        id=booking.id,
        user_id=booking.user_id,
        expert_id=booking.expert_id,
        booking_date=booking.booking_date,
        slot=booking.slot,
        status=booking.status,
        notes=booking.notes,
        meeting_link=booking.meeting_link,
        cancellation_reason=booking.cancellation_reason,
        created_at=booking.created_at,
        updated_at=booking.updated_at,
        expert_name=expert_name,
        expert_expertise=expert.expertise if expert else None,
        expert_profile_image=expert.profile_image if expert else None,
        user_name=current_user.name,
    )


@router.delete("/{booking_id}", response_model=BookingResponse)
async def cancel_booking(
    booking_id: UUID,
    data: BookingCancel = BookingCancel(),
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """
    Cancel a booking.
    - Releases the slot and broadcasts the update.
    - Sends FCM cancellation notification.
    """
    booking = await booking_service.cancel_booking(booking_id, current_user.id, data, db)

    # Real-time broadcast: slot is released
    await broadcast_slot_released(
        expert_id=str(booking.expert_id),
        booking_date=str(booking.booking_date),
        slot=booking.slot,
    )

    # FCM Notification
    expert_result = await db.execute(
        select(Expert).options(selectinload(Expert.user)).where(Expert.id == booking.expert_id)
    )
    expert = expert_result.scalar_one_or_none()
    expert_name = expert.user.name if expert and expert.user else "Your Expert"

    await notify_booking_cancelled(
        fcm_token=current_user.fcm_token,
        expert_name=expert_name,
        date=str(booking.booking_date),
    )

    return BookingResponse(
        id=booking.id,
        user_id=booking.user_id,
        expert_id=booking.expert_id,
        booking_date=booking.booking_date,
        slot=booking.slot,
        status=booking.status,
        notes=booking.notes,
        meeting_link=booking.meeting_link,
        cancellation_reason=booking.cancellation_reason,
        created_at=booking.created_at,
        updated_at=booking.updated_at,
        expert_name=expert_name,
    )


@router.patch("/{booking_id}/reschedule", response_model=BookingResponse)
async def reschedule_booking(
    booking_id: UUID,
    data: BookingUpdate,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """
    Reschedule a booking to a new date/slot.
    - Broadcasts old slot released + new slot booked.
    - Sends FCM rescheduled notification.
    """
    old_date = None
    old_slot = None

    # Capture old slot before update
    from app.models.booking import Booking
    from sqlalchemy import select as sa_select
    result = await db.execute(sa_select(Booking).where(Booking.id == booking_id))
    existing = result.scalar_one_or_none()
    if existing:
        old_date = str(existing.booking_date)
        old_slot = existing.slot

    booking = await booking_service.reschedule_booking(booking_id, current_user.id, data, db)

    # Broadcast slot changes
    if old_date and old_slot:
        await broadcast_slot_released(
            expert_id=str(booking.expert_id),
            booking_date=old_date,
            slot=old_slot,
        )
    await broadcast_slot_booked(
        expert_id=str(booking.expert_id),
        booking_date=str(booking.booking_date),
        slot=booking.slot,
        booking_id=str(booking.id),
    )

    # FCM Notification
    expert_result = await db.execute(
        select(Expert).options(selectinload(Expert.user)).where(Expert.id == booking.expert_id)
    )
    expert = expert_result.scalar_one_or_none()
    expert_name = expert.user.name if expert and expert.user else "Your Expert"

    await notify_booking_rescheduled(
        fcm_token=current_user.fcm_token,
        expert_name=expert_name,
        new_date=str(booking.booking_date),
        new_slot=booking.slot,
    )

    return BookingResponse(
        id=booking.id,
        user_id=booking.user_id,
        expert_id=booking.expert_id,
        booking_date=booking.booking_date,
        slot=booking.slot,
        status=booking.status,
        notes=booking.notes,
        meeting_link=booking.meeting_link,
        cancellation_reason=booking.cancellation_reason,
        created_at=booking.created_at,
        updated_at=booking.updated_at,
        expert_name=expert_name,
    )


@router.patch("/{booking_id}/confirm", response_model=BookingResponse)
async def confirm_booking(
    booking_id: UUID,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """
    Confirm a pending booking.
    - Sends FCM notification to the user.
    """
    booking = await booking_service.confirm_booking(booking_id, current_user.id, db)

    # Fetch user for notification
    user_result = await db.execute(select(User).where(User.id == booking.user_id))
    user = user_result.scalar_one_or_none()

    # FCM Notification
    if user:
        await notify_booking_confirmed(
            fcm_token=user.fcm_token,
            expert_name=current_user.name,
            date=str(booking.booking_date),
            slot=booking.slot,
        )

    return BookingResponse(
        id=booking.id,
        user_id=booking.user_id,
        expert_id=booking.expert_id,
        booking_date=booking.booking_date,
        slot=booking.slot,
        status=booking.status,
        notes=booking.notes,
        meeting_link=booking.meeting_link,
        cancellation_reason=booking.cancellation_reason,
        created_at=booking.created_at,
        updated_at=booking.updated_at,
        expert_name=current_user.name,
    )


@router.get("/history", response_model=dict)
async def booking_history(
    page: int = Query(default=1, ge=1),
    page_size: int = Query(default=20, ge=1, le=100),
    status: Optional[str] = Query(None),
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """Fetch paginated booking history for the current user."""
    bookings, total = await booking_service.get_booking_history(
        current_user.id, db, page, page_size, status, current_user.role
    )

    items = []
    for b in bookings:
        expert = b.expert
        items.append(BookingResponse(
            id=b.id,
            user_id=b.user_id,
            expert_id=b.expert_id,
            booking_date=b.booking_date,
            slot=b.slot,
            status=b.status,
            notes=b.notes,
            meeting_link=b.meeting_link,
            cancellation_reason=b.cancellation_reason,
            created_at=b.created_at,
            updated_at=b.updated_at,
            expert_name=expert.user.name if expert and expert.user else None,
            expert_expertise=expert.expertise if expert else None,
            expert_profile_image=expert.profile_image if expert else None,
            user_name=b.user.name if b.user else current_user.name,
        ).model_dump())

    return {
        "items": items,
        "total": total,
        "page": page,
        "page_size": page_size,
        "total_pages": (total + page_size - 1) // page_size,
    }


@router.get("/upcoming", response_model=list)
async def upcoming_sessions(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """Get upcoming confirmed sessions for the expert dashboard."""
    bookings = await booking_service.get_upcoming_bookings(current_user.id, db, current_user.role)
    return [
        BookingResponse(
            id=b.id,
            user_id=b.user_id,
            expert_id=b.expert_id,
            booking_date=b.booking_date,
            slot=b.slot,
            status=b.status,
            notes=b.notes,
            meeting_link=b.meeting_link,
            cancellation_reason=b.cancellation_reason,
            created_at=b.created_at,
            updated_at=b.updated_at,
            user_name=b.user.name if b.user else None,
        ).model_dump()
        for b in bookings
    ]
