package com.motibuddy.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.motibuddy.app.ui.theme.MotiBuddyTheme
import kotlinx.coroutines.delay

private const val CHANNEL_ID = "motibuddy_channel"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()             // keep your window styling
        createNotificationChannel()     // set up Android O+ channel
        setContent {
            MotiBuddyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MotiBuddy(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    /** Create the notification channel (Android O+) */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pomodoro Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "MotiBuddy Timer Alerts" }
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }
}

/** Context extension to show a simple notification */
@Suppress("MissingPermission")
fun Context.showNotification(title: String, message: String) {
    val builder = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
        .setContentTitle(title)
        .setContentText(message)
        .setAutoCancel(true)
        .setDefaults(NotificationCompat.DEFAULT_ALL)

    NotificationManagerCompat.from(this)
        .notify(1001, builder.build())
}

@Composable
fun MotiBuddy(modifier: Modifier = Modifier) {
    // 2) Grab a Context for notifications
    val context = LocalContext.current

    // 3) Timer state (25*60 = 1500 seconds)
    var timeLeft by remember { mutableStateOf(25 * 60L) }
    var isResetable by remember { mutableStateOf(false) }
    var isTimerRunning by remember { mutableStateOf(false) }

    // 4) Countdown coroutine + trigger notification
    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            while (timeLeft > 0) {
                delay(1_000L)
                timeLeft--
            }
            isTimerRunning = false
            isResetable = true
            context.showNotification("Pomodoro Complete", "Time for a break!")
        }
    }

    // 5) UI layout
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isTimerRunning) {
            if (isResetable) Text(text = "Paused")
            else Text(text = "Ready")
        } else Text(text = "")
        Text(text = formatTime(timeLeft), fontSize = 96.sp)
        Spacer(Modifier.height(12.dp))
        if (timeLeft > 0) {
            Button(onClick = {
                isTimerRunning = !isTimerRunning
                isResetable = true
            }) {
                Text(if (isTimerRunning) "Pause" else "Start")
            }
        }
        Spacer(Modifier.height(12.dp))
        if (isResetable) {
            Button(onClick = {
//            timeLeft = 25 * 60L
                timeLeft = 5
                isTimerRunning = false
                isResetable = false
            }) {
                Text("Reset")
            }
        }
    }
}

/** Helper to format seconds as MM:SS */
fun formatTime(seconds: Long): String {
    val minutes = (seconds / 60) % 60
    val secs = seconds % 60
    return String.format("%02d:%02d", minutes, secs)
}
