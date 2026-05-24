"""
api/routes/reviews.py
=====================
Review management endpoints: create, update, list, rating summary.
"""

from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID

from app.core.database import get_db
from app.schemas.review import ReviewCreate, ReviewUpdate, ReviewResponse, RatingSummary
from app.services import review_service
from app.services.expert_service import refresh_expert_ratings
from app.api.dependencies.auth import get_current_user
from app.models.user import User

router = APIRouter(prefix="/api/reviews", tags=["Reviews"])


@router.post("", response_model=ReviewResponse, status_code=201)
async def create_review(
    data: ReviewCreate,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """
    Submit a review for an expert.
    Requires a completed booking with the expert.
    """
    review = await review_service.create_review(current_user.id, data, db)
    # Refresh cached rating on expert
    await refresh_expert_ratings(data.expert_id, db)

    return ReviewResponse(
        id=review.id,
        user_id=review.user_id,
        expert_id=review.expert_id,
        rating=review.rating,
        review_text=review.review_text,
        created_at=review.created_at,
        updated_at=review.updated_at,
        reviewer_name=current_user.name,
        reviewer_image=current_user.profile_image,
    )


@router.patch("/{review_id}", response_model=ReviewResponse)
async def update_review(
    review_id: UUID,
    data: ReviewUpdate,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """Update an existing review (author only)."""
    review = await review_service.update_review(review_id, current_user.id, data, db)
    await refresh_expert_ratings(review.expert_id, db)

    return ReviewResponse(
        id=review.id,
        user_id=review.user_id,
        expert_id=review.expert_id,
        rating=review.rating,
        review_text=review.review_text,
        created_at=review.created_at,
        updated_at=review.updated_at,
        reviewer_name=current_user.name,
        reviewer_image=current_user.profile_image,
    )


@router.get("/expert/{expert_id}", response_model=dict)
async def get_expert_reviews(
    expert_id: UUID,
    page: int = Query(default=1, ge=1),
    page_size: int = Query(default=20, ge=1, le=100),
    db: AsyncSession = Depends(get_db),
):
    """Get paginated reviews for a specific expert."""
    reviews, total = await review_service.get_expert_reviews(expert_id, db, page, page_size)

    items = [
        ReviewResponse(
            id=r.id,
            user_id=r.user_id,
            expert_id=r.expert_id,
            rating=r.rating,
            review_text=r.review_text,
            created_at=r.created_at,
            updated_at=r.updated_at,
            reviewer_name=r.reviewer.name if r.reviewer else None,
            reviewer_image=r.reviewer.profile_image if r.reviewer else None,
        ).model_dump()
        for r in reviews
    ]

    return {
        "items": items,
        "total": total,
        "page": page,
        "page_size": page_size,
        "total_pages": (total + page_size - 1) // page_size,
    }


@router.get("/expert/{expert_id}/summary", response_model=RatingSummary)
async def get_rating_summary(expert_id: UUID, db: AsyncSession = Depends(get_db)):
    """Get aggregated rating statistics for an expert."""
    return await review_service.get_rating_summary(expert_id, db)
