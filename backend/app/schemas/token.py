"""
schemas/token.py
================
Pydantic schemas for JWT token responses.
"""

from pydantic import BaseModel
from typing import Optional


class Token(BaseModel):
    """JWT access token response."""
    access_token: str
    token_type: str = "bearer"
    role: str
    user_id: str


class TokenData(BaseModel):
    """Data extracted from a decoded JWT token."""
    user_id: Optional[str] = None
    role: Optional[str] = None
