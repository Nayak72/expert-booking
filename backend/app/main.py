"""
app/main.py
===========
FastAPI application entrypoint.

Registers:
- All API routers
- CORS and logging middleware
- Lifespan events (DB table creation, recommendation engine initialization)
- Health check endpoint
"""

import logging
from contextlib import asynccontextmanager
from fastapi import FastAPI

from app.core.config import settings
from app.core.database import create_tables, AsyncSessionLocal
from app.middleware.cors import add_cors_middleware
from app.middleware.logging import RequestLoggingMiddleware
from app.recommendation.recommender import rebuild_engine

# ── Import all models so metadata is populated before create_all ──────────────
import app.models  # noqa: F401

# ── API Routers ───────────────────────────────────────────────────────────────
from app.api.routes import auth, experts, bookings, reviews, favorites, recommendations

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s | %(levelname)-8s | %(name)s | %(message)s",
)
logger = logging.getLogger(__name__)


# ── Lifespan ──────────────────────────────────────────────────────────────────
@asynccontextmanager
async def lifespan(app: FastAPI):
    """
    Application startup/shutdown lifecycle handler.
    Startup:
      1. Create all DB tables (development convenience).
      2. Build the recommendation engine.
    """
    logger.info(f"Starting {settings.APP_NAME} v{settings.APP_VERSION} ...")

    # Create tables
    await create_tables()

    # Build recommendation engine
    async with AsyncSessionLocal() as db:
        try:
            await rebuild_engine(db)
        except Exception as e:
            logger.warning(f"Recommendation engine build failed (non-fatal): {e}")

    logger.info("Application startup complete.")
    yield
    logger.info("Application shutting down.")


# ── FastAPI Application ───────────────────────────────────────────────────────
app = FastAPI(
    title=settings.APP_NAME,
    version=settings.APP_VERSION,
    description="""
    ## AI-Powered Expert Consultation & Booking Platform API

    A complete REST API for:
    - **Authentication** — JWT-based signup, login, role management
    - **Expert Discovery** — search, filter, sort experts
    - **Booking** — book, cancel, reschedule sessions
    - **Reviews** — rate and review experts
    - **Favorites** — bookmark experts
    - **Recommendations** — ML-powered personalized expert recommendations
    - **Real-time** — WebSocket slot availability updates
    - **Notifications** — Firebase Cloud Messaging push notifications
    """,
    docs_url="/docs",
    redoc_url="/redoc",
    lifespan=lifespan,
)

# ── Middleware ────────────────────────────────────────────────────────────────
add_cors_middleware(app)
app.add_middleware(RequestLoggingMiddleware)

# ── Routes ────────────────────────────────────────────────────────────────────
app.include_router(auth.router)
app.include_router(experts.router)
app.include_router(bookings.router)
app.include_router(reviews.router)
app.include_router(favorites.router)
app.include_router(recommendations.router)


# ── Health Check ──────────────────────────────────────────────────────────────
@app.get("/health", tags=["Health"])
async def health_check():
    """Quick health check endpoint."""
    return {
        "status": "healthy",
        "app": settings.APP_NAME,
        "version": settings.APP_VERSION,
    }


@app.get("/", tags=["Root"])
async def root():
    return {
        "message": f"Welcome to {settings.APP_NAME} API",
        "docs": "/docs",
        "version": settings.APP_VERSION,
    }
