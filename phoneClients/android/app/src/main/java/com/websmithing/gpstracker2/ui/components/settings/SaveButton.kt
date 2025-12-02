package com.websmithing.gpstracker2.ui.components.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.websmithing.gpstracker2.R
import com.websmithing.gpstracker2.ui.theme.neutralVariant
import com.websmithing.gpstracker2.ui.theme.primary
import com.websmithing.gpstracker2.ui.theme.secondary

@Composable
fun SaveButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = false
) {
    val backgroundColor = if (enabled) primary else secondary
    val textColor = if (enabled) Color.White else neutralVariant
    val iconTint = if (enabled) Color.White else neutralVariant

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp)
            .background(backgroundColor, RoundedCornerShape(4.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = "check",
                modifier = Modifier
                    .size(16.dp)
                    .padding(end = 4.dp),
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(iconTint)
            )
            Text(
                text = stringResource(R.string.settings_save),
                color = textColor,
                fontSize = 14.sp
            )
        }
    }
}

