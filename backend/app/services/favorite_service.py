"""
services/favorite_service.py
============================
Business logic for managing user favorites (bookmarking experts).
"""

from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from sqlalchemy.orm import selectinload
from fastapi import HTTPException, status
from uuid import UUID
from typing import List

from app.models.favorite import Favorite
from app.models.expert import Expert


async def add_favorite(user_id: UUID, expert_id: UUID, db: AsyncSession) -> Favorite:
    """
    Add an expert to a user's favorites list.

    Raises:
        404 if expert not found.
        409 if already favorited.
    """
    expert_result = await db.execute(select(Expert).where(Expert.id == expert_id))
    if not expert_result.scalar_one_or_none():
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Expert not found.")

    existing = await db.execute(
        select(Favorite).where(
            Favorite.user_id == user_id,
            Favorite.expert_id == expert_id,
        )
    )
    if existing.scalar_one_or_none():
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="Expert is already in your favorites.",
        )

    favorite = Favorite(user_id=user_id, expert_id=expert_id)
    db.add(favorite)
    await db.flush()
    
    # Reload with relations so the frontend gets the expert name
    result = await db.execute(
        select(Favorite)
        .options(selectinload(Favorite.expert).selectinload(Expert.user))
        .where(Favorite.id == favorite.id)
    )
    favorite = result.scalar_one()
    
    return favorite


async def remove_favorite(user_id: UUID, expert_id: UUID, db: AsyncSession) -> None:
    """Remove an expert from a user's favorites list."""
    result = await db.execute(
        select(Favorite).where(
            Favorite.user_id == user_id,
            Favorite.expert_id == expert_id,
        )
    )
    favorite = result.scalar_one_or_none()
    if not favorite:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Favorite not found.")

    await db.delete(favorite)


async def get_user_favorites(user_id: UUID, db: AsyncSession) -> List[Favorite]:
    """Retrieve all favorites for a user with expert and user info loaded."""
    result = await db.execute(
        select(Favorite)
        .options(
            selectinload(Favorite.expert).selectinload(Expert.user)
        )
        .where(Favorite.user_id == user_id)
        .order_by(Favorite.created_at.desc())
    )
    return list(result.scalars().all())


async def is_expert_favorited(user_id: UUID, expert_id: UUID, db: AsyncSession) -> bool:
    """Check if a user has favorited a specific expert."""
    result = await db.execute(
        select(Favorite).where(
            Favorite.user_id == user_id,
            Favorite.expert_id == expert_id,
        )
    )
    return result.scalar_one_or_none() is not None
