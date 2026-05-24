"""
schemas/expert.py
=================
Pydantic schemas for Expert request / response validation.
"""

from pydantic import BaseModel, Field
from typing import Optional, Dict, Any
from datetime import datetime
from uuid import UUID


class ExpertCreate(BaseModel):
    """Schema for creating an expert profile."""
    bio: Optional[str] = None
    expertise: str = Field(..., min_length=2, max_length=255)
    skills: Optional[str] = None          # Comma-separated: "Python, ML, FastAPI"
    experience: int = Field(default=0, ge=0, le=60)
    languages: str = Field(default="English")
    pricing: float = Field(default=0.0, ge=0)
    profile_image: Optional[str] = None
    categories: Optional[str] = None      # Comma-separated: "Tech, AI, Data Science"
    availability: Optional[Dict[str, Any]] = {}


class ExpertUpdate(BaseModel):
    """Schema for updating an expert profile."""
    bio: Optional[str] = None
    expertise: Optional[str] = None
    skills: Optional[str] = None
    experience: Optional[int] = Field(None, ge=0, le=60)
    languages: Optional[str] = None
    pricing: Optional[float] = Field(None, ge=0)
    profile_image: Optional[str] = None
    categories: Optional[str] = None
    availability: Optional[Dict[str, Any]] = None
    is_available: Optional[int] = None


class ExpertResponse(BaseModel):
    """Full expert profile response."""
    id: UUID
    user_id: UUID
    bio: Optional[str] = None
    expertise: str
    skills: Optional[str] = None
    experience: int
    languages: str
    pricing: float
    profile_image: Optional[str] = None
    categories: Optional[str] = None
    availability: Optional[Dict[str, Any]] = {}
    average_rating: float
    total_reviews: int
    total_bookings: int
    is_available: int
    created_at: datetime

    # Embedded user info
    user_name: Optional[str] = None
    user_email: Optional[str] = None

    model_config = {"from_attributes": True}


class ExpertListItem(BaseModel):
    """Compact expert card for listing/search results."""
    id: UUID
    user_id: UUID
    expertise: str
    skills: Optional[str] = None
    experience: int
    languages: str
    pricing: float
    profile_image: Optional[str] = None
    categories: Optional[str] = None
    average_rating: float
    total_reviews: int
    total_bookings: int
    is_available: int
    created_at: datetime
    user_name: Optional[str] = None

    model_config = {"from_attributes": True}


class ExpertFilter(BaseModel):
    """Query parameters for filtering experts."""
    search: Optional[str] = None          # Full-text search
    expertise: Optional[str] = None
    category: Optional[str] = None
    min_rating: Optional[float] = Field(None, ge=0, le=5)
    max_price: Optional[float] = Field(None, ge=0)
    min_experience: Optional[int] = Field(None, ge=0)
    language: Optional[str] = None
    is_available: Optional[int] = None
    sort_by: str = Field(default="average_rating")  # average_rating | pricing | experience | total_bookings
    sort_order: str = Field(default="desc")          # asc | desc
    page: int = Field(default=1, ge=1)
    page_size: int = Field(default=20, ge=1, le=100)
