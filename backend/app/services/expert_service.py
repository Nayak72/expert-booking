"""
services/expert_service.py
==========================
Business logic for expert discovery, filtering, and profile management.
"""

from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, func, or_, and_
from sqlalchemy.orm import selectinload
from fastapi import HTTPException, status
from uuid import UUID
from typing import List, Optional, Tuple

from app.models.expert import Expert
from app.models.user import User
from app.schemas.expert import ExpertCreate, ExpertUpdate, ExpertListItem, ExpertResponse


async def create_expert_profile(
    user_id: UUID,
    data: ExpertCreate,
    db: AsyncSession,
) -> Expert:
    """Create an expert profile linked to a user account."""
    # Ensure user doesn't already have an expert profile
    result = await db.execute(select(Expert).where(Expert.user_id == user_id))
    if result.scalar_one_or_none():
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Expert profile already exists for this user.",
        )

    expert = Expert(
        user_id=user_id,
        bio=data.bio,
        expertise=data.expertise,
        skills=data.skills,
        experience=data.experience,
        languages=data.languages,
        pricing=data.pricing,
        profile_image=data.profile_image,
        categories=data.categories,
        availability=data.availability or {},
    )
    db.add(expert)
    await db.flush()
    await db.refresh(expert)
    return expert


async def get_expert_by_id(expert_id: UUID, db: AsyncSession) -> Expert:
    """Fetch a single expert by ID with user relationship loaded."""
    result = await db.execute(
        select(Expert)
        .options(selectinload(Expert.user))
        .where(Expert.id == expert_id)
    )
    expert = result.scalar_one_or_none()
    if not expert:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Expert not found.",
        )
    return expert


async def get_experts(
    db: AsyncSession,
    search: Optional[str] = None,
    expertise: Optional[str] = None,
    category: Optional[str] = None,
    min_rating: Optional[float] = None,
    max_price: Optional[float] = None,
    min_experience: Optional[int] = None,
    language: Optional[str] = None,
    is_available: Optional[int] = None,
    sort_by: str = "average_rating",
    sort_order: str = "desc",
    page: int = 1,
    page_size: int = 20,
) -> Tuple[List[Expert], int]:
    """
    Retrieve a paginated, filtered, and sorted list of experts.
    Returns (experts, total_count).
    """
    query = select(Expert).options(selectinload(Expert.user))

    # ── Filters ──────────────────────────────────────────────────────────────
    conditions = []
    if search:
        search_term = f"%{search.lower()}%"
        conditions.append(
            or_(
                func.lower(Expert.expertise).like(search_term),
                func.lower(Expert.skills).like(search_term),
                func.lower(Expert.bio).like(search_term),
                func.lower(Expert.categories).like(search_term),
            )
        )
    if expertise:
        conditions.append(func.lower(Expert.expertise).like(f"%{expertise.lower()}%"))
    if category:
        conditions.append(func.lower(Expert.categories).like(f"%{category.lower()}%"))
    if min_rating is not None:
        conditions.append(Expert.average_rating >= min_rating)
    if max_price is not None:
        conditions.append(Expert.pricing <= max_price)
    if min_experience is not None:
        conditions.append(Expert.experience >= min_experience)
    if language:
        conditions.append(func.lower(Expert.languages).like(f"%{language.lower()}%"))
    if is_available is not None:
        conditions.append(Expert.is_available == is_available)

    if conditions:
        query = query.where(and_(*conditions))

    # ── Count ──────────────────────────────────────────────────────────────
    count_result = await db.execute(
        select(func.count()).select_from(query.subquery())
    )
    total = count_result.scalar_one()

    # ── Sorting ────────────────────────────────────────────────────────────
    sort_column = {
        "average_rating": Expert.average_rating,
        "pricing": Expert.pricing,
        "experience": Expert.experience,
        "total_bookings": Expert.total_bookings,
    }.get(sort_by, Expert.average_rating)

    if sort_order == "asc":
        query = query.order_by(sort_column.asc())
    else:
        query = query.order_by(sort_column.desc())

    # ── Pagination ─────────────────────────────────────────────────────────
    offset = (page - 1) * page_size
    query = query.offset(offset).limit(page_size)

    result = await db.execute(query)
    experts = result.scalars().all()
    return list(experts), total


async def update_expert_profile(
    expert_id: UUID,
    user_id: UUID,
    data: ExpertUpdate,
    db: AsyncSession,
) -> Expert:
    """Update an expert's profile. Only the owning user can update."""
    expert = await get_expert_by_id(expert_id, db)

    if expert.user_id != user_id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="You can only update your own expert profile.",
        )

    update_data = data.model_dump(exclude_unset=True)
    for field, value in update_data.items():
        setattr(expert, field, value)

    db.add(expert)
    return expert


async def refresh_expert_ratings(expert_id: UUID, db: AsyncSession) -> None:
    """
    Recalculate and update average_rating and total_reviews for an expert.
    Called after any review create/update/delete.
    """
    from app.models.review import Review
    result = await db.execute(
        select(func.avg(Review.rating), func.count(Review.id))
        .where(Review.expert_id == expert_id)
    )
    avg_rating, total = result.one()
    expert_result = await db.execute(select(Expert).where(Expert.id == expert_id))
    expert = expert_result.scalar_one_or_none()
    if expert:
        expert.average_rating = round(float(avg_rating or 0), 2)
        expert.total_reviews = int(total or 0)
        db.add(expert)
