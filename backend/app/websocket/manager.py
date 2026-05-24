"""
websocket/manager.py
====================
WebSocket connection manager for real-time slot availability broadcasting.
Manages a dictionary of active connections keyed by expert_id.
"""

from fastapi import WebSocket
from typing import Dict, List, Set
import json
import logging

logger = logging.getLogger(__name__)


class SlotConnectionManager:
    """
    Manages active WebSocket connections.

    Connections are grouped by expert_id so that only relevant clients
    receive slot update broadcasts.
    """

    def __init__(self):
        # Map: expert_id (str) → set of connected WebSockets
        self._connections: Dict[str, Set[WebSocket]] = {}

    async def connect(self, websocket: WebSocket, expert_id: str) -> None:
        """Accept a WebSocket connection and register it for a specific expert."""
        await websocket.accept()
        if expert_id not in self._connections:
            self._connections[expert_id] = set()
        self._connections[expert_id].add(websocket)
        logger.info(
            f"WS connected for expert {expert_id}. "
            f"Total connections: {self.total_connections}"
        )

    def disconnect(self, websocket: WebSocket, expert_id: str) -> None:
        """Remove a WebSocket from the connection pool."""
        if expert_id in self._connections:
            self._connections[expert_id].discard(websocket)
            if not self._connections[expert_id]:
                del self._connections[expert_id]
        logger.info(
            f"WS disconnected for expert {expert_id}. "
            f"Total connections: {self.total_connections}"
        )

    async def broadcast_to_expert(self, expert_id: str, payload: dict) -> None:
        """
        Broadcast a JSON payload to all clients watching a specific expert's slots.

        Args:
            expert_id: The expert whose slot status changed.
            payload:   JSON-serializable dict to broadcast.
        """
        connections = self._connections.get(expert_id, set()).copy()
        if not connections:
            return

        message = json.dumps(payload)
        dead_connections = set()

        for websocket in connections:
            try:
                await websocket.send_text(message)
            except Exception as e:
                logger.warning(f"Failed to send to WS client: {e}")
                dead_connections.add(websocket)

        # Clean up broken connections
        for ws in dead_connections:
            self.disconnect(ws, expert_id)

    async def broadcast_global(self, payload: dict) -> None:
        """Broadcast a message to ALL connected WebSocket clients."""
        message = json.dumps(payload)
        for expert_id, connections in list(self._connections.items()):
            for websocket in connections.copy():
                try:
                    await websocket.send_text(message)
                except Exception:
                    self.disconnect(websocket, expert_id)

    @property
    def total_connections(self) -> int:
        return sum(len(conns) for conns in self._connections.values())


# Singleton manager — shared across all routes
connection_manager = SlotConnectionManager()
