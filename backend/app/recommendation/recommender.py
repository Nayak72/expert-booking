"""
recommendation/recommender.py
==============================
High-level recommender service that:
1. Loads expert data from the DB.
2. Rebuilds the TF-IDF engine on startup (and periodically).
3. Generates personalized recommendations for a user.
"""

import logging
from uuid import UUID
from typing import List, Dict, Any
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from sqlalchemy.orm import selectinload

from app.models.expert import Expert
from app.models.booking import Booking
from app.models.favorite import Favorite
from app.recommendation.engine import recommendation_engine
from app.core.config import settings

logger = logging.getLogger(__name__)


async def rebuild_engine(db: AsyncSession) -> None:
    """
    Fetch all experts and (re)build the TF-IDF recommendation matrix.
    Called on application startup and can be triggered by a scheduled job.
    """
    result = await db.execute(
        select(Expert).options(selectinload(Expert.user))
    )
    experts = result.scalars().all()

    expert_dicts = []
    for e in experts:
        expert_dicts.append({
            "id": str(e.id),
            "user_id": str(e.user_id),
            "expertise": e.expertise or "",
            "skills": e.skills or "",
            "categories": e.categories or "",
            "bio": e.bio or "",
            "languages": e.languages or "",
            "average_rating": e.average_rating or 0.0,
            "total_bookings": e.total_bookings or 0,
            "pricing": e.pricing or 0.0,
            "experience": e.experience or 0,
            "profile_image": e.profile_image,
            "user_name": e.user.name if e.user else "",
            "is_available": e.is_available,
            "created_at": e.created_at.isoformat() if e.created_at else "",
        })

    recommendation_engine.fit(expert_dicts)
    logger.info(f"Recommendation engine rebuilt with {len(expert_dicts)} experts.")


async def get_recommendations_for_user(
    user_id: UUID,
    db: AsyncSession,
    top_n: int = None,
) -> List[Dict[str, Any]]:
    """
    Generate personalized expert recommendations for a user.

    Strategy:
    - Warm user: uses booked + favorited experts as seeds for content-based filtering.
    - Cold-start: falls back to top-rated + most-booked blend if no history.

    Args:
        user_id: The requesting user's UUID.
        db:      Async database session.
        top_n:   Max recommendations to return (defaults to settings.MAX_RECOMMENDATIONS).

    Returns:
        List of expert dicts enriched with recommendation_score / similarity_score.
    """
    n = top_n or settings.MAX_RECOMMENDATIONS

    # ── Gather User History ────────────────────────────────────────────────────
    # Booked experts
    booked_result = await db.execute(
        select(Booking.expert_id)
        .where(Booking.user_id == user_id)
        .distinct()
    )
    booked_expert_ids = [str(row[0]) for row in booked_result.all()]

    # Favorited experts
    fav_result = await db.execute(
        select(Favorite.expert_id)
        .where(Favorite.user_id == user_id)
    )
    fav_expert_ids = [str(row[0]) for row in fav_result.all()]

    # Combine seeds (deduplicated)
    seed_ids = list(set(booked_expert_ids + fav_expert_ids))
    exclude_ids = seed_ids  # Don't recommend experts already interacted with

    # ── Cold-Start: No History ─────────────────────────────────────────────────
    if not seed_ids:
        logger.info(f"Cold-start recommendations for user {user_id}.")
        recommendations = recommendation_engine.get_cold_start_recommendations(
            top_n=n,
            exclude_ids=exclude_ids,
        )
        return recommendations

    # ── Warm User: Content-Based Filtering ────────────────────────────────────
    logger.info(
        f"Content-based recommendations for user {user_id} "
        f"using {len(seed_ids)} seed experts."
    )
    recommendations = recommendation_engine.get_similar_experts(
        seed_expert_ids=seed_ids,
        exclude_ids=exclude_ids,
        top_n=n,
        min_score=settings.MIN_SIMILARITY_SCORE,
    )

    # Fallback to cold-start if similarity found nothing
    if not recommendations:
        logger.info(f"No similarity matches; falling back to cold-start for user {user_id}.")
        recommendations = recommendation_engine.get_cold_start_recommendations(
            top_n=n,
            exclude_ids=exclude_ids,
        )

    return recommendations
