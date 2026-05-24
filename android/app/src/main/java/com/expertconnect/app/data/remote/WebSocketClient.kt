package com.expertconnect.app.data.remote

import com.expertconnect.app.BuildConfig
import com.expertconnect.app.data.remote.dto.SlotUpdateMessage
import com.google.gson.Gson
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject
import javax.inject.Singleton

/**
 * WebSocket client for real-time slot availability updates.
 *
 * Usage:
 *   1. Call connect(expertId) to start listening.
 *   2. Collect slotUpdates Flow to receive updates.
 *   3. Call disconnect() when done.
 */
@Singleton
class WebSocketClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson
) {
    private var webSocket: WebSocket? = null
    private val _slotUpdates = Channel<SlotUpdateMessage>(Channel.BUFFERED)
    val slotUpdates: Flow<SlotUpdateMessage> = _slotUpdates.receiveAsFlow()

    /**
     * Connect to the slot WebSocket for a specific expert.
     * @param expertId The expert's UUID string.
     */
    fun connect(expertId: String) {
        val url = "${BuildConfig.WS_BASE_URL}api/experts/ws/$expertId"
        val request = Request.Builder().url(url).build()

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                // Start heartbeat
                webSocket.send("ping")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                if (text == "pong") return
                try {
                    val update = gson.fromJson(text, SlotUpdateMessage::class.java)
                    _slotUpdates.trySend(update)
                } catch (e: Exception) {
                    // Ignore malformed messages
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                // Connection failed — could reconnect here
            }
        })
    }

    /** Disconnect the current WebSocket connection. */
    fun disconnect() {
        webSocket?.close(1000, "Client disconnected")
        webSocket = null
    }
}
