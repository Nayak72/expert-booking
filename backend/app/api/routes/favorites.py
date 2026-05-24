"""
api/routes/favorites.py
=======================
Favorites (bookmarking) endpoints: add, remove, list.
"""

from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID

from app.core.database import get_db
from app.schemas.favorite import FavoriteCreate, FavoriteResponse
from app.services import favorite_service
from app.api.dependencies.auth import get_current_user
from app.models.user import User

router = APIRouter(prefix="/api/favorites", tags=["Favorites"])


@router.post("", response_model=FavoriteResponse, status_code=201)
async def add_favorite(
    data: FavoriteCreate,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """Add an expert to the current user's favorites."""
    favorite = await favorite_service.add_favorite(current_user.id, data.expert_id, db)
    expert = favorite.expert

    return FavoriteResponse(
        id=favorite.id,
        user_id=favorite.user_id,
        expert_id=favorite.expert_id,
        created_at=favorite.created_at,
        expert_expertise=expert.expertise if expert else None,
        expert_pricing=expert.pricing if expert else None,
        expert_average_rating=expert.average_rating if expert else None,
        expert_profile_image=expert.profile_image if expert else None,
        expert_name=expert.user.name if expert and expert.user else None,
    )


@router.delete("/{expert_id}", status_code=204)
async def remove_favorite(
    expert_id: UUID,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """Remove an expert from the current user's favorites."""
    await favorite_service.remove_favorite(current_user.id, expert_id, db)
    return None


@router.get("", response_model=list)
async def list_favorites(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """Retrieve all favorited experts for the current user."""
    favorites = await favorite_service.get_user_favorites(current_user.id, db)
    return [
        FavoriteResponse(
            id=f.id,
            user_id=f.user_id,
            expert_id=f.expert_id,
            created_at=f.created_at,
            expert_expertise=f.expert.expertise if f.expert else None,
            expert_pricing=f.expert.pricing if f.expert else None,
            expert_average_rating=f.expert.average_rating if f.expert else None,
            expert_profile_image=f.expert.profile_image if f.expert else None,
            expert_name=f.expert.user.name if f.expert and f.expert.user else None,
        ).model_dump()
        for f in favorites
    ]


@router.get("/{expert_id}/check")
async def check_favorite(
    expert_id: UUID,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """Check if the current user has favorited a specific expert."""
    is_fav = await favorite_service.is_expert_favorited(current_user.id, expert_id, db)
    return {"expert_id": str(expert_id), "is_favorited": is_fav}
