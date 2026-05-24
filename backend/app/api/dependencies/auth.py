"""
api/dependencies/auth.py
========================
FastAPI dependencies for authentication and authorization.
Provides get_current_user (with DB lookup) and require_role.
"""

from fastapi import Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from uuid import UUID

from app.core.database import get_db
from app.core.security import get_current_user_payload
from app.models.user import User


async def get_current_user(
    payload: dict = Depends(get_current_user_payload),
    db: AsyncSession = Depends(get_db),
) -> User:
    """
    Full current user dependency: validates JWT and returns the User ORM object.
    Raises 401 if token invalid, 404 if user no longer exists.
    """
    user_id = payload.get("user_id")
    result = await db.execute(select(User).where(User.id == UUID(user_id)))
    user = result.scalar_one_or_none()

    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User not found.",
        )
    return user


def require_role(*roles: str):
    """
    Role-based authorization dependency factory.

    Usage:
        @router.get("/expert-only", dependencies=[Depends(require_role("expert"))])
    """
    async def role_checker(
        current_user: User = Depends(get_current_user),
    ) -> User:
        if current_user.role not in roles:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail=f"Access denied. Required role(s): {', '.join(roles)}.",
            )
        return current_user

    return role_checker
