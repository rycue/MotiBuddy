package com.motibuddy.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.motibuddy.app.ui.theme.MotiBuddyTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        createNotificationChannel()
        setContent {
            MotiBuddyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MotiBuddy(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun MotiBuddy(modifier: Modifier = Modifier) {
//    TIMER VARIABLES
    var timeLeft by remember { mutableLongStateOf(25 * 60L) }
    var isTimerRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            while (timeLeft > 0) {
                delay(1000)
                timeLeft -= 1
            }
            isTimerRunning = false
            // Trigger notification

        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isTimerRunning) {
            Text(text = "Paused")
        } else Text(text = "")
        Text(text = formatTime(timeLeft), fontSize = 96.sp)
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                if (isTimerRunning) {
                    // Pause the timer
                    isTimerRunning = false
//                    timeLeft = 25 * 60L
                } else {
                    // Start the timer
                    isTimerRunning = true
                }
            }
        ) {
            Text(text = if (isTimerRunning) "Pause" else "Start")
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                // Reset the timer
                timeLeft = 25 * 60L
//                timeLeft = 5 // debug
                isTimerRunning = false
            }
        ) {
            Text(text = "Reset")
        }
    }
}

/** Helper to format seconds as MM:SS */
fun formatTime(seconds: Long): String {
    val minutes = (seconds / 60) % 60
    val secs = seconds % 60
    return String.format("%02d:%02d", minutes, secs)
}

