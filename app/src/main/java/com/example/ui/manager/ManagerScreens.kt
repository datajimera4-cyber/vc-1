package com.example.ui.manager

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CompanyRepository
import com.example.model.*
import com.example.ui.components.UserAvatar
import com.example.ui.theme.*

// -------------------------------------------------------------
// 1. MANAGER DASHBOARD SCREEN
// -------------------------------------------------------------
@Composable
fun ManagerDashboardScreen(
    users: List<CompanyUser>,
    callLogs: List<CompanyCallLog>,
    activeCalls: List<ActiveCallSession>,
    onNavigateToUsers: () -> Unit,
    onNavigateToDialer: () -> Unit,
    onNavigateToCalls: () -> Unit,
    onStartCall: (CompanyUser, CallType) -> Unit,
    onSimulateIncoming: (CompanyUser, CallType) -> Unit
) {
    val context = LocalContext.current
    val totalUsers = users.size
    val onlineNow = users.count { it.presence == Presence.ONLINE }
    val inCallNow = users.count { it.presence == Presence.IN_CALL }
    val callsToday = callLogs.size
    val missedToday = callLogs.count { it.outcome == CallOutcome.MISSED }
    val totalTalkTimeSec = callLogs.sumOf { it.durationSec }

    val formattedTalkTime = remember(totalTalkTimeSec) {
        val mins = totalTalkTimeSec / 60
        "${mins}m"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Company Overview Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CompanyNavy),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "MANAGER CONTROL CENTER",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CompanyBlue,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = CompanyRepository.settings.value.companyName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Surface(
                            color = SuccessGreen.copy(alpha = 0.2f),
                            shape = CircleShape
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(SuccessGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("System Live", color = SuccessGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Real-time monitoring of corporate communication, peer connections, and team security.",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Quick Metrics Grid
        item {
            Text(
                text = "Key Operational Metrics",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Total Directory",
                    value = totalUsers.toString(),
                    icon = Icons.Default.People,
                    color = CompanyBlue
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Online Team",
                    value = onlineNow.toString(),
                    icon = Icons.Default.CheckCircle,
                    color = SuccessGreen
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "In Call Now",
                    value = (inCallNow + activeCalls.size).toString(),
                    icon = Icons.Default.PhoneInTalk,
                    color = WarningOrange
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Calls Today",
                    value = callsToday.toString(),
                    icon = Icons.Default.Call,
                    color = CompanyBlue
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Missed Calls",
                    value = missedToday.toString(),
                    icon = Icons.Default.PhoneMissed,
                    color = DangerRed
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Talk Time",
                    value = formattedTalkTime,
                    icon = Icons.Default.Timer,
                    color = Color(0xFF9C27B0)
                )
            }
        }

        // Active Calls Right Now (Live Monitor)
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Live Active Calls (${activeCalls.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (activeCalls.isNotEmpty()) {
                    Text(
                        text = "Encrypted DTLS-SRTP",
                        fontSize = 11.sp,
                        color = SuccessGreen,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        if (activeCalls.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No employee calls currently in progress",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            items(activeCalls) { call ->
                ActiveCallMonitorCard(
                    call = call,
                    onForceEnd = {
                        CompanyRepository.forceEndCompanyCall(call.callId)
                        Toast.makeText(context, "Call terminated by manager", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // Quick Administrative Actions
        item {
            Text(
                text = "Quick Manager Actions",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickActionRow(
                    icon = Icons.Default.PersonAdd,
                    title = "Add New Employee Account",
                    subtitle = "Generate login ID, password & set call permissions",
                    onClick = onNavigateToUsers
                )
                QuickActionRow(
                    icon = Icons.Default.Dialpad,
                    title = "Open Corporate Dialer",
                    subtitle = "Call any team member or registered number",
                    onClick = onNavigateToDialer
                )
                QuickActionRow(
                    icon = Icons.Default.History,
                    title = "Export Call Logs (CSV)",
                    subtitle = "View full history and backup to Google Drive",
                    onClick = onNavigateToCalls
                )
                // Simulation button to test incoming call feature
                val testEmployee = users.firstOrNull { it.role == Role.USER }
                if (testEmployee != null) {
                    QuickActionRow(
                        icon = Icons.Default.PhoneCallback,
                        title = "Simulate Incoming Call from ${testEmployee.displayName}",
                        subtitle = "Test lockscreen/fullscreen ringing & WebRTC answer",
                        color = SuccessGreen,
                        onClick = {
                            onSimulateIncoming(testEmployee, CallType.VIDEO)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ActiveCallMonitorCard(
    call: ActiveCallSession,
    onForceEnd: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(WarningOrange.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (call.callType == CallType.VIDEO) Icons.Default.Videocam else Icons.Default.Call,
                    contentDescription = null,
                    tint = WarningOrange
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${call.callerName} ➔ ${call.calleeName}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${call.callerDept} to ${call.calleeDept} • ${call.durationSec}s",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onForceEnd,
                colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Text("End Call", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun QuickActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color = CompanyBlue,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
