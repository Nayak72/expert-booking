"""
utils/pagination.py
===================
Pagination helper utilities.
"""

from typing import TypeVar, Generic, List
from pydantic import BaseModel

T = TypeVar("T")


class PaginatedResponse(BaseModel, Generic[T]):
    """Generic paginated response wrapper."""
    items: List[T]
    total: int
    page: int
    page_size: int
    total_pages: int


def paginate(total: int, page: int, page_size: int) -> dict:
    """Calculate pagination metadata."""
    return {
        "total": total,
        "page": page,
        "page_size": page_size,
        "total_pages": max(1, (total + page_size - 1) // page_size),
        "has_next": page * page_size < total,
        "has_prev": page > 1,
    }
