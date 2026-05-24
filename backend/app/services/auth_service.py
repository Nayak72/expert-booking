"""
services/auth_service.py
========================
Business logic for user authentication: registration and login.
"""

from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from fastapi import HTTPException, status

from app.models.user import User
from app.schemas.user import UserCreate
from app.schemas.token import Token
from app.core.security import hash_password, verify_password, create_access_token


async def register_user(data: UserCreate, db: AsyncSession) -> User:
    """
    Register a new user.

    Raises:
        400 if email already exists.
    """
    # Check for existing email
    result = await db.execute(select(User).where(User.email == data.email))
    if result.scalar_one_or_none():
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="An account with this email already exists.",
        )

    user = User(
        name=data.name,
        email=data.email,
        password_hash=hash_password(data.password),
        role=data.role,
    )
    db.add(user)
    await db.flush()   # Get the generated UUID before commit
    await db.refresh(user)
    return user


async def login_user(email: str, password: str, db: AsyncSession) -> Token:
    """
    Authenticate a user and return a JWT token.

    Raises:
        401 if credentials are invalid.
    """
    result = await db.execute(select(User).where(User.email == email))
    user = result.scalar_one_or_none()

    if not user or not verify_password(password, user.password_hash):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid email or password.",
            headers={"WWW-Authenticate": "Bearer"},
        )

    access_token = create_access_token(subject=user.id, role=user.role)
    return Token(
        access_token=access_token,
        token_type="bearer",
        role=user.role,
        user_id=str(user.id),
    )


async def update_fcm_token(user: User, fcm_token: str, db: AsyncSession) -> None:
    """Store or update the user's FCM device token."""
    user.fcm_token = fcm_token
    db.add(user)
