"""
models/user.py
==============
SQLAlchemy ORM model for the `users` table.
"""

from sqlalchemy import Column, String, Enum, DateTime, Text
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import relationship
from datetime import datetime, timezone
import uuid

from app.core.database import Base


class User(Base):
    __tablename__ = "users"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4, index=True)
    name = Column(String(255), nullable=False)
    email = Column(String(255), unique=True, nullable=False, index=True)
    password_hash = Column(String(255), nullable=False)
    role = Column(Enum("user", "expert", name="user_role"), default="user", nullable=False)
    profile_image = Column(Text, nullable=True)
    fcm_token = Column(String(512), nullable=True)  # For push notifications
    created_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc), nullable=False)
    updated_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc),
                        onupdate=lambda: datetime.now(timezone.utc))

    # ── Relationships ─────────────────────────────────────────────────────────
    expert_profile = relationship("Expert", back_populates="user", uselist=False, cascade="all, delete-orphan")
    bookings = relationship("Booking", foreign_keys="Booking.user_id", back_populates="user", cascade="all, delete-orphan")
    reviews = relationship("Review", foreign_keys="Review.user_id", back_populates="reviewer", cascade="all, delete-orphan")
    favorites = relationship("Favorite", back_populates="user", cascade="all, delete-orphan")

    def __repr__(self) -> str:
        return f"<User id={self.id} email={self.email} role={self.role}>"
