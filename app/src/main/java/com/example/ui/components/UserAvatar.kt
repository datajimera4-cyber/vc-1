package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Presence
import com.example.ui.theme.PresenceOffline
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningOrange

@Composable
fun UserAvatar(
    name: String,
    avatarColorHex: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    presence: Presence? = null,
    showPresenceBorder: Boolean = true
) {
    val initials = name.trim().split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .uppercase()

    val parsedColor = try {
        Color(android.graphics.Color.parseColor(avatarColorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(parsedColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials.ifEmpty { "C" },
                color = Color.White,
                fontSize = (size.value * 0.4f).sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (presence != null) {
            val presenceColor = when (presence) {
                Presence.ONLINE -> SuccessGreen
                Presence.IN_CALL -> WarningOrange
                Presence.OFFLINE -> PresenceOffline
            }

            val dotSize = (size.value * 0.28f).coerceIn(10f, 16f).dp
            val borderModifier = if (showPresenceBorder) {
                Modifier.border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
            } else Modifier

            Box(
                modifier = Modifier
                    .size(dotSize)
                    .align(Alignment.BottomEnd)
                    .then(borderModifier)
                    .clip(CircleShape)
                    .background(presenceColor)
            )
        }
    }
}
