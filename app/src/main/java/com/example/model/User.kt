package com.example.model

enum class Role {
    MANAGER,
    USER
}

enum class UserStatus {
    ACTIVE,
    DISABLED
}

enum class Presence {
    ONLINE,
    IN_CALL,
    OFFLINE
}

data class UserPermissions(
    val canCallUsers: Boolean = true,
    val canCallManager: Boolean = true,
    val canDialNewNumber: Boolean = true,
    val canVideo: Boolean = true,
    val canGroupCall: Boolean = true
)

data class CompanyUser(
    val userId: String, // unique lowercase ID, e.g. "vikram.ceo", "rahul.dev"
    val displayName: String,
    val phone: String, // digits or formatted phone number
    val role: Role,
    val status: UserStatus = UserStatus.ACTIVE,
    val department: String,
    val avatarColorHex: String = "#1F6FEB",
    val presence: Presence = Presence.ONLINE,
    val lastSeen: Long = System.currentTimeMillis(),
    val permissions: UserPermissions = UserPermissions(),
    val deviceId: String = "DEV-88219",
    val isDoNotDisturb: Boolean = false
)
