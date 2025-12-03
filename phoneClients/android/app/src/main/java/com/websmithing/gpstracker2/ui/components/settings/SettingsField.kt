package com.websmithing.gpstracker2.ui.components.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.websmithing.gpstracker2.ui.theme.error
import com.websmithing.gpstracker2.ui.theme.neutral
import com.websmithing.gpstracker2.ui.theme.surfaceContainerHigh

@Composable
fun SettingsField(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    labelSpacing: Dp,
    fieldPadding: Dp,
    isError: Boolean = false
) {
    Text(
        text = title,
        fontSize = 12.sp,
        lineHeight = 15.sp,
        color = neutral
    )

    Spacer(modifier = Modifier.height(labelSpacing))

    val borderColor = if (isError) error else Color.Transparent
    val borderWidth = if (isError) 2.dp else 0.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(borderWidth, borderColor, RoundedCornerShape(4.dp)) // border снаружи
            .background(surfaceContainerHigh, RoundedCornerShape(4.dp)) // фон внутри
            .height(40.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(modifier = Modifier.padding(horizontal = fieldPadding)) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = Color.White,
                    fontSize = 14.sp
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

