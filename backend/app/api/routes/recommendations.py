"""
api/routes/recommendations.py
==============================
ML-powered recommendation endpoint.
Returns personalized expert recommendations for the current user.
"""

from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.database import get_db
from app.recommendation.recommender import get_recommendations_for_user
from app.api.dependencies.auth import get_current_user
from app.models.user import User

router = APIRouter(prefix="/api/recommendations", tags=["Recommendations"])


@router.get("")
async def get_recommendations(
    top_n: int = Query(default=10, ge=1, le=50, description="Number of recommendations"),
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """
    Get personalized expert recommendations for the current user.

    Algorithm:
    - Warm user: TF-IDF cosine similarity based on booking + favorite history.
    - Cold-start (new user): Top-rated + most-booked experts blend.

    Returns a list of expert dicts with similarity_score or recommendation_score.
    """
    recommendations = await get_recommendations_for_user(
        user_id=current_user.id,
        db=db,
        top_n=top_n,
    )
    return {
        "user_id": str(current_user.id),
        "strategy": "content_based" if recommendations and "similarity_score" in recommendations[0] else "cold_start",
        "total": len(recommendations),
        "recommendations": recommendations,
    }
