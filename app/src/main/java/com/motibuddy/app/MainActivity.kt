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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Whatshot
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.motibuddy.app.ui.theme.MotiBuddyTheme
import kotlinx.coroutines.delay

data class BottomNavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val hasNews: Boolean,
    val badgeCount: Int? = null
)

private const val CHANNEL_ID = "motibuddy_channel"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()             // keep your window styling
        createNotificationChannel()     // set up Android O+ channel
        setContent {
            MotiBuddyTheme {
                val items = listOf(
                    BottomNavigationItem(
                        title = "Home",
                        selectedIcon = Icons.Filled.Home,
                        unselectedIcon = Icons.Outlined.Home,
                        hasNews = false,
                        badgeCount = 45,
                    ),
                    BottomNavigationItem(
                        title = "Pomodoro",
                        selectedIcon = Icons.Filled.Timer,
                        unselectedIcon = Icons.Outlined.Timer,
                        hasNews = false
                    ),
                    BottomNavigationItem(
                        title = "Bot",
                        selectedIcon = Icons.Filled.SmartToy,
                        unselectedIcon = Icons.Outlined.SmartToy,
                        hasNews = true
                    )
                )
                var selectedItemIndex by rememberSaveable { mutableIntStateOf(0) }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar {
                            items.forEachIndexed { index, item ->
                                NavigationBarItem(
                                    selected = selectedItemIndex == index,
                                    onClick = {
                                        selectedItemIndex = index
//                                    navController.navigate(item.title)
                                    },
                                    label = { Text(item.title) },
                                    icon = {
                                        BadgedBox(
                                            badge = {
                                                if (item.badgeCount != null) {
                                                    Badge {
                                                        Text(item.badgeCount.toString())
                                                    }
                                                } else if (item.hasNews) Badge()
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (index == selectedItemIndex) {
                                                    item.selectedIcon
                                                } else item.unselectedIcon,
                                                contentDescription = item.title
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                        MotiBuddy()
                    }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotiBuddy(modifier: Modifier = Modifier) {
    // Grab a Context for notifications
    val context = LocalContext.current

    // Timer state (25*60 = 1500 seconds)
    var timeLeft by remember { mutableIntStateOf(25 * 60) }
    var isResetable by remember { mutableStateOf(false) }
    var isTimerRunning by remember { mutableStateOf(false) }
    var isTakingABreak by remember { mutableStateOf(false) }

    // Countdown coroutine + trigger notification
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

    // UI layout
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isTimerRunning) {
            if (isResetable) Text(if (timeLeft > 0) "Paused" else "Done")
            else Text( text = if (!isTakingABreak) "Ready to work?" else "Ready to take a break?")
        } else Text(text = if (!isTakingABreak) "Working" else "Taking a break")
        Text(text = formatTime(timeLeft), fontSize = 96.sp)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (timeLeft > 0) {
                Button(onClick = {
                    isTimerRunning = !isTimerRunning
                    isResetable = true
                }) {
                    if (!isResetable && !isTimerRunning) {
                        Icon(
                            imageVector = Icons.Outlined.PlayArrow,
                            contentDescription = "Start the timer",
                            modifier = Modifier.size(ButtonDefaults.IconSize),
                        )
                        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Start")
                    } else {
                        if (isTimerRunning) {
                            Icon(
                                imageVector = Icons.Outlined.Pause,
                                contentDescription = "Pause the timer",
                                modifier = Modifier.size(ButtonDefaults.IconSize),
                            )
                            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                            Text(text = "Pause")
                        }
                        else {
                            Icon(
                                imageVector = Icons.Outlined.PlayArrow,
                                contentDescription = "Resume the timer",
                                modifier = Modifier.size(ButtonDefaults.IconSize),
                            )
                            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                            Text(text = "Resume")
                        }
                    }
                }
            }
            if (timeLeft == 0) {
                if (!isTakingABreak) {
                    Button(onClick = {
                        isTimerRunning = false
                        isResetable = false
                        isTakingABreak = true
                        timeLeft = resetTimer(isTakingABreak)
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Coffee,
                            contentDescription = "Take a break",
                            modifier = Modifier.size(ButtonDefaults.IconSize),
                        )
                        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Take a break")
                    }
                } else {
                    Button(onClick = {
                        isTimerRunning = false
                        isResetable = false
                        isTakingABreak = false
                        timeLeft = resetTimer(isTakingABreak)
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.RestartAlt,
                            contentDescription = "Reset or start a new session",
                            modifier = Modifier.size(ButtonDefaults.IconSize),
                        )
                        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Get back to work")
                    }
                }
            }
            if (isResetable) {
                if (isTakingABreak) {
                    OutlinedButton(onClick = {
                        isTimerRunning = false
                        isResetable = false
                        timeLeft = resetTimer(isTakingABreak)
                    }) {
                        Icon(
                            imageVector = if (timeLeft > 0) Icons.Outlined.RestartAlt else Icons.Outlined.Coffee,
                            contentDescription = "Another break",
                            modifier = Modifier.size(ButtonDefaults.IconSize),
                        )
                        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                        Text(if (timeLeft > 0) "Reset" else "Extend")
                    }
                } else {
                    OutlinedButton(onClick = {
                        isTimerRunning = false
                        isResetable = false
                        timeLeft = resetTimer(isTakingABreak)
                    }) {
                        Icon(
                            imageVector = if (timeLeft > 0) Icons.Outlined.RestartAlt else Icons.Outlined.Whatshot,
                            contentDescription = "Another break",
                            modifier = Modifier.size(ButtonDefaults.IconSize),
                        )
                        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                        Text(if (timeLeft > 0) "Reset" else "Keep going")
                    }
                }
            }
        }
    }
}

fun resetTimer(isTakingABreak: Boolean): Int {
    return if (isTakingABreak) 5 else 8 // DEBUG
//    return if (isTakingABreak) 5 * 60 else 25 * 60
}

/** Helper to format seconds as MM:SS */
fun formatTime(seconds: Int): String {
    val minutes = (seconds / 60) % 60
    val secs = seconds % 60
    return String.format("%02d:%02d", minutes, secs)
}
