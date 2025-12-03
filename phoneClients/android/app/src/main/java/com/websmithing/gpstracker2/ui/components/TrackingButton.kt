package com.websmithing.gpstracker2.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.websmithing.gpstracker2.R
import com.websmithing.gpstracker2.ui.theme.error
import com.websmithing.gpstracker2.ui.theme.neutral
import com.websmithing.gpstracker2.ui.theme.secondary
import com.websmithing.gpstracker2.ui.theme.success

enum class TrackingButtonState {
    Disabled,
    Tracking,
    Stopped
}

@Composable
fun TrackingButton(
    modifier: Modifier = Modifier,
    status: TrackingButtonState = TrackingButtonState.Disabled,
    onClick: () -> Unit
) {
    val (bgColor, iconRes, iconTint) = when (status) {
        TrackingButtonState.Disabled -> Triple(
            secondary,
            R.drawable.ic_stop,
            neutral
        )
        TrackingButtonState.Tracking -> Triple(
            error,
            R.drawable.ic_pause,
            Color.White
        )
        TrackingButtonState.Stopped -> Triple(
            success,
            R.drawable.ic_start,
            Color.White
        )
    }

    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(bgColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
    }
}

