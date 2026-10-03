package com.example.model

data class CompanySettings(
    val companyName: String = "TechNova Solutions Ltd.",
    val ringTimeoutSec: Int = 45,
    val maxCallDurationMinutes: Int = 0, // 0 = unlimited
    val maintenanceMode: Boolean = false,
    val allowGroupCalls: Boolean = true,
    val singleDeviceLock: Boolean = true,
    val defaultCountryCode: String = "+91",
    val stunServer: String = "stun:stun.l.google.com:19302",
    val turnServer: String = "turn:turn.companycall.com:3478",
    val minAppVersion: String = "1.0.0",
    val googleDriveConnected: Boolean = true,
    val lastDriveBackup: String = "Today, 04:30 AM (Auto-synced)"
)
