"""
websocket/slot_broadcaster.py
==============================
Helper functions to broadcast slot availability changes
after booking/cancellation events.
"""

from app.websocket.manager import connection_manager
import logging

logger = logging.getLogger(__name__)


async def broadcast_slot_booked(
    expert_id: str,
    booking_date: str,
    slot: str,
    booking_id: str,
) -> None:
    """
    Notify all connected clients watching this expert that a slot was booked.
    Called immediately after a successful booking creation.
    """
    payload = {
        "event": "slot_booked",
        "expert_id": expert_id,
        "booking_date": booking_date,
        "slot": slot,
        "booking_id": booking_id,
        "is_available": False,
    }
    await connection_manager.broadcast_to_expert(expert_id, payload)
    logger.info(f"Broadcasted slot_booked: expert={expert_id} date={booking_date} slot={slot}")


async def broadcast_slot_released(
    expert_id: str,
    booking_date: str,
    slot: str,
) -> None:
    """
    Notify all connected clients watching this expert that a slot was released
    (due to cancellation or rescheduling).
    """
    payload = {
        "event": "slot_released",
        "expert_id": expert_id,
        "booking_date": booking_date,
        "slot": slot,
        "is_available": True,
    }
    await connection_manager.broadcast_to_expert(expert_id, payload)
    logger.info(f"Broadcasted slot_released: expert={expert_id} date={booking_date} slot={slot}")
