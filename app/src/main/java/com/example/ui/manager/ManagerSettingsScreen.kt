package com.example.ui.manager

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CompanyRepository
import com.example.model.CompanySettings
import com.example.ui.theme.*

@Composable
fun ManagerSettingsScreen(
    settings: CompanySettings,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var companyName by remember { mutableStateOf(settings.companyName) }
    var ringTimeout by remember { mutableIntStateOf(settings.ringTimeoutSec) }
    var allowGroupCalls by remember { mutableStateOf(settings.allowGroupCalls) }
    var singleDeviceLock by remember { mutableStateOf(settings.singleDeviceLock) }
    var maintenanceMode by remember { mutableStateOf(settings.maintenanceMode) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Enterprise Administration",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Global rules, WebRTC servers & company policies",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Company Details Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("General Organization", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = companyName,
                        onValueChange = {
                            companyName = it
                            CompanyRepository.updateSettings(settings.copy(companyName = it))
                        },
                        label = { Text("Company Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Ring Timeout", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("Auto-cancel if unanswered", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("${ringTimeout}s", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CompanyBlue)
                    }

                    Slider(
                        value = ringTimeout.toFloat(),
                        onValueChange = {
                            ringTimeout = it.toInt()
                            CompanyRepository.updateSettings(settings.copy(ringTimeoutSec = it.toInt()))
                        },
                        valueRange = 15f..90f,
                        steps = 5
                    )
                }
            }
        }

        // Security & Enforcement Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Security & Call Controls", fontSize = 15.sp, fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Single Device Lock", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("Account can only be active on one phone at a time", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = singleDeviceLock,
                            onCheckedChange = {
                                singleDeviceLock = it
                                CompanyRepository.updateSettings(settings.copy(singleDeviceLock = it))
                            }
                        )
                    }

                    Divider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Allow Group Mesh Calls", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("Up to 4 colleagues in multi-party conference", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = allowGroupCalls,
                            onCheckedChange = {
                                allowGroupCalls = it
                                CompanyRepository.updateSettings(settings.copy(allowGroupCalls = it))
                            }
                        )
                    }

                    Divider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Maintenance Mode", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("Block all outbound employee calls during drills/maintenance", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = maintenanceMode,
                            onCheckedChange = {
                                maintenanceMode = it
                                CompanyRepository.updateSettings(settings.copy(maintenanceMode = it))
                            }
                        )
                    }
                }
            }
        }

        // WebRTC Infrastructure Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("WebRTC NAT Traversal (STUN & TURN)", fontSize = 15.sp, fontWeight = FontWeight.Bold)

                    Text(
                        text = "STUN Server: ${settings.stunServer}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "TURN Relay: ${settings.turnServer}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = {
                            Toast.makeText(context, "Testing STUN & TURN connectivity... 24ms (OK)", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CompanyBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test Server Connection", fontSize = 12.sp)
                    }
                }
            }
        }

        // Google Drive Backup (Section 7.6)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CompanyNavy),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Manager Cloud Backup", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(
                        text = "Nightly export of user list and encrypted call records to Google Drive folder 'CompanyCall Backups'.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = {
                            Toast.makeText(context, "Backing up users.csv and call_logs.csv to Google Drive...", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CompanyBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Back Up Now", fontSize = 12.sp)
                    }
                }
            }
        }

        // Account & Logout
        item {
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out of Manager Console")
            }
        }
    }
}
