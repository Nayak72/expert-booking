"""
models/__init__.py
==================
Import all models so SQLAlchemy metadata is populated
before create_all() is called.
"""

from app.models.user import User
from app.models.expert import Expert
from app.models.booking import Booking
from app.models.review import Review
from app.models.favorite import Favorite

__all__ = ["User", "Expert", "Booking", "Review", "Favorite"]
