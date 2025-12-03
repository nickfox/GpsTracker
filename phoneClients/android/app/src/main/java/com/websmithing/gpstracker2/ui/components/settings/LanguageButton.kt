package com.websmithing.gpstracker2.ui.components.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.websmithing.gpstracker2.ui.theme.secondary
import com.websmithing.gpstracker2.ui.theme.surfaceContainer
import com.websmithing.gpstracker2.ui.theme.surfaceContainerHigh

@Composable
fun LanguageButton(
    selected: Boolean,
    label: String,
    icon: Int,
    onClick: () -> Unit,
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(
                if (selected) secondary else surfaceContainerHigh,
                RoundedCornerShape(4.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier
                    .size(16.dp)
                    .padding(end = 4.dp)
            )
            Text(
                text = label,
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}