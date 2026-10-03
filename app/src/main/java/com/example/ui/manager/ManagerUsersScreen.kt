package com.example.ui.manager

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CompanyRepository
import com.example.model.*
import com.example.ui.components.UserAvatar
import com.example.ui.theme.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerUsersScreen(
    users: List<CompanyUser>,
    onStartCall: (CompanyUser, CallType) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedUserForDetail by remember { mutableStateOf<CompanyUser?>(null) }

    val filterOptions = listOf("All", "Online", "In Call", "Offline", "Disabled", "Engineering", "Design", "Product")

    val filteredUsers = remember(users, searchQuery, selectedFilter) {
        users.filter { user ->
            val matchesSearch = user.displayName.contains(searchQuery, ignoreCase = true) ||
                    user.userId.contains(searchQuery, ignoreCase = true) ||
                    user.department.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                "All" -> true
                "Online" -> user.presence == Presence.ONLINE
                "In Call" -> user.presence == Presence.IN_CALL
                "Offline" -> user.presence == Presence.OFFLINE
                "Disabled" -> user.status == UserStatus.DISABLED
                else -> user.department.equals(selectedFilter, ignoreCase = true)
            }

            matchesSearch && matchesFilter
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Company Directory",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${users.size} Registered Employees",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(
                            onClick = {
                                val csv = CompanyRepository.exportUsersCsv()
                                Toast.makeText(context, "Exported ${users.size} employees (CSV ready)", Toast.LENGTH_SHORT).show()
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("CSV", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CompanyBlue),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("add_user_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add User", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_users_field"),
                    placeholder = { Text("Search name, ID, or department...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CompanyBlue) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CompanyBlue,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(filterOptions) { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter, fontSize = 12.sp) },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CompanyBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (filteredUsers.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No team members found",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Try modifying your search or filter",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredUsers, key = { it.userId }) { user ->
                    UserRowCard(
                        user = user,
                        onClick = { selectedUserForDetail = user },
                        onAudioCall = { onStartCall(user, CallType.AUDIO) },
                        onVideoCall = { onStartCall(user, CallType.VIDEO) }
                    )
                }
            }
        }
    }

    // Add User Dialog
    if (showAddDialog) {
        AddUserDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { id, name, phone, dept, perms ->
                val success = CompanyRepository.addUser(id, name, phone, dept, perms)
                if (success) {
                    Toast.makeText(context, "Employee $name created successfully!", Toast.LENGTH_SHORT).show()
                    showAddDialog = false
                } else {
                    Toast.makeText(context, "User ID '$id' is already taken.", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // User Detail Bottom Sheet
    selectedUserForDetail?.let { user ->
        UserDetailSheet(
            user = user,
            onDismiss = { selectedUserForDetail = null },
            onAudioCall = {
                selectedUserForDetail = null
                onStartCall(user, CallType.AUDIO)
            },
            onVideoCall = {
                selectedUserForDetail = null
                onStartCall(user, CallType.VIDEO)
            },
            onToggleStatus = {
                CompanyRepository.toggleUserStatus(user.userId)
                selectedUserForDetail = null
                Toast.makeText(context, "Account status updated", Toast.LENGTH_SHORT).show()
            },
            onDelete = {
                CompanyRepository.deleteUser(user.userId)
                selectedUserForDetail = null
                Toast.makeText(context, "User removed from company", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun UserRowCard(
    user: CompanyUser,
    onClick: () -> Unit,
    onAudioCall: () -> Unit,
    onVideoCall: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(
                name = user.displayName,
                avatarColorHex = user.avatarColorHex,
                presence = user.presence,
                size = 48.dp
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.displayName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (user.role == Role.MANAGER) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = CompanyBlue.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Admin",
                                color = CompanyBlue,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (user.status == UserStatus.DISABLED) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = DangerRed.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Disabled",
                                color = DangerRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${user.department} • @${user.userId}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Quick Call Actions
            if (user.status == UserStatus.ACTIVE) {
                IconButton(
                    onClick = onAudioCall,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Audio Call",
                        tint = SuccessGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onVideoCall,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Video Call",
                        tint = CompanyBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddUserDialog(
    onDismiss: () -> Unit,
    onAdd: (id: String, name: String, phone: String, dept: String, perms: UserPermissions) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var fullName by remember { mutableStateOf("") }
    var userId by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var generatedPassword by remember { mutableStateOf("TechPass@${(1000..9999).random()}") }

    var canCallUsers by remember { mutableStateOf(true) }
    var canCallManager by remember { mutableStateOf(true) }
    var canDialNewNumber by remember { mutableStateOf(true) }
    var canVideo by remember { mutableStateOf(true) }
    var canGroupCall by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add New Company Employee",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = {
                        fullName = it
                        if (userId.isEmpty() && it.contains(" ")) {
                            userId = it.lowercase().replace(" ", ".").filter { c -> c.isLetterOrDigit() || c == '.' }
                        }
                    },
                    label = { Text("Full Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = userId,
                    onValueChange = { userId = it.lowercase().replace(" ", "") },
                    label = { Text("User ID (e.g. rahul.dev) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number (with code)") },
                    placeholder = { Text("+91 98765 43210") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = { Text("Department (e.g. Engineering)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Password Generator Card
                Surface(
                    color = Slate100,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Temporary Password", fontSize = 11.sp, color = Slate700)
                            Text(generatedPassword, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CompanyNavy)
                        }
                        IconButton(onClick = {
                            clipboardManager.setText(AnnotatedString(generatedPassword))
                            Toast.makeText(context, "Password copied!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Password", tint = CompanyBlue)
                        }
                    }
                }

                Text("Call Permissions", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate800)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = canVideo, onCheckedChange = { canVideo = it })
                    Text("Allow Video Calling", fontSize = 13.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = canDialNewNumber, onCheckedChange = { canDialNewNumber = it })
                    Text("Allow Dialing Keypad Numbers", fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fullName.isNotBlank() && userId.isNotBlank()) {
                        val perms = UserPermissions(
                            canCallUsers = canCallUsers,
                            canCallManager = canCallManager,
                            canDialNewNumber = canDialNewNumber,
                            canVideo = canVideo,
                            canGroupCall = canGroupCall
                        )
                        onAdd(userId, fullName, phone, department, perms)
                    }
                },
                enabled = fullName.isNotBlank() && userId.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CompanyBlue)
            ) {
                Text("Create Account")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailSheet(
    user: CompanyUser,
    onDismiss: () -> Unit,
    onAudioCall: () -> Unit,
    onVideoCall: () -> Unit,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            UserAvatar(
                name = user.displayName,
                avatarColorHex = user.avatarColorHex,
                presence = user.presence,
                size = 72.dp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = user.displayName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "${user.department} • @${user.userId}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Action Call Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onAudioCall,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Audio Call")
                }

                Button(
                    onClick = onVideoCall,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = CompanyBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Video Call")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Divider()

            Spacer(modifier = Modifier.height(16.dp))

            // Info rows
            InfoDetailRow(label = "Phone", value = user.phone.ifEmpty { "None" })
            InfoDetailRow(label = "Account Status", value = user.status.name)
            InfoDetailRow(label = "Device ID", value = user.deviceId)
            InfoDetailRow(label = "Video Permission", value = if (user.permissions.canVideo) "Allowed" else "Restricted")
            InfoDetailRow(label = "Dialer Permission", value = if (user.permissions.canDialNewNumber) "Allowed" else "Restricted")

            Spacer(modifier = Modifier.height(24.dp))

            // Administrative toggles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onToggleStatus,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (user.status == UserStatus.ACTIVE) DangerRed else SuccessGreen
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (user.status == UserStatus.ACTIVE) "Disable User" else "Enable User")
                }

                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
fun InfoDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}
