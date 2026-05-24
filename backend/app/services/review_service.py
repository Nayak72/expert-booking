"""
services/review_service.py
==========================
Business logic for expert reviews and rating aggregation.
"""

from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, func
from sqlalchemy.orm import selectinload
from fastapi import HTTPException, status
from uuid import UUID
from typing import List, Tuple, Optional

from app.models.review import Review
from app.models.booking import Booking
from app.schemas.review import ReviewCreate, ReviewUpdate, RatingSummary


async def create_review(
    user_id: UUID,
    data: ReviewCreate,
    db: AsyncSession,
) -> Review:
    """
    Submit a review for an expert.

    Rules:
    - User must have a completed booking with the expert.
    - Only one review per user-expert pair (enforced by DB unique constraint).
    """
    # Must have a completed booking
    booking_result = await db.execute(
        select(Booking).where(
            Booking.user_id == user_id,
            Booking.expert_id == data.expert_id,
            Booking.status == "completed",
        )
    )
    if not booking_result.scalar_one_or_none():
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="You can only review experts you have completed a session with.",
        )

    # Check for existing review
    existing = await db.execute(
        select(Review).where(
            Review.user_id == user_id,
            Review.expert_id == data.expert_id,
        )
    )
    if existing.scalar_one_or_none():
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="You have already reviewed this expert. Use PATCH to update.",
        )

    review = Review(
        user_id=user_id,
        expert_id=data.expert_id,
        rating=data.rating,
        review_text=data.review_text,
    )
    db.add(review)
    await db.flush()
    await db.refresh(review)
    return review


async def update_review(
    review_id: UUID,
    user_id: UUID,
    data: ReviewUpdate,
    db: AsyncSession,
) -> Review:
    """Update an existing review (only by the author)."""
    result = await db.execute(select(Review).where(Review.id == review_id))
    review = result.scalar_one_or_none()

    if not review:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Review not found.")
    if review.user_id != user_id:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Access denied.")

    if data.rating is not None:
        review.rating = data.rating
    if data.review_text is not None:
        review.review_text = data.review_text

    db.add(review)
    return review


async def get_expert_reviews(
    expert_id: UUID,
    db: AsyncSession,
    page: int = 1,
    page_size: int = 20,
) -> Tuple[List[Review], int]:
    """Fetch paginated reviews for a given expert."""
    query = (
        select(Review)
        .options(selectinload(Review.reviewer))
        .where(Review.expert_id == expert_id)
    )
    total = (await db.execute(select(func.count()).select_from(query.subquery()))).scalar_one()
    reviews = (
        await db.execute(
            query.order_by(Review.created_at.desc())
            .offset((page - 1) * page_size)
            .limit(page_size)
        )
    ).scalars().all()
    return list(reviews), total


async def get_rating_summary(expert_id: UUID, db: AsyncSession) -> RatingSummary:
    """Return aggregated rating stats for an expert."""
    result = await db.execute(
        select(func.avg(Review.rating), func.count(Review.id))
        .where(Review.expert_id == expert_id)
    )
    avg, total = result.one()

    # Rating distribution
    distribution = {}
    for star in [5, 4, 3, 2, 1]:
        cnt_result = await db.execute(
            select(func.count(Review.id)).where(
                Review.expert_id == expert_id,
                func.floor(Review.rating) == star,
            )
        )
        distribution[str(star)] = cnt_result.scalar_one()

    return RatingSummary(
        expert_id=str(expert_id),
        average_rating=round(float(avg or 0), 2),
        total_reviews=int(total or 0),
        rating_distribution=distribution,
    )
