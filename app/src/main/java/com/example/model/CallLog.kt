package com.example.model

enum class CallDirection {
    INCOMING,
    OUTGOING
}

enum class CallOutcome {
    COMPLETED,
    MISSED,
    DECLINED,
    BUSY,
    CANCELLED
}

data class CompanyCallLog(
    val id: String,
    val callerId: String,
    val callerName: String,
    val calleeId: String,
    val calleeName: String,
    val otherPartyName: String,
    val otherPartyDept: String,
    val direction: CallDirection,
    val type: CallType,
    val outcome: CallOutcome,
    val timestamp: Long,
    val durationSec: Int
)
