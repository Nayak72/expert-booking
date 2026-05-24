"""
models/favorite.py
==================
SQLAlchemy ORM model for the `favorites` table.
"""

from sqlalchemy import Column, DateTime, ForeignKey, UniqueConstraint
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import relationship
from datetime import datetime, timezone
import uuid

from app.core.database import Base


class Favorite(Base):
    __tablename__ = "favorites"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4, index=True)
    user_id = Column(UUID(as_uuid=True), ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    expert_id = Column(UUID(as_uuid=True), ForeignKey("experts.id", ondelete="CASCADE"), nullable=False, index=True)
    created_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc))

    # A user can only favorite an expert once
    __table_args__ = (
        UniqueConstraint("user_id", "expert_id", name="uq_user_expert_favorite"),
    )

    # ── Relationships ─────────────────────────────────────────────────────────
    user = relationship("User", back_populates="favorites")
    expert = relationship("Expert", back_populates="favorited_by")

    def __repr__(self) -> str:
        return f"<Favorite user={self.user_id} expert={self.expert_id}>"
