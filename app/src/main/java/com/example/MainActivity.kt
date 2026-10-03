package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CompanyRepository
import com.example.model.*
import com.example.ui.auth.LoginScreen
import com.example.ui.call.ActiveCallScreen
import com.example.ui.call.IncomingCallScreen
import com.example.ui.components.PermissionsOnboardingDialog
import com.example.ui.components.UserAvatar
import com.example.ui.dialer.CorporateDialerScreen
import com.example.ui.manager.*
import com.example.ui.theme.*
import com.example.ui.user.*

enum class ManagerTab(val title: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    USERS("Users", Icons.Default.Group),
    CALLS("Call Logs", Icons.Default.History),
    DIALER("Dialer", Icons.Default.Dialpad),
    SETTINGS("Settings", Icons.Default.Settings)
}

enum class UserTab(val title: String, val icon: ImageVector) {
    HOME("Contacts", Icons.Default.Contacts),
    DIALER("Dialer", Icons.Default.Dialpad),
    RECENTS("Recents", Icons.Default.Schedule),
    PROFILE("Profile", Icons.Default.Person)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CompanyCallTheme {
                CompanyCallApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyCallApp() {
    val context = LocalContext.current

    val currentUser by CompanyRepository.currentUser.collectAsStateWithLifecycle()
    val users by CompanyRepository.users.collectAsStateWithLifecycle()
    val callLogs by CompanyRepository.callLogs.collectAsStateWithLifecycle()
    val currentCall by CompanyRepository.currentCall.collectAsStateWithLifecycle()
    val activeCalls by CompanyRepository.companyActiveCalls.collectAsStateWithLifecycle()
    val settings by CompanyRepository.settings.collectAsStateWithLifecycle()

    var managerTab by remember { mutableStateOf(ManagerTab.DASHBOARD) }
    var userTab by remember { mutableStateOf(UserTab.HOME) }
    var showUserSwitcherMenu by remember { mutableStateOf(false) }
    var showPermissionsDialog by remember { mutableStateOf(false) }

    // First time onboarding check
    LaunchedEffect(Unit) {
        showPermissionsDialog = true
    }

    if (showPermissionsDialog) {
        PermissionsOnboardingDialog(onDismiss = { showPermissionsDialog = false })
    }

    // Call Screen Overlay
    currentCall?.let { session ->
        val isIncoming = session.calleeId == currentUser?.userId && session.status == CallStatus.RINGING
        if (isIncoming) {
            IncomingCallScreen(
                callSession = session,
                onAccept = { CompanyRepository.acceptCall() },
                onDecline = { CompanyRepository.declineCall() }
            )
        } else {
            ActiveCallScreen(
                session = session,
                currentUserId = currentUser?.userId ?: "",
                onEndCall = { duration ->
                    CompanyRepository.endCall(duration)
                },
                onToggleMute = { CompanyRepository.toggleMute() },
                onToggleCamera = { CompanyRepository.toggleCamera() },
                onToggleCameraLens = { CompanyRepository.toggleCameraLens() },
                onToggleSpeaker = { CompanyRepository.toggleSpeaker() },
                onUpgradeToVideo = { CompanyRepository.upgradeToVideo() }
            )
        }
        return
    }

    // If not logged in, show Login Screen
    val user = currentUser
    if (user == null) {
        LoginScreen(
            users = users,
            onLoginSuccess = { loggedInUser ->
                Toast.makeText(context, "Signed in as ${loggedInUser.displayName}", Toast.LENGTH_SHORT).show()
            }
        )
        return
    }

    // Back handling for navigation tabs
    if (user.role == Role.MANAGER && managerTab != ManagerTab.DASHBOARD) {
        BackHandler { managerTab = ManagerTab.DASHBOARD }
    } else if (user.role == Role.USER && userTab != UserTab.HOME) {
        BackHandler { userTab = UserTab.HOME }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = CompanyBlue,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Videocam,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "CompanyCall",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (user.role == Role.MANAGER) "Admin Suite" else "${user.department} Space",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // DND status indicator
                    if (user.isDoNotDisturb) {
                        Surface(
                            color = DangerRed.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = "DND",
                                color = DangerRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // User Switcher Pill / Profile Button
                    Box {
                        Surface(
                            onClick = { showUserSwitcherMenu = true },
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("switch_account_pill")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                UserAvatar(
                                    name = user.displayName,
                                    avatarColorHex = user.avatarColorHex,
                                    presence = user.presence,
                                    size = 28.dp,
                                    showPresenceBorder = false
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (user.role == Role.MANAGER) "Manager" else user.displayName.split(" ").first(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Switch Account",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showUserSwitcherMenu,
                            onDismissRequest = { showUserSwitcherMenu = false }
                        ) {
                            Text(
                                text = "Switch Account / Test Role",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CompanyBlue,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                            users.forEach { u ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            UserAvatar(name = u.displayName, avatarColorHex = u.avatarColorHex, size = 26.dp)
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(u.displayName, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                                Text(if (u.role == Role.MANAGER) "Manager (Admin)" else u.department, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    },
                                    onClick = {
                                        CompanyRepository.switchUser(u.userId)
                                        showUserSwitcherMenu = false
                                        Toast.makeText(context, "Switched to ${u.displayName}", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                            Divider()
                            DropdownMenuItem(
                                text = { Text("Log Out", color = DangerRed) },
                                leadingIcon = { Icon(Icons.Default.Logout, contentDescription = null, tint = DangerRed) },
                                onClick = {
                                    showUserSwitcherMenu = false
                                    CompanyRepository.logout()
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                if (user.role == Role.MANAGER) {
                    ManagerTab.values().forEach { tab ->
                        NavigationBarItem(
                            selected = managerTab == tab,
                            onClick = { managerTab = tab },
                            icon = { Icon(tab.icon, contentDescription = tab.title) },
                            label = { Text(tab.title, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = CompanyBlue,
                                indicatorColor = CompanyBlue
                            )
                        )
                    }
                } else {
                    UserTab.values().forEach { tab ->
                        NavigationBarItem(
                            selected = userTab == tab,
                            onClick = { userTab = tab },
                            icon = { Icon(tab.icon, contentDescription = tab.title) },
                            label = { Text(tab.title, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = CompanyBlue,
                                indicatorColor = CompanyBlue
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (user.role == Role.MANAGER) {
                // Manager App Flow
                when (managerTab) {
                    ManagerTab.DASHBOARD -> ManagerDashboardScreen(
                        users = users,
                        callLogs = callLogs,
                        activeCalls = activeCalls,
                        onNavigateToUsers = { managerTab = ManagerTab.USERS },
                        onNavigateToDialer = { managerTab = ManagerTab.DIALER },
                        onNavigateToCalls = { managerTab = ManagerTab.CALLS },
                        onStartCall = { target, type ->
                            val res = CompanyRepository.initiateCall(target, type)
                            res.onFailure { Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show() }
                        },
                        onSimulateIncoming = { caller, type ->
                            CompanyRepository.simulateIncomingCall(caller, type)
                        }
                    )
                    ManagerTab.USERS -> ManagerUsersScreen(
                        users = users,
                        onStartCall = { target, type ->
                            val res = CompanyRepository.initiateCall(target, type)
                            res.onFailure { Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show() }
                        }
                    )
                    ManagerTab.CALLS -> ManagerCallHistoryScreen(callLogs = callLogs)
                    ManagerTab.DIALER -> CorporateDialerScreen(
                        currentUser = user,
                        allUsers = users,
                        onStartCall = { target, type ->
                            val res = CompanyRepository.initiateCall(target, type)
                            res.onFailure { Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show() }
                        }
                    )
                    ManagerTab.SETTINGS -> ManagerSettingsScreen(
                        settings = settings,
                        onLogout = { CompanyRepository.logout() }
                    )
                }
            } else {
                // User / Employee App Flow
                when (userTab) {
                    UserTab.HOME -> UserHomeScreen(
                        currentUser = user,
                        allUsers = users,
                        onStartCall = { target, type ->
                            val res = CompanyRepository.initiateCall(target, type)
                            res.onFailure { Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show() }
                        },
                        onSimulateIncoming = { caller, type ->
                            CompanyRepository.simulateIncomingCall(caller, type)
                        }
                    )
                    UserTab.DIALER -> CorporateDialerScreen(
                        currentUser = user,
                        allUsers = users,
                        onStartCall = { target, type ->
                            val res = CompanyRepository.initiateCall(target, type)
                            res.onFailure { Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show() }
                        }
                    )
                    UserTab.RECENTS -> UserRecentsScreen(
                        currentUser = user,
                        callLogs = callLogs,
                        allUsers = users,
                        onCallAgain = { target, type ->
                            val res = CompanyRepository.initiateCall(target, type)
                            res.onFailure { Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show() }
                        }
                    )
                    UserTab.PROFILE -> UserProfileScreen(
                        currentUser = user,
                        onLogout = { CompanyRepository.logout() }
                    )
                }
            }
        }
    }
}
