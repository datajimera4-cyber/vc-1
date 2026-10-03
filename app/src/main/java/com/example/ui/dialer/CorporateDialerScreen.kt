package com.example.ui.dialer

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.UserAvatar
import com.example.ui.theme.*

@Composable
fun CorporateDialerScreen(
    currentUser: CompanyUser,
    allUsers: List<CompanyUser>,
    onStartCall: (CompanyUser, CallType) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Company Directory", "Keypad")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = CompanyBlue
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 14.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        if (selectedTab == 0) {
            DialerContactsList(
                currentUser = currentUser,
                users = allUsers.filter { it.userId != currentUser.userId },
                onStartCall = onStartCall
            )
        } else {
            DialerKeypadView(
                currentUser = currentUser,
                allUsers = allUsers,
                onStartCall = onStartCall
            )
        }
    }
}

@Composable
private fun DialerContactsList(
    currentUser: CompanyUser,
    users: List<CompanyUser>,
    onStartCall: (CompanyUser, CallType) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(users, searchQuery) {
        users.filter {
            it.displayName.contains(searchQuery, ignoreCase = true) ||
            it.userId.contains(searchQuery, ignoreCase = true) ||
            it.department.contains(searchQuery, ignoreCase = true) ||
            it.phone.contains(searchQuery)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by name, ID or extension...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CompanyBlue) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("dialer_contacts_search"),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filtered) { user ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserAvatar(
                            name = user.displayName,
                            avatarColorHex = user.avatarColorHex,
                            presence = user.presence,
                            size = 44.dp
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = user.displayName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${user.department} • ${user.phone}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Call Buttons
                        IconButton(
                            onClick = { onStartCall(user, CallType.AUDIO) },
                            enabled = user.status == UserStatus.ACTIVE
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Audio Call", tint = SuccessGreen)
                        }

                        IconButton(
                            onClick = { onStartCall(user, CallType.VIDEO) },
                            enabled = user.status == UserStatus.ACTIVE && (currentUser.role == Role.MANAGER || currentUser.permissions.canVideo)
                        ) {
                            Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = CompanyBlue)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DialerKeypadView(
    currentUser: CompanyUser,
    allUsers: List<CompanyUser>,
    onStartCall: (CompanyUser, CallType) -> Unit
) {
    val context = LocalContext.current
    var dialedNumber by remember { mutableStateOf("") }

    // Live matching against company directory
    val matchedUser = remember(dialedNumber, allUsers) {
        val clean = dialedNumber.replace(" ", "").replace("-", "").replace("+", "")
        if (clean.isEmpty()) null
        else {
            allUsers.find {
                val p = it.phone.replace(" ", "").replace("-", "").replace("+", "")
                p.endsWith(clean) || it.userId.replace(".", "").contains(clean)
            }
        }
    }

    val canDial = currentUser.role == Role.MANAGER || currentUser.permissions.canDialNewNumber

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        if (!canDial) {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                color = WarningOrange.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = WarningOrange, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Dialer restricted by manager. Use directory tab.",
                        fontSize = 12.sp,
                        color = WarningOrange,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Display Dialed Number & Live Match
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                text = dialedNumber.ifEmpty { "Enter Number" },
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = if (dialedNumber.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (dialedNumber.isNotEmpty()) {
                if (matchedUser != null) {
                    Surface(
                        color = SuccessGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(SuccessGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${matchedUser.displayName} (${matchedUser.department})",
                                color = SuccessGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Surface(
                        color = DangerRed.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Not registered in company",
                            color = DangerRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Keypad Grid
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            val keyRows = listOf(
                listOf("1" to "", "2" to "ABC", "3" to "DEF"),
                listOf("4" to "GHI", "5" to "JKL", "6" to "MNO"),
                listOf("7" to "PQRS", "8" to "TUV", "9" to "WXYZ"),
                listOf("*" to "", "0" to "+", "#" to "")
            )

            keyRows.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    row.forEach { (num, sub) ->
                        KeypadButton(
                            modifier = Modifier.weight(1f),
                            number = num,
                            sub = sub,
                            onClick = {
                                if (canDial) {
                                    dialedNumber += num
                                }
                            }
                        )
                    }
                }
            }
        }

        // Action row: Audio Call, Video Call, Backspace
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 96.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Video Call
            FilledIconButton(
                onClick = {
                    if (matchedUser != null) {
                        onStartCall(matchedUser, CallType.VIDEO)
                    } else {
                        Toast.makeText(context, "Only registered company members can be called", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = canDial && matchedUser != null,
                modifier = Modifier.size(56.dp),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = CompanyBlue)
            ) {
                Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = Color.White)
            }

            // Audio Call
            FilledIconButton(
                onClick = {
                    if (matchedUser != null) {
                        onStartCall(matchedUser, CallType.AUDIO)
                    } else {
                        Toast.makeText(context, "Only registered company members can be called", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = canDial && matchedUser != null,
                modifier = Modifier.size(64.dp),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = SuccessGreen)
            ) {
                Icon(Icons.Default.Call, contentDescription = "Audio Call", tint = Color.White, modifier = Modifier.size(28.dp))
            }

            // Backspace
            IconButton(
                onClick = {
                    if (dialedNumber.isNotEmpty()) {
                        dialedNumber = dialedNumber.dropLast(1)
                    }
                },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.Backspace, contentDescription = "Backspace", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun KeypadButton(
    modifier: Modifier = Modifier,
    number: String,
    sub: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.aspectRatio(1.25f),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = number,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (sub.isNotEmpty()) {
                Text(
                    text = sub,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
