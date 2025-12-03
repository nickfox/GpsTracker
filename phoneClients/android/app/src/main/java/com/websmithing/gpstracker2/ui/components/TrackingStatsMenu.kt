package com.websmithing.gpstracker2.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.websmithing.gpstracker2.ui.TrackingViewModel
import kotlinx.coroutines.launch
import com.websmithing.gpstracker2.data.repository.UploadStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.websmithing.gpstracker2.R
import com.websmithing.gpstracker2.ui.theme.error
import com.websmithing.gpstracker2.ui.theme.neutral
import com.websmithing.gpstracker2.ui.theme.outline
import com.websmithing.gpstracker2.ui.theme.outlineVariant
import com.websmithing.gpstracker2.ui.theme.primary
import com.websmithing.gpstracker2.ui.theme.secondary
import com.websmithing.gpstracker2.ui.theme.surface
import com.websmithing.gpstracker2.ui.theme.surfaceContainer
import com.websmithing.gpstracker2.ui.theme.surfaceContainerHigh
import com.websmithing.gpstracker2.ui.theme.surfaceVariant


@Composable
fun BottomSwipeMenu(
    visible: Boolean,
    onClose: () -> Unit,
    viewModel: TrackingViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    if (!visible) return

    val scope = rememberCoroutineScope()
    val maxOffsetDp = 320.dp
    val density = LocalDensity.current
    val maxOffsetPx = with(density) { maxOffsetDp.toPx() }

    val offsetPx = remember { Animatable(maxOffsetPx) }

    LaunchedEffect(visible) {
        if (visible) {
            offsetPx.animateTo(0f, tween(300))
        } else {
            offsetPx.animateTo(maxOffsetPx, tween(300))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) { detectVerticalDragGestures { _, _ -> } }
            .pointerInput(Unit) {
                detectTapGestures {
                    scope.launch {
                        offsetPx.animateTo(maxOffsetPx, tween(250))
                        onClose()
                    }
                }
            }
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(maxOffsetDp)
                .align(Alignment.BottomCenter)
                .offset { IntOffset(x = 0, y = offsetPx.value.toInt()) }
                .background(surfaceVariant),
            contentAlignment = Alignment.TopCenter
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onVerticalDrag = { _, dragAmountPx ->
                                    val newOffset = (offsetPx.value + dragAmountPx)
                                        .coerceIn(0f, maxOffsetPx)
                                    scope.launch { offsetPx.snapTo(newOffset) }
                                },
                                onDragEnd = {
                                    scope.launch {
                                        if (offsetPx.value > maxOffsetPx * 0.35f) {
                                            offsetPx.animateTo(maxOffsetPx, tween(250))
                                            onClose()
                                        } else {
                                            offsetPx.animateTo(0f, tween(250))
                                        }
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(56.dp)
                            .height(4.dp)
                            .background(surfaceContainerHigh, shape = RoundedCornerShape(2.dp))
                    )
                }

                val username by viewModel.userName.observeAsState(initial = "")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    UsernameBox(
                        username = username,
                        modifier = Modifier
                    )
                }

                val location by viewModel.latestLocation.collectAsState(initial = null)
                val coordinateText = location?.let { "${it.latitude}; ${it.longitude}" } ?: "–"
                val speedText = location?.let { String.format("%.1f км/ч", it.speed * 3.6f) } ?: "–"

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(surface)
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            MenuRow(stringResource(R.string.stats_coordinates), coordinateText)
                            MenuRow(stringResource(R.string.stats_speed), speedText)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(surfaceContainer)
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            MenuRow(stringResource(R.string.stats_gsm_level), "–")
                            MenuRow(
                                stringResource(R.string.stats_gps_signal),
                                location?.let { getSignalStrengthDescription(it.accuracy) } ?: "–"
                            )
                            val uploadStatus by viewModel.lastUploadStatus.collectAsState(initial = UploadStatus.Idle)
                            MenuRow(
                                stringResource(R.string.stats_updated_at),
                                getUploadStatusText(uploadStatus, location?.time)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        val location by viewModel.latestLocation.collectAsState(initial = null)
                        val distanceMeters by viewModel.totalDistance.collectAsState(initial = 0f)

                        AdditionalDataSection(
                            data = listOf(
                                stringResource(R.string.stats_accuracy) to (location?.accuracy?.toString() ?: "–"),
                                stringResource(R.string.stats_distance) to String.format("%.2f км", distanceMeters / 1000f),
                                stringResource(R.string.stats_data_buffer) to "–"
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdditionalDataSection(
    data: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(surface)
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.stats_additional_data),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFFA8B2B7),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val allEmpty = data.all { it.second == "–" }

            if (allEmpty) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    data.forEach { (title, value) ->
                        MiniDataItem(title, value)
                    }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    MiniDataItem(data[0].first, data[0].second)
                    MiniDataItem(data[1].first, data[1].second)
                }
                if (data.size > 2) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        MiniDataItem(data[2].first, data[2].second)
                    }
                }
            }
        }
    }
}

@Composable
fun MiniDataItem(title: String, value: String) {
    Box(
        modifier = Modifier
            .height(20.dp)
            .background(surfaceContainerHigh, RoundedCornerShape(2.dp)) // внешний фон всего элемента
            .border(width = 0.5.dp, color = outline)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 4.dp, vertical = 1.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$title:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(outlineVariant, RoundedCornerShape(1.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = value,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun MenuRow(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = neutral,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = Color.White
        )
    }
}

@Composable
fun UsernameBox(
    username: String,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (username.isEmpty()) error else primary
    val displayText = if (username.isNotEmpty()) username else stringResource(R.string.stats_id_specify)

    Box(
        modifier = modifier
            .height(24.dp)
            .background(backgroundColor, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

/**
 * Updates the upload status display with the provided status and timestamp
 *
 * @param status The upload status to display
 * @param lastLocationTime The timestamp of the last location, or null if not available
 */
@Composable
private fun getUploadStatusText(status: UploadStatus, lastLocationTime: Long?): String {
    val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val timeString = lastLocationTime?.let { timeFormatter.format(Date(it)) } ?: "--:--:--"

    return when (status) {
        is UploadStatus.Idle -> stringResource(R.string.upload_status_idle)
        is UploadStatus.Success -> stringResource(R.string.upload_status_success, timeString)
        is UploadStatus.Failure -> stringResource(R.string.upload_status_failure, timeString, status.errorMessage ?: "Unknown error")
    }
}

/**
 * Gets a human-readable description of signal strength based on accuracy
 *
 * @param accuracy The accuracy of the location in meters
 * @return A string describing the signal strength
 */
@Composable
private fun getSignalStrengthDescription(accuracy: Float): String {
    return when {
        accuracy <= 0 -> stringResource(R.string.signal_unknown)
        accuracy <= 10 -> stringResource(R.string.signal_excellent)
        accuracy <= 25 -> stringResource(R.string.signal_good)
        accuracy <= 50 -> stringResource(R.string.signal_fair)
        else -> stringResource(R.string.signal_poor)
    }
}
