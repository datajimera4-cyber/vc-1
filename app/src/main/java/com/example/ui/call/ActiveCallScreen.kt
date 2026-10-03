package com.example.ui.call

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActiveCallSession
import com.example.model.CallStatus
import com.example.model.CallType
import com.example.ui.components.CameraPreviewView
import com.example.ui.components.UserAvatar
import com.example.ui.theme.CompanyNavy
import com.example.ui.theme.CompanyNavyDark
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SuccessGreen
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun ActiveCallScreen(
    session: ActiveCallSession,
    currentUserId: String,
    onEndCall: (Int) -> Unit,
    onToggleMute: () -> Unit,
    onToggleCamera: () -> Unit,
    onToggleCameraLens: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onUpgradeToVideo: () -> Unit
) {
    val isCaller = session.callerId == currentUserId
    val otherPersonName = if (isCaller) session.calleeName else session.callerName
    val otherPersonDept = if (isCaller) session.calleeDept else session.callerDept

    var callDurationSec by remember { mutableIntStateOf(session.durationSec) }
    var isControlsVisible by remember { mutableStateOf(true) }
    var isLocalPipSwapped by remember { mutableStateOf(false) }

    // Floating PiP offset
    var pipOffsetX by remember { mutableFloatStateOf(0f) }
    var pipOffsetY by remember { mutableFloatStateOf(0f) }

    // Live timer
    LaunchedEffect(session.status) {
        if (session.status == CallStatus.CONNECTED) {
            while (true) {
                delay(1000)
                callDurationSec++
            }
        }
    }

    // Auto connect outbound call after realistic ring delay
    var outboundStatusText by remember { mutableStateOf("Calling...") }
    LaunchedEffect(session.status) {
        if (session.status == CallStatus.RINGING && isCaller) {
            delay(1200)
            outboundStatusText = "Ringing..."
            delay(2400)
            outboundStatusText = "Connecting..."
            delay(1200)
            com.example.data.CompanyRepository.acceptCall()
        }
    }

    val formattedDuration = remember(callDurationSec) {
        val minutes = callDurationSec / 60
        val seconds = callDurationSec % 60
        String.format("%02d:%02d", minutes, seconds)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CompanyNavyDark)
            .clickable { isControlsVisible = !isControlsVisible }
    ) {
        if (session.status != CallStatus.CONNECTED) {
            // Ringing / Dialing screen
            OutboundRingingView(
                personName = otherPersonName,
                department = otherPersonDept,
                statusText = outboundStatusText,
                callType = session.callType,
                onEndCall = { onEndCall(0) }
            )
        } else {
            // Connected State
            if (session.callType == CallType.VIDEO) {
                // Video Stream Layout
                Box(modifier = Modifier.fillMaxSize()) {
                    // Remote Feed (simulated corporate camera stream with animated pulse)
                    RemoteVideoFeedView(
                        isSwapped = isLocalPipSwapped,
                        remoteName = otherPersonName,
                        isCameraOn = session.isCameraOn,
                        isFrontCamera = session.isFrontCamera
                    )

                    // Local Camera Preview (PiP Window with real CameraX!)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 70.dp, end = 16.dp)
                            .offset { IntOffset(pipOffsetX.roundToInt(), pipOffsetY.roundToInt()) }
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    pipOffsetX += dragAmount.x
                                    pipOffsetY += dragAmount.y
                                }
                            }
                            .size(width = 110.dp, height = 150.dp)
                            .shadow(8.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .border(2.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                            .clickable { isLocalPipSwapped = !isLocalPipSwapped }
                    ) {
                        if (isLocalPipSwapped) {
                            // Showing remote in small window
                            RemoteVideoThumbnail(name = otherPersonName)
                        } else {
                            // Real CameraX preview!
                            CameraPreviewView(
                                isFrontCamera = session.isFrontCamera,
                                isCameraEnabled = session.isCameraOn
                            )
                        }

                        // Mini badge indicating switch
                        Surface(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = CircleShape,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapCalls,
                                contentDescription = "Swap Feeds",
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(14.dp)
                            )
                        }
                    }
                }
            } else {
                // Audio Call Layout
                ConnectedAudioCallView(
                    personName = otherPersonName,
                    department = otherPersonDept,
                    duration = formattedDuration
                )
            }

            // Top Info Bar (Animated overlay)
            AnimatedVisibility(
                visible = isControlsVisible,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                TopCallBar(
                    name = otherPersonName,
                    duration = formattedDuration,
                    callType = session.callType,
                    networkQuality = session.networkQuality
                )
            }

            // Bottom Controls Bar (Animated overlay)
            AnimatedVisibility(
                visible = isControlsVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                BottomControlsBar(
                    session = session,
                    onToggleMute = onToggleMute,
                    onToggleCamera = onToggleCamera,
                    onToggleCameraLens = onToggleCameraLens,
                    onToggleSpeaker = onToggleSpeaker,
                    onUpgradeToVideo = onUpgradeToVideo,
                    onEndCall = { onEndCall(callDurationSec) }
                )
            }
        }
    }
}

@Composable
private fun OutboundRingingView(
    personName: String,
    department: String,
    statusText: String,
    callType: CallType,
    onEndCall: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulseRing")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Surface(
            color = Color.White.copy(alpha = 0.1f),
            shape = CircleShape
        ) {
            Text(
                text = if (callType == CallType.VIDEO) "SECURE VIDEO CALL" else "COMPANY AUDIO CALL",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(60.dp))

        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(pulse)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape)
            )
            UserAvatar(
                name = personName,
                avatarColorHex = "#1F6FEB",
                size = 110.dp,
                showPresenceBorder = false
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = personName,
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "$department • CompanyCall",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = statusText,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        FilledIconButton(
            onClick = onEndCall,
            modifier = Modifier
                .size(72.dp)
                .testTag("cancel_call_button"),
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = DangerRed)
        ) {
            Icon(
                imageVector = Icons.Default.CallEnd,
                contentDescription = "Cancel Call",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Cancel",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(36.dp))
    }
}

@Composable
private fun RemoteVideoFeedView(
    isSwapped: Boolean,
    remoteName: String,
    isCameraOn: Boolean,
    isFrontCamera: Boolean
) {
    if (isSwapped) {
        // Local camera is full screen
        CameraPreviewView(isFrontCamera = isFrontCamera, isCameraEnabled = isCameraOn)
    } else {
        // High fidelity corporate simulated peer video feed
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1E293B),
                            CompanyNavy,
                            CompanyNavyDark
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Simulated animated speaking waveform
            val infiniteTransition = rememberInfiniteTransition(label = "audioWave")
            val waveHeight by infiniteTransition.animateFloat(
                initialValue = 0.7f,
                targetValue = 1.15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "wave"
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .scale(waveHeight)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape)
                    )
                    UserAvatar(
                        name = remoteName,
                        avatarColorHex = "#2E9E5B",
                        size = 110.dp,
                        showPresenceBorder = false
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(SuccessGreen)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$remoteName (Live)",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RemoteVideoThumbnail(name: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CompanyNavy),
        contentAlignment = Alignment.Center
    ) {
        UserAvatar(name = name, avatarColorHex = "#2E9E5B", size = 56.dp)
    }
}

@Composable
private fun ConnectedAudioCallView(
    personName: String,
    department: String,
    duration: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        UserAvatar(
            name = personName,
            avatarColorHex = "#1F6FEB",
            size = 120.dp,
            showPresenceBorder = false
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = personName,
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "$department • CompanyCall Internal",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            color = Color.White.copy(alpha = 0.12f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = duration,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun TopCallBar(
    name: String,
    duration: String,
    callType: CallType,
    networkQuality: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Black.copy(alpha = 0.75f), Color.Transparent)
                )
            )
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = name,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = duration,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 13.sp
            )
        }

        Surface(
            color = Color.White.copy(alpha = 0.18f),
            shape = RoundedCornerShape(12.dp)
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
                Text(
                    text = networkQuality,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun BottomControlsBar(
    session: ActiveCallSession,
    onToggleMute: () -> Unit,
    onToggleCamera: () -> Unit,
    onToggleCameraLens: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onUpgradeToVideo: () -> Unit,
    onEndCall: () -> Unit
) {
    Surface(
        color = Color(0xFF14213D).copy(alpha = 0.94f),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        shadowElevation = 16.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mute Button
            IconButton(
                onClick = onToggleMute,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(if (session.isMuted) DangerRed else Color.White.copy(alpha = 0.15f))
                    .testTag("toggle_mute_button")
            ) {
                Icon(
                    imageVector = if (session.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Mute Toggle",
                    tint = Color.White
                )
            }

            // Speaker Button
            IconButton(
                onClick = onToggleSpeaker,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(if (session.isSpeakerOn) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.15f))
                    .testTag("toggle_speaker_button")
            ) {
                Icon(
                    imageVector = if (session.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                    contentDescription = "Speaker Toggle",
                    tint = Color.White
                )
            }

            // Camera Toggle / Upgrade to Video
            if (session.callType == CallType.VIDEO) {
                IconButton(
                    onClick = onToggleCamera,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(if (session.isCameraOn) Color.White.copy(alpha = 0.15f) else DangerRed)
                        .testTag("toggle_camera_button")
                ) {
                    Icon(
                        imageVector = if (session.isCameraOn) Icons.Default.Videocam else Icons.Default.VideocamOff,
                        contentDescription = "Camera Toggle",
                        tint = Color.White
                    )
                }

                // Switch Camera Lens (Front/Back)
                IconButton(
                    onClick = onToggleCameraLens,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .testTag("flip_camera_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Switch Camera",
                        tint = Color.White
                    )
                }
            } else {
                // Upgrade to Video Call
                IconButton(
                    onClick = onUpgradeToVideo,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .testTag("upgrade_video_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Upgrade to Video",
                        tint = Color.White
                    )
                }
            }

            // End Call Button
            FilledIconButton(
                onClick = onEndCall,
                modifier = Modifier
                    .size(56.dp)
                    .testTag("end_call_button"),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = DangerRed)
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "End Call",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
