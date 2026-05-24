"""
models/booking.py
=================
SQLAlchemy ORM model for the `bookings` table.
"""

from sqlalchemy import Column, String, Enum, DateTime, ForeignKey, Date, Text
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import relationship
from datetime import datetime, timezone
import uuid

from app.core.database import Base


class Booking(Base):
    __tablename__ = "bookings"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4, index=True)
    user_id = Column(UUID(as_uuid=True), ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    expert_id = Column(UUID(as_uuid=True), ForeignKey("experts.id", ondelete="CASCADE"), nullable=False, index=True)

    booking_date = Column(Date, nullable=False)
    slot = Column(String(50), nullable=False)    # e.g. "09:00-10:00"
    status = Column(
        Enum("pending", "confirmed", "cancelled", "completed", name="booking_status", native_enum=False),
        default="pending",
        nullable=False,
    )
    notes = Column(Text, nullable=True)           # Optional user notes
    meeting_link = Column(String(512), nullable=True)
    cancellation_reason = Column(Text, nullable=True)

    created_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc))
    updated_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc),
                        onupdate=lambda: datetime.now(timezone.utc))

    # ── Relationships ─────────────────────────────────────────────────────────
    user = relationship("User", foreign_keys=[user_id], back_populates="bookings")
    expert = relationship("Expert", foreign_keys=[expert_id], back_populates="bookings")

    def __repr__(self) -> str:
        return f"<Booking id={self.id} status={self.status} date={self.booking_date}>"
