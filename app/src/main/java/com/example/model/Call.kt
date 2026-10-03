package com.example.model

enum class CallType {
    AUDIO,
    VIDEO
}

enum class CallStatus {
    IDLE,
    DIALING,
    RINGING,
    CONNECTING,
    CONNECTED,
    RECONNECTING,
    ENDED,
    DECLINED,
    MISSED,
    BUSY
}

data class ActiveCallSession(
    val callId: String,
    val callerId: String,
    val callerName: String,
    val callerDept: String,
    val calleeId: String,
    val calleeName: String,
    val calleeDept: String,
    val callType: CallType,
    val isGroup: Boolean = false,
    val groupMembers: List<String> = emptyList(),
    val status: CallStatus = CallStatus.DIALING,
    val startedAt: Long = System.currentTimeMillis(),
    val connectedAt: Long? = null,
    val endedAt: Long? = null,
    val durationSec: Int = 0,
    val isMuted: Boolean = false,
    val isCameraOn: Boolean = true,
    val isSpeakerOn: Boolean = true,
    val isFrontCamera: Boolean = true,
    val networkQuality: String = "HD (1080p • 28ms)"
)
