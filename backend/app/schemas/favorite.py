"""
schemas/favorite.py
===================
Pydantic schemas for Favorite (bookmarking) request / response validation.
"""

from pydantic import BaseModel
from typing import Optional
from datetime import datetime
from uuid import UUID


class FavoriteCreate(BaseModel):
    """Schema for adding an expert to favorites."""
    expert_id: UUID


class FavoriteResponse(BaseModel):
    """Favorite record response."""
    id: UUID
    user_id: UUID
    expert_id: UUID
    created_at: datetime

    # Embedded expert info
    expert_expertise: Optional[str] = None
    expert_pricing: Optional[float] = None
    expert_average_rating: Optional[float] = None
    expert_profile_image: Optional[str] = None
    expert_name: Optional[str] = None

    model_config = {"from_attributes": True}
