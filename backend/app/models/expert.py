"""
models/expert.py
================
SQLAlchemy ORM model for the `experts` table.
"""

from sqlalchemy import Column, String, Text, Float, Integer, DateTime, ForeignKey, ARRAY
from sqlalchemy.dialects.postgresql import UUID, JSONB
from sqlalchemy.orm import relationship
from datetime import datetime, timezone
import uuid

from app.core.database import Base


class Expert(Base):
    __tablename__ = "experts"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4, index=True)
    user_id = Column(UUID(as_uuid=True), ForeignKey("users.id", ondelete="CASCADE"), unique=True, nullable=False)

    bio = Column(Text, nullable=True)
    expertise = Column(String(255), nullable=False)          # Primary domain e.g. "Machine Learning"
    skills = Column(Text, nullable=True)                     # Comma-separated skills
    experience = Column(Integer, default=0, nullable=False)  # Years of experience
    languages = Column(String(255), default="English")       # Comma-separated
    pricing = Column(Float, default=0.0, nullable=False)     # Per hour in USD
    profile_image = Column(Text, nullable=True)
    categories = Column(String(512), nullable=True)          # Comma-separated categories

    # Availability as JSON: {"monday": ["09:00-10:00", "14:00-15:00"], ...}
    availability = Column(JSONB, default={})

    # Computed / cached rating fields (updated by triggers/queries)
    average_rating = Column(Float, default=0.0)
    total_reviews = Column(Integer, default=0)
    total_bookings = Column(Integer, default=0)

    is_available = Column(Integer, default=1)  # 1 = available, 0 = unavailable
    created_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc), nullable=False)

    # ── Relationships ─────────────────────────────────────────────────────────
    user = relationship("User", back_populates="expert_profile")
    bookings = relationship("Booking", foreign_keys="Booking.expert_id", back_populates="expert")
    reviews = relationship("Review", foreign_keys="Review.expert_id", back_populates="expert")
    favorited_by = relationship("Favorite", back_populates="expert")

    def __repr__(self) -> str:
        return f"<Expert id={self.id} expertise={self.expertise}>"
