"""
api/routes/auth.py
==================
Authentication endpoints: signup, login, profile, FCM token update.
"""

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.database import get_db
from app.schemas.user import UserCreate, UserLogin, UserResponse, UserUpdate
from app.schemas.token import Token
from app.services import auth_service
from app.api.dependencies.auth import get_current_user
from app.models.user import User

router = APIRouter(prefix="/api/auth", tags=["Authentication"])


@router.post("/signup", response_model=UserResponse, status_code=status.HTTP_201_CREATED)
async def signup(data: UserCreate, db: AsyncSession = Depends(get_db)):
    """
    Register a new user account.
    - Role must be 'user' or 'expert'.
    - Returns the created user profile (no token — user must login separately).
    """
    user = await auth_service.register_user(data, db)
    return user


@router.post("/login", response_model=Token)
async def login(data: UserLogin, db: AsyncSession = Depends(get_db)):
    """
    Authenticate with email and password.
    Returns a JWT access token + user role.
    """
    return await auth_service.login_user(data.email, data.password, db)


@router.get("/me", response_model=UserResponse)
async def get_me(current_user: User = Depends(get_current_user)):
    """Return the currently authenticated user's profile."""
    return current_user


@router.patch("/me", response_model=UserResponse)
async def update_me(
    data: UserUpdate,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """Update the current user's profile (name, profile_image, fcm_token)."""
    update_data = data.model_dump(exclude_unset=True)
    for field, value in update_data.items():
        setattr(current_user, field, value)
    db.add(current_user)
    return current_user


@router.post("/fcm-token")
async def update_fcm_token(
    fcm_token: str,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """
    Register or update the device's FCM token for push notifications.
    Call this after the Android app receives a new FCM token.
    """
    await auth_service.update_fcm_token(current_user, fcm_token, db)
    return {"message": "FCM token updated successfully."}
