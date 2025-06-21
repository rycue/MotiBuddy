// MainActivity.kt
package com.motibuddy.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.ListItem

private const val CHANNEL_ID = "motibuddy_channel"

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        createNotificationChannel()
        setContent {
            MotiBuddyTheme {
                val taskVM = viewModel<TaskViewModel>()
                val pomoVM = viewModel<PomodoroViewModel>()
                var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
                var editingTaskId by rememberSaveable { mutableStateOf<Int?>(null) }
                var isCreatingNewTask by rememberSaveable { mutableStateOf(false) }
                val ctx = LocalContext.current

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            val items = listOf(
                                BottomNavigationItem(
                                    "Tasks",
                                    Icons.Filled.TaskAlt,
                                    Icons.Outlined.TaskAlt,
                                    false,
                                    45
                                ),
                                BottomNavigationItem(
                                    "Pomodoro",
                                    Icons.Filled.Timer,
                                    Icons.Outlined.Timer,
                                    false
                                ),
                                BottomNavigationItem(
                                    "Bot",
                                    Icons.Filled.SmartToy,
                                    Icons.Outlined.SmartToy,
                                    true
                                )
                            )

                            items.forEachIndexed { i, item ->
                                NavigationBarItem(
                                    selected = selectedIndex == i,
                                    onClick = { selectedIndex = i },
                                    icon = {
                                        BadgedBox(
                                            badge = {
                                                when {
                                                    item.badgeCount != null -> Badge { Text(item.badgeCount.toString()) }
                                                    item.hasNews -> Badge()
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
                    }
                ) { inner ->
                    Box(Modifier.padding(inner)) {
                        when (selectedIndex) {
                            0 -> TaskScreen(
                                taskViewModel = taskVM,
                                onTaskClick = { id ->
                                    // only allow if not already done
                                    taskVM.taskList.value.find { it.id == id }?.let { task ->
                                        taskVM.taskList.value.find { it.id == id }?.let { task ->
                                            if (!task.isDone) {
                                                taskVM.setCurrentTask(id)
                                                selectedIndex = 1
                                                isCreatingNewTask = false
                                            } else {
                                                Toast.makeText(
                                                    ctx,
                                                    "That task is already completed",
                                                    Toast.LENGTH_SHORT
                                                ).show()

                                            }
                                        }
                                    }
                                },
                                onEditTask = { id -> editingTaskId = id },
                                editingTaskId = editingTaskId,
                                onEditDone = {
                                    editingTaskId = null
                                    isCreatingNewTask = false
                                },
                                isCreatingNewTask = isCreatingNewTask,

                                // 👇 these are new
                                onCreateNewTask = {
                                    isCreatingNewTask = true
                                    editingTaskId = null
                                },
                                onCancel = {
                                    isCreatingNewTask = false
                                    editingTaskId = null
                                },
                                onSaveTask = { title, desc ->
                                    if (isCreatingNewTask) {
                                        taskVM.addTask(title, desc)
                                    } else {
                                        editingTaskId?.let { taskVM.updateTask(it, title, desc) }
                                    }
                                    isCreatingNewTask = false
                                    editingTaskId = null
                                },
                                onDeleteTask = {
                                    editingTaskId?.let { taskVM.removeTask(it) }
                                    isCreatingNewTask = false
                                    editingTaskId = null
                                }
                            )

                            1 -> PomodoroScreen(
                                pomodoroViewModel = pomoVM,
                                taskViewModel = taskVM
                            )

                            2 -> BotScreen()
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
    modifier: Modifier = Modifier,
    pomodoroViewModel: PomodoroViewModel = viewModel(),
    taskViewModel: TaskViewModel = viewModel()
) {
    var showConfirmDone by remember { mutableStateOf(false) }
    var pendingDoneState by remember { mutableStateOf(false) }
    val current by taskViewModel.currentTask.collectAsState()
    val context = LocalContext.current
    val timeLeft by pomodoroViewModel.timeLeft.collectAsState()
    val isRunning by pomodoroViewModel.isRunning.collectAsState()
//    var segmentButtonSelectedIndex by remember { mutableIntStateOf(pomodoroViewModel.segmentButtonSelectedIndex.value) }
    val progress =
        if (pomodoroViewModel.segmentButtonSelectedIndex.value == 0) timeLeft.toFloat() / pomodoroViewModel.timeWork else timeLeft.toFloat() / pomodoroViewModel.timeBreak
    val totalDuration by pomodoroViewModel.currentTotalDuration.collectAsState()
    val animatedProgress by animateFloatAsState(
        targetValue = timeLeft.toFloat() / totalDuration.toFloat(),
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
    )

    val currentTask by taskViewModel.currentTask.collectAsState()

    LaunchedEffect(isRunning) {
        if (isRunning) pomodoroViewModel.startTickLoop()
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ElevatedCard(
            elevation = CardDefaults.elevatedCardElevation(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = MaterialTheme.shapes.small
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (current != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = current!!.isDone,
                            onCheckedChange = {
                                // Pause timer if running and confirm marking done
                                pendingDoneState = it
                                if (isRunning) pomodoroViewModel.stopTimer()
                                showConfirmDone = true
                            }
                        )
                        Column {
                            Text(current!!.title, style = MaterialTheme.typography.headlineSmall)
                            if (current!!.description.isNotBlank())
                                Text(
                                    current!!.description,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                        }
                    }
                } else {
                    Text("No task selected", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(Modifier.height(24f.dp))

        // Segmented buttons
        SingleChoiceSegmentedButtonRow {
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(0, 2),
                onClick = {
                    pomodoroViewModel.setSegmentButtonSelectedIndex(0)
                    pomodoroViewModel.resetTimer()
                },
                selected = pomodoroViewModel.segmentButtonSelectedIndex.value == 0,
                icon = { Icon(Icons.Filled.HourglassBottom, contentDescription = "Work") },
                label = { Text("Focus") }
            )
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(1, 2),
                onClick = {
                    pomodoroViewModel.setSegmentButtonSelectedIndex(1)
//                    pomodoroViewModel.resetTimer(false)
                },
                selected = pomodoroViewModel.segmentButtonSelectedIndex.value == 1,
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
                    if (isRunning) pomodoroViewModel.stopTimer()
                    else pomodoroViewModel.runTimer {
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

            val justReset by pomodoroViewModel.timerJustReset.collectAsState()
            OutlinedButton(
                onClick = {
                    pomodoroViewModel.resetTimer()
                },
                enabled = !justReset
            ) {
                Icon(Icons.Outlined.RestartAlt, contentDescription = null)
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Reset")
            }
        }

        Spacer(Modifier.width(8.dp))

        Spacer(Modifier.height(16.dp))

// Preset timer buttons
        val openCustomDialog = remember { mutableStateOf(false) }
        if (openCustomDialog.value) {
            var minutes by remember { mutableStateOf("") }
            var seconds by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { openCustomDialog.value = false },
                title = { Text("Custom Timer") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = minutes,
                            onValueChange = { minutes = it.filter { c -> c.isDigit() } },
                            label = { Text("Minutes") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = seconds,
                            onValueChange = { seconds = it.filter { c -> c.isDigit() } },
                            label = { Text("Seconds") },
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val min = minutes.toLongOrNull() ?: 0L
                        val sec = seconds.toLongOrNull() ?: 0L
                        val totalMs = (min * 60 + sec) * 1000
                        pomodoroViewModel.setCustomTime(totalMs)
                        openCustomDialog.value = false
                    }) {
                        Text("Set")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { openCustomDialog.value = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val isWorkMode = pomodoroViewModel.segmentButtonSelectedIndex.value == 0

            val presets = if (isWorkMode)
                listOf(25 * 60 * 1000L to "25 min", 10 * 60 * 1000L to "10 min")
            else
                listOf(5 * 60 * 1000L to "5 min", 3 * 60 * 1000L to "3 min")

            presets.forEach { (duration, label) ->
                OutlinedButton(
                    onClick = { pomodoroViewModel.setCustomTime(duration) },
                    enabled = !isRunning
                ) {
                    Text(label)
                }
            }

            OutlinedButton(
                onClick = { openCustomDialog.value = true },
                enabled = !isRunning
            ) {
                Text("Custom")
            }
        }


        // Confirmation dialog
        if (showConfirmDone && current != null) {
            AlertDialog(
                onDismissRequest = { showConfirmDone = false },
                title = { Text("Mark task ${if (pendingDoneState) "done" else "undone"}?") },
                text = {
                    Text(
                        if (pendingDoneState)
                            "This will stop your Pomodoro and mark “${current!!.title}” as completed."
                        else
                            "This will un‐mark “${current!!.title}” as completed."
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        // commit the toggle
                        taskViewModel.toggleTaskDone(current!!.id)
                        showConfirmDone = false
                    }) { Text("Yes") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        // if they cancel, we need to resume timer if it was running
                        showConfirmDone = false
                        if (timeLeft > 0L) {
                            // they had been running, so restart
                            pomodoroViewModel.runTimer { /* same notification */ }
                        }
                    }) { Text("No") }
                }
            )
        }
    }
}

data class Task(
    val id: Int,
    val title: String,
    val description: String = "",
    val isDone: Boolean = false,
    val isTemp: Boolean = false
)


@Composable
fun HomeScreen() {
    Text("Home")
}

@Composable
fun BotScreen() {
    Text("Bot")
}


@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun TaskScreen(
    taskViewModel: TaskViewModel,
    onTaskClick: (Int) -> Unit,
    onEditTask: (Int) -> Unit,
    editingTaskId: Int?,
    onEditDone: () -> Unit,
    isCreatingNewTask: Boolean,
    onCreateNewTask: () -> Unit,
    onCancel: () -> Unit,
    onSaveTask: (String, String) -> Unit,
    onDeleteTask: () -> Unit
) {
    val tasks by taskViewModel.taskList.collectAsState()
    val listState = rememberLazyListState()
    var editTitle by remember { mutableStateOf("") }
    var editDesc by remember { mutableStateOf("") }
    var confirmDeleteTaskId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(editingTaskId, isCreatingNewTask) {
        if (isCreatingNewTask) {
            editTitle = ""
            editDesc = ""
        } else {
            editingTaskId?.let { id ->
                tasks.find { it.id == id }?.let { task ->
                    editTitle = task.title
                    editDesc = task.description
                }
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            MediumFloatingActionButton(onClick = onCreateNewTask) {
                Icon(Icons.Filled.Add, contentDescription = "Add Task")
            }
        }
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tasks.reversed(), key = { it.id }) { task ->
                    ListItem(
                        headlineContent = { Text(task.title) },
                        supportingContent = { if (task.description.isNotBlank()) Text(task.description) },
                        leadingContent = {
                            Checkbox(
                                checked = task.isDone,
                                onCheckedChange = { checked ->
                                    taskViewModel.toggleTaskDone(task.id)
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { onTaskClick(task.id) },
                                onLongClick = { onEditTask(task.id) }
                            )
                    )
                    HorizontalDivider()
                }
            }
        }

        if (editingTaskId != null || isCreatingNewTask) {
            BasicAlertDialog(
                onDismissRequest = onCancel,
                content = {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        tonalElevation = 6.dp,
                        modifier = Modifier
                            .wrapContentHeight()
                            .fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text(
                                text = if (isCreatingNewTask) "New Task" else "Edit Task",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Spacer(Modifier.height(16.dp))
                            OutlinedTextField(
                                value = editTitle,
                                onValueChange = { editTitle = it },
                                label = { Text("Title") },
                                singleLine = true
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = editDesc,
                                onValueChange = { editDesc = it },
                                label = { Text("Description") }
                            )
                            Spacer(Modifier.height(24.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!isCreatingNewTask) {
                                    TextButton(onClick = onDeleteTask) {
                                        Text("Delete", color = MaterialTheme.colorScheme.error)
                                    }
                                } else {
                                    Spacer(Modifier)
                                }
                                Row {
                                    TextButton(onClick = onCancel) { Text("Cancel") }
                                    Spacer(Modifier.width(8.dp))
                                    TextButton(onClick = {
                                        onSaveTask(
                                            editTitle,
                                            editDesc
                                        )
                                    }) { Text("Save") }
                                }
                            }
                        }
                    }
                }
            )
        }

        if (confirmDeleteTaskId != null) {
            AlertDialog(
                onDismissRequest = { confirmDeleteTaskId = null },
                title = { Text("Delete Task") },
                text = { Text("Are you sure you want to delete this task? This cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = {
                        taskViewModel.removeTask(confirmDeleteTaskId!!)
                        confirmDeleteTaskId = null
                        onCancel()
                    }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    TextButton(onClick = {
                        confirmDeleteTaskId = null
                    }) { Text("Cancel") }
                }
            )
        }
    }
}


// Helpers
fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val m = totalSec / 60
    val s = totalSec % 60
    return "%02d:%02d".format(m, s)
}
