package com.example.ui.user

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CompanyRepository
import com.example.model.*
import com.example.ui.components.UserAvatar
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

// -------------------------------------------------------------
// 1. USER HOME / CONTACTS SCREEN
// -------------------------------------------------------------
@Composable
fun UserHomeScreen(
    currentUser: CompanyUser,
    allUsers: List<CompanyUser>,
    onStartCall: (CompanyUser, CallType) -> Unit,
    onSimulateIncoming: (CompanyUser, CallType) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    val managerUser = remember(allUsers) { allUsers.find { it.role == Role.MANAGER } }
    val colleagues = remember(allUsers, currentUser, searchQuery) {
        allUsers.filter {
            it.userId != currentUser.userId &&
            it.role != Role.MANAGER &&
            (it.displayName.contains(searchQuery, ignoreCase = true) ||
             it.department.contains(searchQuery, ignoreCase = true) ||
             it.userId.contains(searchQuery, ignoreCase = true))
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Prominent "Call Manager" Card (Section 8.1 & 8.2 requirement)
        if (managerUser != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CompanyNavy),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            UserAvatar(
                                name = managerUser.displayName,
                                avatarColorHex = managerUser.avatarColorHex,
                                presence = managerUser.presence,
                                size = 52.dp
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "COMPANY DIRECTOR",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CompanyBlue,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = managerUser.displayName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Direct Line • Priority Support",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { onStartCall(managerUser, CallType.AUDIO) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("call_manager_audio_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Voice Call")
                            }

                            Button(
                                onClick = {
                                    if (currentUser.permissions.canVideo) {
                                        onStartCall(managerUser, CallType.VIDEO)
                                    } else {
                                        Toast.makeText(context, "Your manager has not allowed video calls", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("call_manager_video_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = CompanyBlue),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Video Call")
                            }
                        }
                    }
                }
            }
        }

        // Test Call Simulator Bar
        item {
            Surface(
                color = SuccessGreen.copy(alpha = 0.12f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val caller = managerUser ?: allUsers.first()
                        onSimulateIncoming(caller, CallType.VIDEO)
                    }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PhoneCallback, contentDescription = null, tint = SuccessGreen)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Simulate Manager Incoming Video Call", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Tap to test full-screen ringing UI & camera answer", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = SuccessGreen)
                }
            }
        }

        // Colleagues Directory
        item {
            Text(
                text = "Authorized Team Colleagues",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search coworkers or department...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CompanyBlue) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("user_contacts_search"),
                shape = RoundedCornerShape(12.dp)
            )
        }

        if (!currentUser.permissions.canCallUsers) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Direct peer calling is restricted for your account by policy. You can call your manager anytime.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else if (colleagues.isEmpty()) {
            item {
                Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("No colleagues found", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(colleagues, key = { it.userId }) { colleague ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserAvatar(
                            name = colleague.displayName,
                            avatarColorHex = colleague.avatarColorHex,
                            presence = colleague.presence,
                            size = 46.dp
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = colleague.displayName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${colleague.department} • @${colleague.userId}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = { onStartCall(colleague, CallType.AUDIO) },
                            enabled = colleague.status == UserStatus.ACTIVE
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Call", tint = SuccessGreen)
                        }

                        IconButton(
                            onClick = {
                                if (currentUser.permissions.canVideo) {
                                    onStartCall(colleague, CallType.VIDEO)
                                } else {
                                    Toast.makeText(context, "Your manager has not allowed video calls", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = colleague.status == UserStatus.ACTIVE
                        ) {
                            Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = CompanyBlue)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. USER RECENTS / CALL LOGS SCREEN
// -------------------------------------------------------------
@Composable
fun UserRecentsScreen(
    currentUser: CompanyUser,
    callLogs: List<CompanyCallLog>,
    allUsers: List<CompanyUser>,
    onCallAgain: (CompanyUser, CallType) -> Unit
) {
    val userLogs = remember(callLogs, currentUser) {
        callLogs.filter { it.callerId == currentUser.userId || it.calleeId == currentUser.userId }
    }

    val dateFormatter = remember { SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Recent Calls",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Tap any call to connect instantly",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (userLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.PhoneMissed,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No recent calls yet",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Your voice and video call records will appear here",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(userLogs) { log ->
                    val isCaller = log.callerId == currentUser.userId
                    val otherUserId = if (isCaller) log.calleeId else log.callerId
                    val otherUser = allUsers.find { it.userId == otherUserId }

                    val isMissed = log.outcome == CallOutcome.MISSED

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (otherUser != null) {
                                    onCallAgain(otherUser, log.type)
                                }
                            },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isMissed) Icons.Default.CallMissed else if (isCaller) Icons.Default.CallMade else Icons.Default.CallReceived,
                                contentDescription = null,
                                tint = if (isMissed) DangerRed else SuccessGreen,
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = log.otherPartyName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isMissed) DangerRed else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${log.type.name} • ${dateFormatter.format(Date(log.timestamp))}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (log.durationSec > 0) {
                                val mins = log.durationSec / 60
                                val secs = log.durationSec % 60
                                Text(
                                    text = String.format("%dm %02ds", mins, secs),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            Icon(
                                imageVector = if (log.type == CallType.VIDEO) Icons.Default.Videocam else Icons.Default.Call,
                                contentDescription = null,
                                tint = CompanyBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. USER PROFILE & SETTINGS SCREEN
// -------------------------------------------------------------
@Composable
fun UserProfileScreen(
    currentUser: CompanyUser,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var selectedRingtone by remember { mutableStateOf("Enterprise Chime (Default)") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CompanyNavy),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    UserAvatar(
                        name = currentUser.displayName,
                        avatarColorHex = currentUser.avatarColorHex,
                        presence = currentUser.presence,
                        size = 72.dp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = currentUser.displayName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${currentUser.department} • @${currentUser.userId}",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
            }
        }

        // Do Not Disturb Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Do Not Disturb", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Silence all incoming calls. Callers will see you as unavailable.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = currentUser.isDoNotDisturb,
                        onCheckedChange = {
                            CompanyRepository.toggleDoNotDisturb()
                            val msg = if (!currentUser.isDoNotDisturb) "DND Enabled" else "DND Disabled"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        // Account Details Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Corporate Identity", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Divider()
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Phone Number", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(currentUser.phone, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Device Security ID", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(currentUser.deviceId, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Encryption", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("End-to-End DTLS-SRTP", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = SuccessGreen)
                    }
                }
            }
        }

        // Logout
        item {
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out")
            }
        }
    }
}
