"""
schemas/review.py
=================
Pydantic schemas for Review request / response validation.
"""

from pydantic import BaseModel, Field
from typing import Optional
from datetime import datetime
from uuid import UUID


class ReviewCreate(BaseModel):
    """Schema for submitting a review."""
    expert_id: UUID
    rating: float = Field(..., ge=1.0, le=5.0)
    review_text: Optional[str] = Field(None, max_length=2000)


class ReviewUpdate(BaseModel):
    """Schema for updating an existing review."""
    rating: Optional[float] = Field(None, ge=1.0, le=5.0)
    review_text: Optional[str] = Field(None, max_length=2000)


class ReviewResponse(BaseModel):
    """Full review response with reviewer info."""
    id: UUID
    user_id: UUID
    expert_id: UUID
    rating: float
    review_text: Optional[str] = None
    created_at: datetime
    updated_at: datetime

    # Embedded reviewer info
    reviewer_name: Optional[str] = None
    reviewer_image: Optional[str] = None

    model_config = {"from_attributes": True}


class RatingSummary(BaseModel):
    """Aggregated rating summary for an expert."""
    expert_id: str
    average_rating: float
    total_reviews: int
    rating_distribution: dict  # {"5": 10, "4": 5, "3": 2, "2": 1, "1": 0}
