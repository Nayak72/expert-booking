"""
schemas/booking.py
==================
Pydantic schemas for Booking request / response validation.
"""

from pydantic import BaseModel, Field
from typing import Optional
from datetime import datetime, date
from uuid import UUID


class BookingCreate(BaseModel):
    """Schema for creating a new booking."""
    expert_id: UUID
    booking_date: date
    slot: str = Field(..., pattern=r"^\d{2}:\d{2}-\d{2}:\d{2}$", description="e.g. '09:00-10:00'")
    notes: Optional[str] = None


class BookingUpdate(BaseModel):
    """Schema for rescheduling a booking."""
    booking_date: Optional[date] = None
    slot: Optional[str] = Field(None, pattern=r"^\d{2}:\d{2}-\d{2}:\d{2}$")
    notes: Optional[str] = None


class BookingCancel(BaseModel):
    """Schema for cancelling a booking."""
    cancellation_reason: Optional[str] = None


class BookingResponse(BaseModel):
    """Full booking response."""
    id: UUID
    user_id: UUID
    expert_id: UUID
    booking_date: date
    slot: str
    status: str
    notes: Optional[str] = None
    meeting_link: Optional[str] = None
    cancellation_reason: Optional[str] = None
    created_at: datetime
    updated_at: datetime

    # Embedded info
    expert_name: Optional[str] = None
    expert_expertise: Optional[str] = None
    expert_profile_image: Optional[str] = None
    user_name: Optional[str] = None

    model_config = {"from_attributes": True}


class SlotAvailabilityUpdate(BaseModel):
    """WebSocket broadcast payload for real-time slot updates."""
    expert_id: str
    booking_date: str
    slot: str
    is_booked: bool
    event: str  # "slot_booked" | "slot_released"
