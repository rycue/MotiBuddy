// MainActivity.kt
package com.motibuddy.app

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.request.ImageRequest
import android.content.Intent

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.motibuddy.app.ui.theme.AppTypography

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.motibuddy.app.ui.theme.ThemeColorManager
import android.widget.Toast
import androidx.compose.ui.graphics.toArgb
import androidx.compose.runtime.collectAsState







import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.motibuddy.app.ui.theme.MotiBuddyTheme


private const val CHANNEL_ID = "motibuddy_channel"

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        createNotificationChannel()
        setContent {
            MotiBuddyTheme {
                // BEFORE Scaffold, get both VMs:
                val taskViewModel: TaskViewModel = viewModel()
                val pomodoroViewModel: PomodoroViewModel = viewModel()

                // BottomNav state…
                var selectedIndex by rememberSaveable { mutableIntStateOf(0) }

                val items = listOf(
                    BottomNavigationItem("Tasks", Icons.Filled.TaskAlt, Icons.Outlined.TaskAlt, false, 45),
                    BottomNavigationItem("Pomodoro", Icons.Filled.Timer, Icons.Outlined.Timer, false),
                    BottomNavigationItem("Bot", Icons.Filled.SmartToy, Icons.Outlined.SmartToy, true),
                    BottomNavigationItem("Themes", Icons.Filled.ColorLens, Icons.Outlined.ColorLens, false)

                )

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            items.forEachIndexed { i, item ->
                                NavigationBarItem(
                                    selected = selectedIndex == i,
                                    onClick = { selectedIndex = i },
                                    icon = {
                                        BadgedBox(
                                            badge = {
                                                when {
                                                    item.badgeCount != null -> Badge { Text(item.badgeCount.toString()) }
                                                    item.hasNews            -> Badge()
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (selectedIndex == i) item.selectedIcon else item.unselectedIcon,
                                                contentDescription = item.title
                                            )
                                        }
                                    },
                                    label = { Text(item.title) }
                                )
                            }
                        }
                    },
                    floatingActionButton = {
                        if (selectedIndex == 0) {
                            // now taskViewModel exists!
                            FloatingActionButton(onClick = {
                                taskViewModel.addTask("New Task", "Temporary")
                            }) {
                                Icon(Icons.Filled.Add, contentDescription = "Add task")
                            }
                        }
                    }
                ) { inner ->
                    Box(Modifier.padding(inner)) {
                        when (selectedIndex) {
                            0 -> TaskScreen()
                            1 -> PomodoroScreen()
                            2 -> BotScreen()
                            3 -> ThemeSettingsScreen { recreate() } // This will trigger activity recreation for new theme
                        }
                    }
                }
            }
        }

    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(
                CHANNEL_ID, "Pomodoro Alerts", NotificationManager.IMPORTANCE_DEFAULT
            ).let { channel ->
                channel.description = "Notifications when Pomodoro finishes"
                getSystemService(NotificationManager::class.java)
                    .createNotificationChannel(channel)
            }
        }
    }
}

data class BottomNavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val hasNews: Boolean,
    val badgeCount: Int? = null
)

@Suppress("MissingPermission")
fun Context.showNotification(title: String, message: String) {
    NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
        .setContentTitle(title)
        .setContentText(message)
        .setAutoCancel(true)
        .setDefaults(NotificationCompat.DEFAULT_ALL)
        .also { NotificationManagerCompat.from(this).notify(1001, it.build()) }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
@Preview(showBackground = true)
fun PomodoroScreen(
    viewModel: PomodoroViewModel = viewModel(),
    modifier: Modifier = Modifier,
    taskViewModel: PomodoroViewModel = viewModel
) {
    val context = LocalContext.current
    val timeLeft by viewModel.timeLeft.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
//    var segmentButtonSelectedIndex by remember { mutableIntStateOf(viewModel.segmentButtonSelectedIndex.value) }
    val progress = if (viewModel.segmentButtonSelectedIndex.value == 0) timeLeft.toFloat() / viewModel.timeWork else timeLeft.toFloat() / viewModel.timeBreak
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
    )

    LaunchedEffect(isRunning) {
        if (isRunning) viewModel.startTickLoop()
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16f.dp),
            shape = MaterialTheme.shapes.small
        ) {
            Column(
                modifier = Modifier.padding(16f.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Task Name",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Task Description",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        }
        // Segmented buttons
        SingleChoiceSegmentedButtonRow {
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(0, 2),
                onClick = {
                    viewModel.setSegmentButtonSelectedIndex(0)
                    viewModel.resetTimer(true)
                },
                selected = viewModel.segmentButtonSelectedIndex.value == 0,
                icon = { Icon(Icons.Filled.HourglassBottom, contentDescription = "Work") },
                label = { Text("Focus") }
            )
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(1, 2),
                onClick = {
                    viewModel.setSegmentButtonSelectedIndex(1)
                    viewModel.resetTimer(false)
                },
                selected = viewModel.segmentButtonSelectedIndex.value == 1,
                icon = { Icon(Icons.Filled.Coffee, contentDescription = "Break") },
                label = { Text("Break") }
            )
        }

        Spacer(Modifier.height(24f.dp))

        Box(
            modifier = Modifier.size(300.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularWavyProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.matchParentSize()
            )
            Text(
                text = formatTime(timeLeft),
                fontSize = 96.sp
            )
        }

        Spacer(Modifier.height(24.dp))

        // Start/Pause & Reset Buttons
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    if (isRunning) viewModel.stopTimer()
                    else viewModel.runTimer {
                        context.showNotification("Pomodoro Complete", "Time for a break!")
                    }
                },
                enabled = timeLeft != 0L
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                    contentDescription = null
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(if (isRunning) "Pause" else "Start")
            }

            val justReset by viewModel.timerJustReset.collectAsState()
            OutlinedButton(
                onClick = {
                    viewModel.resetTimer(viewModel.segmentButtonSelectedIndex.value == 0)
                },
                enabled = !justReset
            ) {
                Icon(Icons.Outlined.RestartAlt, contentDescription = null)
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Reset")
            }
        }
    }
}

data class Task(
    val id: Int,
    val title: String,
    val description: String = "",
    val isDone: Boolean = false
)
@Composable
fun TaskScreen(taskViewModel: TaskViewModel = viewModel()) {
    val taskList by taskViewModel.taskList.collectAsState()
    val listState = rememberLazyListState()

    // Automatically scroll to top when a new task is added
    LaunchedEffect(taskList.size) {
        if (taskList.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Tasks", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(8.dp))

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Show newest task first
            items(taskList.reversed()) { task ->
                Card(
                    onClick = { taskViewModel.setCurrentTask(task.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(task.title, style = MaterialTheme.typography.titleLarge)
                        if (task.description.isNotBlank())
                            Text(task.description)
                    }
                }
            }
        }
    }
}


@Composable fun HomeScreen() {
    Text("Home")
}
@Composable
fun BotScreen() {
    val context = LocalContext.current
    val prefs = remember { BotPrefs(context) }
    val contentResolver = context.contentResolver

    var username by remember { mutableStateOf(prefs.username) }
    var message by remember { mutableStateOf(prefs.message) }
    var imageUri by remember { mutableStateOf<Uri?>(prefs.imageUri?.let { Uri.parse(it) }) }
    var colorHex by remember { mutableStateOf(prefs.themeColor) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            imageUri = it
            prefs.imageUri = it.toString()
        }
    }

    LaunchedEffect(username, message) {
        prefs.username = username
        prefs.message = message
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Customize Your Bot", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(16.dp))

        Box(contentAlignment = Alignment.BottomEnd) {
            val imageModifier = Modifier
                .size(120.dp)
                .clip(CircleShape)

            if (imageUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUri)
                        .size(100) // resize to 100x100
                        .crossfade(true)
                        .build(),
                    contentDescription = "Bot Profile",
                    contentScale = ContentScale.Crop,
                    modifier = imageModifier
                )
            } else {
                Icon(
                    Icons.Default.SmartToy,
                    contentDescription = null,
                    modifier = imageModifier,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = { launcher.launch("image/*") }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Image")
            }
        }

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Bot Name") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Motivational Message") },
            leadingIcon = { Icon(Icons.Default.Notifications, contentDescription = null) }
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = colorHex,
            onValueChange = {
                colorHex = it
                prefs.themeColor = it
            },
            label = { Text("Primary Color Hex") },
            leadingIcon = { Icon(Icons.Default.ColorLens, contentDescription = null) },
            placeholder = { Text("#6750A4") }
        )

        Spacer(Modifier.height(8.dp))

        val scope = rememberCoroutineScope()

        Button(onClick = {
            try {
                val parsedColor = Color(android.graphics.Color.parseColor(colorHex))
                prefs.themeColor = colorHex // Optional if you still want to persist
                ThemeColorManager.setCustomPrimary(context, parsedColor) // 🔁 This triggers immediate update
            } catch (e: Exception) {
                Toast.makeText(context, "Invalid hex code", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text("Apply Color")
        }
    }
}


@Composable
fun CustomTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    var primaryColor by remember { mutableStateOf(Color(0xFF6750A4)) }

    LaunchedEffect(Unit) {
        primaryColor = ThemeColorManager.getPrimaryColor(context)
    }

    val colorScheme = lightColorScheme(
        primary = primaryColor,
        secondary = Color(0xFF625B71)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}



// Helpers
fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val m = totalSec / 60
    val s = totalSec % 60
    return "%02d:%02d".format(m, s)
}