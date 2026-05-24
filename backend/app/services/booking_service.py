"""
services/booking_service.py
============================
Business logic for booking: create, cancel, reschedule, and history.
Includes double-booking prevention and real-time WebSocket broadcasting.
"""

from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, and_, func
from sqlalchemy.orm import selectinload
from fastapi import HTTPException, status
from uuid import UUID
from datetime import date
from typing import List, Tuple, Optional

from app.models.booking import Booking
from app.models.expert import Expert
from app.schemas.booking import BookingCreate, BookingUpdate, BookingCancel


async def create_booking(
    user_id: UUID,
    data: BookingCreate,
    db: AsyncSession,
) -> Booking:
    """
    Create a new booking with double-booking prevention.

    Raises:
        404 if expert not found.
        409 if the slot is already taken.
    """
    # Verify expert exists
    expert_result = await db.execute(select(Expert).where(Expert.id == data.expert_id))
    expert = expert_result.scalar_one_or_none()
    if not expert:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Expert not found.")
    if not expert.is_available:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Expert is currently unavailable.")

    # Double-booking check: expert cannot have same date+slot confirmed/pending
    conflict_result = await db.execute(
        select(Booking).where(
            and_(
                Booking.expert_id == data.expert_id,
                Booking.booking_date == data.booking_date,
                Booking.slot == data.slot,
                Booking.status.in_(["pending", "confirmed"]),
            )
        )
    )
    if conflict_result.scalar_one_or_none():
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="This slot is already booked. Please choose another time.",
        )

    booking = Booking(
        user_id=user_id,
        expert_id=data.expert_id,
        booking_date=data.booking_date,
        slot=data.slot,
        status="pending",
        notes=data.notes,
    )
    db.add(booking)
    await db.flush()

    # Update expert's total bookings count
    expert.total_bookings += 1
    db.add(expert)

    await db.refresh(booking)
    return booking


async def cancel_booking(
    booking_id: UUID,
    user_id: UUID,
    data: BookingCancel,
    db: AsyncSession,
) -> Booking:
    """
    Cancel a booking. Only the booking owner or the expert can cancel.

    Raises:
        404 if booking not found.
        403 if unauthorized.
        400 if already cancelled/completed.
    """
    result = await db.execute(
        select(Booking)
        .options(selectinload(Booking.expert))
        .where(Booking.id == booking_id)
    )
    booking = result.scalar_one_or_none()
    if not booking:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Booking not found.")

    # Authorization: user who booked or the expert themselves
    is_owner = booking.user_id == user_id
    is_expert_owner = booking.expert.user_id == user_id
    if not (is_owner or is_expert_owner):
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Access denied.")

    if booking.status in ["cancelled", "completed"]:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Cannot cancel a booking that is already {booking.status}.",
        )

    booking.status = "cancelled"
    booking.cancellation_reason = data.cancellation_reason
    db.add(booking)
    return booking


async def confirm_booking(
    booking_id: UUID,
    user_id: UUID,
    db: AsyncSession,
) -> Booking:
    """
    Confirm a pending booking. Only the expert can confirm.
    """
    result = await db.execute(
        select(Booking).options(selectinload(Booking.expert)).where(Booking.id == booking_id)
    )
    booking = result.scalar_one_or_none()
    if not booking:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Booking not found.")

    if booking.expert.user_id != user_id:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Access denied.")

    if booking.status != "pending":
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Cannot confirm a booking with status {booking.status}.",
        )

    booking.status = "confirmed"
    db.add(booking)
    return booking


async def reschedule_booking(
    booking_id: UUID,
    user_id: UUID,
    data: BookingUpdate,
    db: AsyncSession,
) -> Booking:
    """
    Reschedule an existing confirmed booking to a new date/slot.
    """
    result = await db.execute(
        select(Booking).where(Booking.id == booking_id)
    )
    booking = result.scalar_one_or_none()
    if not booking:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Booking not found.")

    if booking.user_id != user_id:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Access denied.")

    if booking.status not in ["pending", "confirmed"]:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Only pending or confirmed bookings can be rescheduled.",
        )

    new_date = data.booking_date or booking.booking_date
    new_slot = data.slot or booking.slot

    # Check new slot availability
    if new_date != booking.booking_date or new_slot != booking.slot:
        conflict = await db.execute(
            select(Booking).where(
                and_(
                    Booking.expert_id == booking.expert_id,
                    Booking.booking_date == new_date,
                    Booking.slot == new_slot,
                    Booking.id != booking_id,
                    Booking.status.in_(["pending", "confirmed"]),
                )
            )
        )
        if conflict.scalar_one_or_none():
            raise HTTPException(
                status_code=status.HTTP_409_CONFLICT,
                detail="The new slot is already taken.",
            )

    booking.booking_date = new_date
    booking.slot = new_slot
    if data.notes is not None:
        booking.notes = data.notes

    db.add(booking)
    return booking


async def get_booking_history(
    user_id: UUID,
    db: AsyncSession,
    page: int = 1,
    page_size: int = 20,
    status_filter: Optional[str] = None,
    role: str = "user",
) -> Tuple[List[Booking], int]:
    """Fetch paginated booking history for a user or expert."""
    query = (
        select(Booking)
        .options(
            selectinload(Booking.expert).selectinload(Expert.user),
            selectinload(Booking.user),
        )
    )
    
    if role == "expert":
        expert_result = await db.execute(select(Expert).where(Expert.user_id == user_id))
        expert = expert_result.scalar_one_or_none()
        if not expert:
            return [], 0
        query = query.where(Booking.expert_id == expert.id)
    else:
        query = query.where(Booking.user_id == user_id)

    if status_filter:
        query = query.where(Booking.status == status_filter)

    count_q = select(func.count()).select_from(query.subquery())
    total = (await db.execute(count_q)).scalar_one()

    query = query.order_by(Booking.created_at.desc())
    query = query.offset((page - 1) * page_size).limit(page_size)
    result = await db.execute(query)
    return list(result.scalars().all()), total


async def get_upcoming_bookings(
    user_id: UUID,
    db: AsyncSession,
    role: str = "user",
) -> List[Booking]:
    """Fetch upcoming bookings for a user or expert."""
    from datetime import date as date_type
    
    query = (
        select(Booking)
        .options(
            selectinload(Booking.expert).selectinload(Expert.user),
            selectinload(Booking.user)
        )
        .where(
            and_(
                Booking.booking_date >= date_type.today(),
                Booking.status.in_(["pending", "confirmed"]),
            )
        )
    )

    if role == "expert":
        expert_result = await db.execute(select(Expert).where(Expert.user_id == user_id))
        expert = expert_result.scalar_one_or_none()
        if not expert:
            return []
        query = query.where(Booking.expert_id == expert.id)
    else:
        query = query.where(Booking.user_id == user_id)

    query = query.order_by(Booking.booking_date.asc(), Booking.slot.asc()).limit(20)
    result = await db.execute(query)
    return list(result.scalars().all())


async def get_booked_slots(
    expert_id: UUID,
    booking_date: date,
    db: AsyncSession,
) -> List[str]:
    """Return list of booked slot strings for a given expert + date."""
    result = await db.execute(
        select(Booking.slot).where(
            and_(
                Booking.expert_id == expert_id,
                Booking.booking_date == booking_date,
                Booking.status.in_(["pending", "confirmed"]),
            )
        )
    )
    return [row[0] for row in result.all()]
