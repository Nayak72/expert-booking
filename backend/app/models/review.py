"""
models/review.py
================
SQLAlchemy ORM model for the `reviews` table.
"""

from sqlalchemy import Column, Integer, Float, Text, DateTime, ForeignKey, UniqueConstraint
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import relationship
from datetime import datetime, timezone
import uuid

from app.core.database import Base


class Review(Base):
    __tablename__ = "reviews"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4, index=True)
    user_id = Column(UUID(as_uuid=True), ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    expert_id = Column(UUID(as_uuid=True), ForeignKey("experts.id", ondelete="CASCADE"), nullable=False, index=True)

    rating = Column(Float, nullable=False)         # 1.0 – 5.0
    review_text = Column(Text, nullable=True)
    created_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc))
    updated_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc),
                        onupdate=lambda: datetime.now(timezone.utc))

    # A user can only review an expert once
    __table_args__ = (
        UniqueConstraint("user_id", "expert_id", name="uq_user_expert_review"),
    )

    # ── Relationships ─────────────────────────────────────────────────────────
    reviewer = relationship("User", foreign_keys=[user_id], back_populates="reviews")
    expert = relationship("Expert", foreign_keys=[expert_id], back_populates="reviews")

    def __repr__(self) -> str:
        return f"<Review id={self.id} rating={self.rating}>"
