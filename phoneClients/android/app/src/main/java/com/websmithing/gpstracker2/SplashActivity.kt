package com.websmithing.gpstracker2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.websmithing.gpstracker2.util.LocaleHelper
import com.websmithing.gpstracker2.di.SettingsRepositoryEntryPoint
import com.websmithing.gpstracker2.ui.theme.AppTheme
import dagger.hilt.android.EntryPointAccessors

class SplashActivity : ComponentActivity() {

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

    /**
     * Called when the activity is first created.
     *
     * Sets up the splash screen UI using schedules the transition
     * to the main activity after a fixed delay.
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SplashScreen()
        }

        Thread {
            Thread.sleep(3300)
            startActivity(Intent(this, GpsTrackerActivity::class.java))
            finish()
        }.start()
    }
}

/**
 * Composable function that renders the splash screen UI.
 *
 * Displays the app logo and subtitle with a fade-in/fade-out animation.
 * The animation lasts for 3 seconds before fading out and transitioning
 * to the next screen.
 */
@Composable
fun SplashScreen() {
    var visible by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(3000)
        visible = false
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = ""
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0080E5)),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(alpha)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .width(204.dp)
                    .height(68.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.splash_subtitle),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

