"""
api/routes/experts.py
=====================
Expert discovery, profile management, and slot availability endpoints.
Also includes the WebSocket endpoint for real-time slot updates.
"""

from fastapi import APIRouter, Depends, Query, WebSocket, WebSocketDisconnect
from sqlalchemy.ext.asyncio import AsyncSession
from typing import Optional
from uuid import UUID

from app.core.database import get_db
from app.schemas.expert import ExpertCreate, ExpertUpdate, ExpertResponse, ExpertListItem
from app.services import expert_service
from app.services.booking_service import get_booked_slots
from app.api.dependencies.auth import get_current_user
from app.websocket.manager import connection_manager
from app.models.user import User
from datetime import date as date_type

router = APIRouter(prefix="/api/experts", tags=["Experts"])


@router.get("", response_model=dict)
async def list_experts(
    search: Optional[str] = Query(None),
    expertise: Optional[str] = Query(None),
    category: Optional[str] = Query(None),
    min_rating: Optional[float] = Query(None, ge=0, le=5),
    max_price: Optional[float] = Query(None, ge=0),
    min_experience: Optional[int] = Query(None, ge=0),
    language: Optional[str] = Query(None),
    is_available: Optional[int] = Query(None),
    sort_by: str = Query(default="average_rating"),
    sort_order: str = Query(default="desc"),
    page: int = Query(default=1, ge=1),
    page_size: int = Query(default=20, ge=1, le=100),
    db: AsyncSession = Depends(get_db),
):
    """
    List experts with optional filters, sorting, and pagination.
    Supports full-text search across expertise, skills, bio, and categories.
    """
    experts, total = await expert_service.get_experts(
        db=db,
        search=search,
        expertise=expertise,
        category=category,
        min_rating=min_rating,
        max_price=max_price,
        min_experience=min_experience,
        language=language,
        is_available=is_available,
        sort_by=sort_by,
        sort_order=sort_order,
        page=page,
        page_size=page_size,
    )

    items = []
    for e in experts:
        item = ExpertListItem(
            id=e.id,
            user_id=e.user_id,
            expertise=e.expertise,
            skills=e.skills,
            experience=e.experience,
            languages=e.languages,
            pricing=e.pricing,
            profile_image=e.profile_image,
            categories=e.categories,
            average_rating=e.average_rating,
            total_reviews=e.total_reviews,
            total_bookings=e.total_bookings,
            is_available=e.is_available,
            created_at=e.created_at,
            user_name=e.user.name if e.user else None,
        )
        items.append(item.model_dump())

    return {
        "items": items,
        "total": total,
        "page": page,
        "page_size": page_size,
        "total_pages": (total + page_size - 1) // page_size,
    }


@router.get("/me", response_model=ExpertResponse)
async def get_my_expert_profile(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    """Get the currently authenticated user's expert profile."""
    from sqlalchemy import select
    from app.models.expert import Expert
    from sqlalchemy.orm import joinedload
    
    stmt = select(Expert).options(joinedload(Expert.user)).where(Expert.user_id == current_user.id)
    result = await db.execute(stmt)
    expert = result.scalars().first()
    
    if not expert:
        from fastapi import HTTPException
        raise HTTPException(status_code=404, detail="Expert profile not found")
        
    return ExpertResponse(
        id=expert.id,
        user_id=expert.user_id,
        bio=expert.bio,
        expertise=expert.expertise,
        skills=expert.skills,
        experience=expert.experience,
        languages=expert.languages,
        pricing=expert.pricing,
        profile_image=expert.profile_image,
        categories=expert.categories,
        availability=expert.availability,
        average_rating=expert.average_rating,
        total_reviews=expert.total_reviews,
        total_bookings=expert.total_bookings,
        is_available=expert.is_available,
        created_at=expert.created_at,
        user_name=expert.user.name if expert.user else None,
        user_email=expert.user.email if expert.user else None,
    )


@router.get("/{expert_id}", response_model=ExpertResponse)
async def get_expert(expert_id: UUID, db: AsyncSession = Depends(get_db)):
    """Get a single expert's full profile by ID."""
    expert = await expert_service.get_expert_by_id(expert_id, db)
    response = ExpertResponse(
        id=expert.id,
        user_id=expert.user_id,
        bio=expert.bio,
        expertise=expert.expertise,
        skills=expert.skills,
        experience=expert.experience,
        languages=expert.languages,
        pricing=expert.pricing,
        profile_image=expert.profile_image,
        categories=expert.categories,
        availability=expert.availability,
        average_rating=expert.average_rating,
        total_reviews=expert.total_reviews,
        total_bookings=expert.total_bookings,
        is_available=expert.is_available,
        created_at=expert.created_at,
        user_name=expert.user.name if expert.user else None,
        user_email=expert.user.email if expert.user else None,
    )
    return response


@router.post("", response_model=ExpertResponse, status_code=201)
async def create_expert_profile(
    data: ExpertCreate,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """Create an expert profile for the currently authenticated user."""
    expert = await expert_service.create_expert_profile(current_user.id, data, db)
    return ExpertResponse(
        id=expert.id,
        user_id=expert.user_id,
        bio=expert.bio,
        expertise=expert.expertise,
        skills=expert.skills,
        experience=expert.experience,
        languages=expert.languages,
        pricing=expert.pricing,
        profile_image=expert.profile_image,
        categories=expert.categories,
        availability=expert.availability,
        average_rating=expert.average_rating,
        total_reviews=expert.total_reviews,
        total_bookings=expert.total_bookings,
        is_available=expert.is_available,
        created_at=expert.created_at,
        user_name=current_user.name,
        user_email=current_user.email,
    )


@router.patch("/{expert_id}", response_model=ExpertResponse)
async def update_expert_profile(
    expert_id: UUID,
    data: ExpertUpdate,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """Update an expert's profile. Only the profile owner can update."""
    expert = await expert_service.update_expert_profile(expert_id, current_user.id, data, db)
    return ExpertResponse(
        id=expert.id,
        user_id=expert.user_id,
        bio=expert.bio,
        expertise=expert.expertise,
        skills=expert.skills,
        experience=expert.experience,
        languages=expert.languages,
        pricing=expert.pricing,
        profile_image=expert.profile_image,
        categories=expert.categories,
        availability=expert.availability,
        average_rating=expert.average_rating,
        total_reviews=expert.total_reviews,
        total_bookings=expert.total_bookings,
        is_available=expert.is_available,
        created_at=expert.created_at,
    )


@router.get("/{expert_id}/slots")
async def get_available_slots(
    expert_id: UUID,
    date: date_type = Query(..., description="Date in YYYY-MM-DD format"),
    db: AsyncSession = Depends(get_db),
):
    """Get booked slots for an expert on a specific date."""
    booked = await get_booked_slots(expert_id, date, db)
    return {"expert_id": str(expert_id), "date": str(date), "booked_slots": booked}


# ── WebSocket Endpoint ────────────────────────────────────────────────────────
@router.websocket("/ws/{expert_id}")
async def slot_websocket(websocket: WebSocket, expert_id: str):
    """
    WebSocket endpoint for real-time slot availability updates.

    Connect: ws://HOST/api/experts/ws/{expert_id}

    The client will receive JSON messages when slots are booked or released:
    {
        "event": "slot_booked" | "slot_released",
        "expert_id": "...",
        "booking_date": "YYYY-MM-DD",
        "slot": "HH:MM-HH:MM",
        "is_available": false | true
    }
    """
    await connection_manager.connect(websocket, expert_id)
    try:
        # Keep connection alive — receive messages (heartbeat/ping from client)
        while True:
            data = await websocket.receive_text()
            # Echo heartbeat back
            if data == "ping":
                await websocket.send_text("pong")
    except WebSocketDisconnect:
        connection_manager.disconnect(websocket, expert_id)
