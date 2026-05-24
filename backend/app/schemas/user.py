"""
schemas/user.py
===============
Pydantic schemas for User request / response validation.
"""

from pydantic import BaseModel, EmailStr, Field
from typing import Optional
from datetime import datetime
from uuid import UUID


class UserCreate(BaseModel):
    """Schema for user signup."""
    name: str = Field(..., min_length=2, max_length=255)
    email: EmailStr
    password: str = Field(..., min_length=6, max_length=128)
    role: str = Field(default="user", pattern="^(user|expert)$")


class UserLogin(BaseModel):
    """Schema for user login."""
    email: EmailStr
    password: str


class UserUpdate(BaseModel):
    """Schema for updating user profile."""
    name: Optional[str] = Field(None, min_length=2, max_length=255)
    profile_image: Optional[str] = None
    fcm_token: Optional[str] = None


class UserResponse(BaseModel):
    """Public-facing user response schema."""
    id: UUID
    name: str
    email: str
    role: str
    profile_image: Optional[str] = None
    created_at: datetime

    model_config = {"from_attributes": True}


class UserPublic(BaseModel):
    """Minimal user info for embedding in other responses."""
    id: UUID
    name: str
    profile_image: Optional[str] = None

    model_config = {"from_attributes": True}
