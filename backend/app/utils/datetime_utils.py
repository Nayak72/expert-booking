"""
utils/datetime_utils.py
=======================
Date/time helpers for slot validation and timezone handling.
"""

from datetime import datetime, date, time, timezone
from typing import List
import pytz


# Standard consultation slot durations (1 hour)
DEFAULT_SLOTS = [
    "08:00-09:00", "09:00-10:00", "10:00-11:00", "11:00-12:00",
    "12:00-13:00", "13:00-14:00", "14:00-15:00", "15:00-16:00",
    "16:00-17:00", "17:00-18:00", "18:00-19:00", "19:00-20:00",
]


def is_valid_slot_format(slot: str) -> bool:
    """Validate slot format: HH:MM-HH:MM."""
    try:
        start_str, end_str = slot.split("-")
        start = datetime.strptime(start_str, "%H:%M").time()
        end = datetime.strptime(end_str, "%H:%M").time()
        return start < end
    except (ValueError, AttributeError):
        return False


def is_future_date(booking_date: date) -> bool:
    """Check that the booking date is today or in the future."""
    return booking_date >= date.today()


def get_available_slots(booked_slots: List[str], all_slots: List[str] = None) -> List[str]:
    """
    Return the list of available slots after excluding booked ones.

    Args:
        booked_slots: List of already-booked slot strings.
        all_slots:    Full set of possible slots (defaults to DEFAULT_SLOTS).

    Returns:
        List of available slot strings.
    """
    slots = all_slots or DEFAULT_SLOTS
    return [s for s in slots if s not in booked_slots]


def utc_now() -> datetime:
    """Return the current UTC datetime (timezone-aware)."""
    return datetime.now(timezone.utc)


def to_ist(dt: datetime) -> datetime:
    """Convert a UTC datetime to Indian Standard Time (IST)."""
    ist = pytz.timezone("Asia/Kolkata")
    return dt.astimezone(ist)
