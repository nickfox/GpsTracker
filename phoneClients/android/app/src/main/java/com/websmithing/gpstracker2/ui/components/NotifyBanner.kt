package com.websmithing.gpstracker2.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.websmithing.gpstracker2.R
import com.websmithing.gpstracker2.ui.theme.successContainer
import com.websmithing.gpstracker2.ui.theme.warningContainer

enum class NotifyStatus {
    Success,
    Error
}

@Composable
fun NotifyBanner(
    notifyStatus: NotifyStatus,
    visible: Boolean,
    onDismiss: () -> Unit
) {
    val backgroundColor: Color
    val text: String
    val iconRes: Int

    when (notifyStatus) {
        NotifyStatus.Success -> {
            backgroundColor = successContainer
            text = stringResource(R.string.notify_success)
            iconRes = R.drawable.ic_notify_success
        }
        NotifyStatus.Error -> {
            backgroundColor = warningContainer
            text = stringResource(R.string.notify_error)
            iconRes = R.drawable.ic_notify_error
        }
    }

    LaunchedEffect(visible) {
        if (visible) {
            delay(5000)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                    start = 0.dp, end = 0.dp, bottom = 0.dp
                )
                .clickable { onDismiss() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Color.Unspecified
            )

            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                color = Color.White,
                fontSize = 16.sp
            )
        }
    }
}

