"""
services/notification_service.py
=================================
Firebase Cloud Messaging (FCM) push notification service.
Sends notifications for booking events.
"""

import logging
import os
from typing import Optional

logger = logging.getLogger(__name__)

# ── Firebase Initialization ───────────────────────────────────────────────────
_firebase_initialized = False


def _init_firebase():
    """Initialize Firebase Admin SDK (lazy, only once)."""
    global _firebase_initialized
    if _firebase_initialized:
        return

    try:
        import firebase_admin
        from firebase_admin import credentials

        creds_path = os.getenv("FIREBASE_CREDENTIALS_PATH", "firebase_credentials.json")
        if os.path.exists(creds_path):
            cred = credentials.Certificate(creds_path)
            firebase_admin.initialize_app(cred)
            _firebase_initialized = True
            logger.info("Firebase Admin SDK initialized successfully.")
        else:
            logger.warning(
                f"Firebase credentials not found at '{creds_path}'. "
                "FCM notifications will be disabled. "
                "Add your firebase_credentials.json to enable."
            )
    except Exception as e:
        logger.error(f"Firebase initialization failed: {e}")


async def send_push_notification(
    fcm_token: Optional[str],
    title: str,
    body: str,
    data: Optional[dict] = None,
) -> bool:
    """
    Send a push notification to a single device via FCM.

    Args:
        fcm_token: The target device's FCM registration token.
        title:     Notification title.
        body:      Notification body text.
        data:      Optional extra data payload (key-value strings).

    Returns:
        True if notification was sent successfully, False otherwise.
    """
    if not fcm_token:
        logger.debug("No FCM token provided, skipping notification.")
        return False

    _init_firebase()
    if not _firebase_initialized:
        return False

    try:
        from firebase_admin import messaging

        message = messaging.Message(
            notification=messaging.Notification(title=title, body=body),
            data={k: str(v) for k, v in (data or {}).items()},
            token=fcm_token,
            android=messaging.AndroidConfig(
                priority="high",
                notification=messaging.AndroidNotification(
                    sound="default",
                    click_action="FLUTTER_NOTIFICATION_CLICK",
                ),
            ),
        )
        response = messaging.send(message)
        logger.info(f"FCM notification sent: {response}")
        return True
    except Exception as e:
        logger.error(f"Failed to send FCM notification: {e}")
        return False


# ── Notification Templates ─────────────────────────────────────────────────────
async def notify_booking_confirmed(fcm_token: Optional[str], expert_name: str, date: str, slot: str):
    await send_push_notification(
        fcm_token=fcm_token,
        title="✅ Booking Confirmed!",
        body=f"Your session with {expert_name} is confirmed for {date} at {slot}.",
        data={"type": "booking_confirmed", "date": date, "slot": slot},
    )


async def notify_booking_cancelled(fcm_token: Optional[str], expert_name: str, date: str):
    await send_push_notification(
        fcm_token=fcm_token,
        title="❌ Booking Cancelled",
        body=f"Your session with {expert_name} on {date} has been cancelled.",
        data={"type": "booking_cancelled", "date": date},
    )


async def notify_session_reminder(fcm_token: Optional[str], expert_name: str, slot: str):
    await send_push_notification(
        fcm_token=fcm_token,
        title="⏰ Session Reminder",
        body=f"You have a session with {expert_name} starting at {slot}. Get ready!",
        data={"type": "session_reminder", "slot": slot},
    )


async def notify_booking_rescheduled(fcm_token: Optional[str], expert_name: str, new_date: str, new_slot: str):
    await send_push_notification(
        fcm_token=fcm_token,
        title="📅 Booking Rescheduled",
        body=f"Your session with {expert_name} has been moved to {new_date} at {new_slot}.",
        data={"type": "booking_rescheduled", "date": new_date, "slot": new_slot},
    )
