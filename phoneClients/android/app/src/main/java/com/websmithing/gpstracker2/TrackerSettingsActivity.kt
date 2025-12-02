package com.websmithing.gpstracker2

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.livedata.observeAsState
import androidx.core.view.WindowInsetsCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.websmithing.gpstracker2.di.SettingsRepositoryEntryPoint
import com.websmithing.gpstracker2.ui.TrackingViewModel
import com.websmithing.gpstracker2.ui.components.settings.LanguageButton
import com.websmithing.gpstracker2.ui.components.settings.SaveButton
import com.websmithing.gpstracker2.ui.components.settings.SettingsField
import com.websmithing.gpstracker2.ui.theme.neutral
import com.websmithing.gpstracker2.ui.theme.neutralVariant
import com.websmithing.gpstracker2.ui.theme.surface
import com.websmithing.gpstracker2.ui.theme.surfaceContainerHigh
import com.websmithing.gpstracker2.ui.theme.surfaceVariant
import com.websmithing.gpstracker2.util.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors

@AndroidEntryPoint
class TrackerSettingsActivity : ComponentActivity() {

    // --- Activity Lifecycle ---

    override fun attachBaseContext(newBase: Context) {
        // 1. Get the EntryPoint accessor from the application context
        val entryPoint = EntryPointAccessors.fromApplication(
            newBase.applicationContext,
            SettingsRepositoryEntryPoint::class.java
        )

        // 2. Use the EntryPoint to get the repository instance
        val repo = entryPoint.getSettingsRepository()

        // 3. Use your LocaleHelper to create the new context
        val newCtx = LocaleHelper.onAttach(newBase, repo)

        super.attachBaseContext(newCtx)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SettingsScreen()
        }
    }
}

@Composable
fun SettingsScreen(viewModel: TrackingViewModel = hiltViewModel()) {
    var selectedLanguage by remember { mutableStateOf(viewModel.language.value ?: "ru") }
    val username by viewModel.userName.observeAsState(initial = "")
    val website by viewModel.websiteUrl.observeAsState(initial = "")
    val intervalMinutes by viewModel.trackingInterval.observeAsState(initial = 1)

    val context = LocalContext.current
    val activity = context as? Activity

    var isSaveClicked by remember { mutableStateOf(false) }

    val isFormValid by derivedStateOf {
        username.isNotBlank() &&
        website.isNotBlank() &&
        intervalMinutes > 0
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF21282E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
        ) {
            val view = LocalView.current
            val statusBarHeight = with(LocalDensity.current) {
                WindowInsetsCompat.toWindowInsetsCompat(view.rootWindowInsets)
                    .getInsets(WindowInsetsCompat.Type.statusBars()).top.toDp()
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF252D33))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(statusBarHeight))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .padding(start = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clickable { activity?.finish() },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_close_settings),
                                contentDescription = "close",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Настройки",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            lineHeight = 23.sp
                        )
                    }
                }
            }

            val fieldSpacing = 16.dp
            val labelToInputSpacing = 8.dp
            val sidePadding = 16.dp

            Column(modifier = Modifier.padding(horizontal = sidePadding, vertical = 16.dp)) {
                SettingsField(
                    title = stringResource(R.string.settings_identifier),
                    value = username,
                    onValueChange = { newValue ->
                        val filtered = newValue.filter { it.isDigit() }
                        viewModel.onUserNameChanged(filtered)
                    },
                    labelSpacing = labelToInputSpacing,
                    fieldPadding = sidePadding,
                    isError = isSaveClicked && username.isBlank()
                )
                Spacer(modifier = Modifier.height(fieldSpacing))

                SettingsField(
                    title = stringResource(R.string.settings_server_url),
                    value = website,
                    onValueChange = { newValue ->
                        val filtered = newValue.filter { it.toString().matches(Regex("[a-zA-Z0-9.:/\\-_?&=]")) }
                        viewModel.onWebsiteUrlChanged(filtered)
                    },
                    labelSpacing = labelToInputSpacing,
                    fieldPadding = sidePadding,
                    isError = isSaveClicked && website.isBlank()
                )
                Spacer(modifier = Modifier.height(fieldSpacing))

                SettingsField(
                    title = stringResource(R.string.settings_interval_minutes),
                    value = intervalMinutes.toString(),
                    onValueChange = { newValue ->
                        val filtered = newValue.filter { it.isDigit() }
                        val intVal = filtered.toIntOrNull() ?: 1
                        viewModel.onIntervalChanged(intVal)
                    },
                    labelSpacing = labelToInputSpacing,
                    fieldPadding = sidePadding,
                    isError = isSaveClicked && intervalMinutes.toString().isBlank()
                )
                Spacer(modifier = Modifier.height(fieldSpacing))

                Text(
                    text = stringResource(R.string.settings_language),
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    color = Color(0xFFA8B2B7)
                )
                Spacer(modifier = Modifier.height(labelToInputSpacing))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                ) {
                    LanguageButton(
                        selected = selectedLanguage == "ru",
                        label = stringResource(R.string.settings_lang_ru),
                        icon = R.drawable.ic_flag_ru,
                        onClick = {
                            selectedLanguage = "ru"
                            viewModel.onLanguageChanged("ru")
                        },
                        modifier = Modifier.weight(1f)
                    )
                    LanguageButton(
                        selected = selectedLanguage == "en",
                        label = stringResource(R.string.settings_lang_en),
                        icon = R.drawable.ic_flag_en,
                        onClick = {
                            selectedLanguage = "en"
                            viewModel.onLanguageChanged("en")
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

            Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.ic_logo),
                contentDescription = "logo",
                modifier = Modifier
                    .width(140.dp)
                    .height(47.dp),
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(neutral)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_app_version),
                    fontSize = 14.sp,
                    color = neutralVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = BuildConfig.VERSION_NAME,
                    fontSize = 14.sp,
                    color = neutral
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(Color(0xFF252D33)),
                contentAlignment = Alignment.Center
            ) {
                val context = LocalContext.current
                val activity = context as? Activity
                SaveButton(
                    onClick = {
                        if (isFormValid) {
                            viewModel.saveSettings()
                            activity?.finish()
                        } else {
                            isSaveClicked = true
                        }
                    },
                    enabled = isFormValid,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            }

    }
}

