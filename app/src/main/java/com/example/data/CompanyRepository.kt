package com.example.data

import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

object CompanyRepository {

    private val initialUsers = listOf(
        CompanyUser(
            userId = "vikram.ceo",
            displayName = "Vikram Sharma",
            phone = "+91 98100 11223",
            role = Role.MANAGER,
            status = UserStatus.ACTIVE,
            department = "Executive",
            avatarColorHex = "#1F6FEB",
            presence = Presence.ONLINE,
            permissions = UserPermissions(canCallUsers = true, canCallManager = true, canDialNewNumber = true, canVideo = true, canGroupCall = true)
        ),
        CompanyUser(
            userId = "rahul.dev",
            displayName = "Rahul Verma",
            phone = "+91 98200 44556",
            role = Role.USER,
            status = UserStatus.ACTIVE,
            department = "Engineering",
            avatarColorHex = "#2E9E5B",
            presence = Presence.ONLINE,
            permissions = UserPermissions(canCallUsers = true, canCallManager = true, canDialNewNumber = true, canVideo = true, canGroupCall = false)
        ),
        CompanyUser(
            userId = "priya.design",
            displayName = "Priya Patel",
            phone = "+91 98300 77889",
            role = Role.USER,
            status = UserStatus.ACTIVE,
            department = "Design",
            avatarColorHex = "#9C27B0",
            presence = Presence.ONLINE,
            permissions = UserPermissions(canCallUsers = true, canCallManager = true, canDialNewNumber = false, canVideo = true, canGroupCall = false)
        ),
        CompanyUser(
            userId = "ananya.pm",
            displayName = "Ananya Singh",
            phone = "+91 98400 22334",
            role = Role.USER,
            status = UserStatus.ACTIVE,
            department = "Product",
            avatarColorHex = "#E91E63",
            presence = Presence.IN_CALL,
            permissions = UserPermissions(canCallUsers = true, canCallManager = true, canDialNewNumber = true, canVideo = true, canGroupCall = true)
        ),
        CompanyUser(
            userId = "amit.qa",
            displayName = "Amit Kumar",
            phone = "+91 98500 55667",
            role = Role.USER,
            status = UserStatus.ACTIVE,
            department = "Quality Assurance",
            avatarColorHex = "#F59E0B",
            presence = Presence.ONLINE,
            permissions = UserPermissions(canCallUsers = true, canCallManager = true, canDialNewNumber = true, canVideo = false, canGroupCall = false)
        ),
        CompanyUser(
            userId = "sneha.hr",
            displayName = "Sneha Gupta",
            phone = "+91 98600 88990",
            role = Role.USER,
            status = UserStatus.ACTIVE,
            department = "Human Resources",
            avatarColorHex = "#0284C7",
            presence = Presence.OFFLINE,
            permissions = UserPermissions(canCallUsers = true, canCallManager = true, canDialNewNumber = false, canVideo = true, canGroupCall = false)
        )
    )

    private val initialCallLogs = listOf(
        CompanyCallLog(
            id = "log-1",
            callerId = "rahul.dev",
            callerName = "Rahul Verma",
            calleeId = "vikram.ceo",
            calleeName = "Vikram Sharma",
            otherPartyName = "Rahul Verma",
            otherPartyDept = "Engineering",
            direction = CallDirection.INCOMING,
            type = CallType.VIDEO,
            outcome = CallOutcome.COMPLETED,
            timestamp = System.currentTimeMillis() - 1000 * 60 * 35,
            durationSec = 432
        ),
        CompanyCallLog(
            id = "log-2",
            callerId = "vikram.ceo",
            callerName = "Vikram Sharma",
            calleeId = "priya.design",
            calleeName = "Priya Patel",
            otherPartyName = "Priya Patel",
            otherPartyDept = "Design",
            direction = CallDirection.OUTGOING,
            type = CallType.VIDEO,
            outcome = CallOutcome.COMPLETED,
            timestamp = System.currentTimeMillis() - 1000 * 60 * 120,
            durationSec = 780
        ),
        CompanyCallLog(
            id = "log-3",
            callerId = "amit.qa",
            callerName = "Amit Kumar",
            calleeId = "vikram.ceo",
            calleeName = "Vikram Sharma",
            otherPartyName = "Amit Kumar",
            otherPartyDept = "Quality Assurance",
            direction = CallDirection.INCOMING,
            type = CallType.AUDIO,
            outcome = CallOutcome.MISSED,
            timestamp = System.currentTimeMillis() - 1000 * 60 * 240,
            durationSec = 0
        ),
        CompanyCallLog(
            id = "log-4",
            callerId = "vikram.ceo",
            callerName = "Vikram Sharma",
            calleeId = "ananya.pm",
            calleeName = "Ananya Singh",
            otherPartyName = "Ananya Singh",
            otherPartyDept = "Product",
            direction = CallDirection.OUTGOING,
            type = CallType.AUDIO,
            outcome = CallOutcome.COMPLETED,
            timestamp = System.currentTimeMillis() - 1000 * 60 * 360,
            durationSec = 215
        )
    )

    private val _users = MutableStateFlow(initialUsers)
    val users: StateFlow<List<CompanyUser>> = _users.asStateFlow()

    private val _currentUser = MutableStateFlow<CompanyUser?>(initialUsers.first())
    val currentUser: StateFlow<CompanyUser?> = _currentUser.asStateFlow()

    private val _callLogs = MutableStateFlow(initialCallLogs)
    val callLogs: StateFlow<List<CompanyCallLog>> = _callLogs.asStateFlow()

    private val _settings = MutableStateFlow(CompanySettings())
    val settings: StateFlow<CompanySettings> = _settings.asStateFlow()

    private val _currentCall = MutableStateFlow<ActiveCallSession?>(null)
    val currentCall: StateFlow<ActiveCallSession?> = _currentCall.asStateFlow()

    // Active calls company-wide (for manager overview)
    private val _companyActiveCalls = MutableStateFlow<List<ActiveCallSession>>(
        listOf(
            ActiveCallSession(
                callId = "call-monitor-1",
                callerId = "ananya.pm",
                callerName = "Ananya Singh",
                callerDept = "Product",
                calleeId = "sneha.hr",
                calleeName = "Sneha Gupta",
                calleeDept = "Human Resources",
                callType = CallType.AUDIO,
                status = CallStatus.CONNECTED,
                startedAt = System.currentTimeMillis() - 1000 * 185,
                durationSec = 185
            )
        )
    )
    val companyActiveCalls: StateFlow<List<ActiveCallSession>> = _companyActiveCalls.asStateFlow()

    fun switchUser(userId: String) {
        val user = _users.value.find { it.userId == userId }
        if (user != null) {
            _currentUser.value = user
        }
    }

    fun login(idOrPhone: String): Result<CompanyUser> {
        val clean = idOrPhone.trim().lowercase().replace(" ", "").replace("-", "")
        val user = _users.value.find {
            it.userId.lowercase() == clean ||
            it.phone.replace(" ", "").replace("-", "").endsWith(clean)
        }
        return if (user != null) {
            if (user.status == UserStatus.DISABLED) {
                Result.failure(Exception("Your account has been disabled by the manager."))
            } else {
                _currentUser.value = user
                Result.success(user)
            }
        } else {
            Result.failure(Exception("Account not found. Please contact your company manager."))
        }
    }

    fun logout() {
        _currentUser.value = null
    }

    fun addUser(
        userId: String,
        displayName: String,
        phone: String,
        department: String,
        permissions: UserPermissions
    ): Boolean {
        val cleanId = userId.trim().lowercase().replace(" ", "")
        if (_users.value.any { it.userId.lowercase() == cleanId }) {
            return false
        }
        val colors = listOf("#1F6FEB", "#2E9E5B", "#9C27B0", "#E91E63", "#0284C7", "#D97706")
        val newUser = CompanyUser(
            userId = cleanId,
            displayName = displayName.trim(),
            phone = phone.trim(),
            role = Role.USER,
            status = UserStatus.ACTIVE,
            department = department.trim().ifEmpty { "General" },
            avatarColorHex = colors[_users.value.size % colors.size],
            presence = Presence.OFFLINE,
            permissions = permissions
        )
        _users.value = _users.value + newUser
        return true
    }

    fun updateUser(
        userId: String,
        displayName: String,
        phone: String,
        department: String,
        permissions: UserPermissions
    ) {
        _users.value = _users.value.map {
            if (it.userId == userId) {
                it.copy(
                    displayName = displayName,
                    phone = phone,
                    department = department,
                    permissions = permissions
                )
            } else it
        }
        if (_currentUser.value?.userId == userId) {
            _currentUser.value = _users.value.find { it.userId == userId }
        }
    }

    fun toggleUserStatus(userId: String) {
        _users.value = _users.value.map {
            if (it.userId == userId) {
                val newStatus = if (it.status == UserStatus.ACTIVE) UserStatus.DISABLED else UserStatus.ACTIVE
                it.copy(status = newStatus)
            } else it
        }
        if (_currentUser.value?.userId == userId && _currentUser.value?.status == UserStatus.DISABLED) {
            _currentUser.value = null
        }
    }

    fun deleteUser(userId: String) {
        _users.value = _users.value.filterNot { it.userId == userId }
        if (_currentUser.value?.userId == userId) {
            _currentUser.value = null
        }
    }

    fun toggleDoNotDisturb() {
        val curr = _currentUser.value ?: return
        val updated = curr.copy(isDoNotDisturb = !curr.isDoNotDisturb)
        _currentUser.value = updated
        _users.value = _users.value.map { if (it.userId == curr.userId) updated else it }
    }

    fun updateSettings(newSettings: CompanySettings) {
        _settings.value = newSettings
    }

    // Call Engine Operations
    fun initiateCall(callee: CompanyUser, type: CallType): Result<ActiveCallSession> {
        val caller = _currentUser.value ?: return Result.failure(Exception("Not logged in"))
        
        // Permission check
        if (caller.role != Role.MANAGER) {
            if (type == CallType.VIDEO && !caller.permissions.canVideo) {
                return Result.failure(Exception("Your manager has not allowed video calls."))
            }
            if (callee.role == Role.MANAGER && !caller.permissions.canCallManager) {
                return Result.failure(Exception("Permission denied to call manager."))
            }
            if (callee.role != Role.MANAGER && !caller.permissions.canCallUsers) {
                return Result.failure(Exception("Your manager has restricted calling colleagues."))
            }
        }

        if (callee.status == UserStatus.DISABLED) {
            return Result.failure(Exception("${callee.displayName} is currently disabled."))
        }

        if (callee.isDoNotDisturb) {
            // Log missed call
            recordCallLog(
                callerId = caller.userId,
                callerName = caller.displayName,
                calleeId = callee.userId,
                calleeName = callee.displayName,
                type = type,
                outcome = CallOutcome.BUSY,
                durationSec = 0
            )
            return Result.failure(Exception("${callee.displayName} is in Do Not Disturb mode."))
        }

        if (callee.presence == Presence.IN_CALL) {
            recordCallLog(
                callerId = caller.userId,
                callerName = caller.displayName,
                calleeId = callee.userId,
                calleeName = callee.displayName,
                type = type,
                outcome = CallOutcome.BUSY,
                durationSec = 0
            )
            return Result.failure(Exception("${callee.displayName} is currently on another call."))
        }

        val session = ActiveCallSession(
            callId = UUID.randomUUID().toString(),
            callerId = caller.userId,
            callerName = caller.displayName,
            callerDept = caller.department,
            calleeId = callee.userId,
            calleeName = callee.displayName,
            calleeDept = callee.department,
            callType = type,
            status = CallStatus.RINGING
        )
        _currentCall.value = session
        return Result.success(session)
    }

    fun simulateIncomingCall(caller: CompanyUser, type: CallType) {
        val curr = _currentUser.value ?: return
        val session = ActiveCallSession(
            callId = UUID.randomUUID().toString(),
            callerId = caller.userId,
            callerName = caller.displayName,
            callerDept = caller.department,
            calleeId = curr.userId,
            calleeName = curr.displayName,
            calleeDept = curr.department,
            callType = type,
            status = CallStatus.RINGING
        )
        _currentCall.value = session
    }

    fun acceptCall() {
        val current = _currentCall.value ?: return
        _currentCall.value = current.copy(
            status = CallStatus.CONNECTED,
            connectedAt = System.currentTimeMillis()
        )
    }

    fun declineCall() {
        val current = _currentCall.value ?: return
        recordCallLog(
            callerId = current.callerId,
            callerName = current.callerName,
            calleeId = current.calleeId,
            calleeName = current.calleeName,
            type = current.callType,
            outcome = CallOutcome.DECLINED,
            durationSec = 0
        )
        _currentCall.value = current.copy(status = CallStatus.DECLINED)
        _currentCall.value = null
    }

    fun endCall(durationSec: Int) {
        val current = _currentCall.value ?: return
        recordCallLog(
            callerId = current.callerId,
            callerName = current.callerName,
            calleeId = current.calleeId,
            calleeName = current.calleeName,
            type = current.callType,
            outcome = if (current.status == CallStatus.CONNECTED) CallOutcome.COMPLETED else CallOutcome.CANCELLED,
            durationSec = durationSec
        )
        _currentCall.value = current.copy(status = CallStatus.ENDED, durationSec = durationSec)
        _currentCall.value = null
    }

    fun forceEndCompanyCall(callId: String) {
        _companyActiveCalls.value = _companyActiveCalls.value.filterNot { it.callId == callId }
    }

    fun toggleMute() {
        val current = _currentCall.value ?: return
        _currentCall.value = current.copy(isMuted = !current.isMuted)
    }

    fun toggleCamera() {
        val current = _currentCall.value ?: return
        _currentCall.value = current.copy(isCameraOn = !current.isCameraOn)
    }

    fun toggleCameraLens() {
        val current = _currentCall.value ?: return
        _currentCall.value = current.copy(isFrontCamera = !current.isFrontCamera)
    }

    fun toggleSpeaker() {
        val current = _currentCall.value ?: return
        _currentCall.value = current.copy(isSpeakerOn = !current.isSpeakerOn)
    }

    fun upgradeToVideo() {
        val current = _currentCall.value ?: return
        _currentCall.value = current.copy(callType = CallType.VIDEO, isCameraOn = true)
    }

    private fun recordCallLog(
        callerId: String,
        callerName: String,
        calleeId: String,
        calleeName: String,
        type: CallType,
        outcome: CallOutcome,
        durationSec: Int
    ) {
        val currUser = _currentUser.value
        val isCaller = currUser?.userId == callerId
        val otherName = if (isCaller) calleeName else callerName
        val otherUser = _users.value.find { if (isCaller) it.userId == calleeId else it.userId == callerId }
        val log = CompanyCallLog(
            id = UUID.randomUUID().toString(),
            callerId = callerId,
            callerName = callerName,
            calleeId = calleeId,
            calleeName = calleeName,
            otherPartyName = otherName,
            otherPartyDept = otherUser?.department ?: "Team",
            direction = if (isCaller) CallDirection.OUTGOING else CallDirection.INCOMING,
            type = type,
            outcome = outcome,
            timestamp = System.currentTimeMillis(),
            durationSec = durationSec
        )
        _callLogs.value = listOf(log) + _callLogs.value
    }

    fun exportCallLogsCsv(): String {
        val sb = StringBuilder()
        sb.append("LogID,Timestamp,Caller,Callee,Type,Direction,Outcome,DurationSeconds\n")
        _callLogs.value.forEach { log ->
            sb.append("${log.id},${log.timestamp},${log.callerName},${log.calleeName},${log.type},${log.direction},${log.outcome},${log.durationSec}\n")
        }
        return sb.toString()
    }

    fun exportUsersCsv(): String {
        val sb = StringBuilder()
        sb.append("UserID,DisplayName,Phone,Department,Role,Status\n")
        _users.value.forEach { u ->
            sb.append("${u.userId},\"${u.displayName}\",${u.phone},${u.department},${u.role},${u.status}\n")
        }
        return sb.toString()
    }
}
