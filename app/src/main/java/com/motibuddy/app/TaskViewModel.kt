package com.motibuddy.app

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * Responsibilities:
 * - Store task list (with Flow)
 * - Add / update / delete tasks
 * - Emit the current selected task (for Pomodoro screen to observe)
 */

class TaskViewModel: ViewModel() {


    // List of all tasks
    private val _taskList = MutableStateFlow<List<Task>>(emptyList())
    val taskList: StateFlow<List<Task>> = _taskList

    // DEMO DATA
    init {
        _taskList.value = listOf(
            Task(
                id = 1,
                title = "Morning Study Session",
                description = "Review notes or study a topic for at least 25 minutes.",
                isDone = false
            ),
            Task(
                id = 2,
                title = "Work on Personal Project",
                description = "Code or design something for your portfolio or side project.",
                isDone = false
            ),
            Task(
                id = 3,
                title = "Read a Book",
                description = "Read a chapter or section using focused 25-minute Pomodoro.",
                isDone = false
            ),
            Task(
                id = 4,
                title = "Exercise Routine",
                description = "Follow a structured workout or yoga with breaks.",
                isDone = false
            ),
            Task(
                id = 5,
                title = "Evening Digital Cleanup",
                description = "Sort files, plan next day, check tasks for tomorrow.",
                isDone = false
            )
        )
    }


    // Currently selected (focused) task
    private val _currentTask = MutableStateFlow<Task?>(null)
    val currentTask: StateFlow<Task?> = _currentTask

    fun addTask(title: String, description: String = ""): Int {
        val newId = (_taskList.value.maxOfOrNull { it.id } ?: 0) + 1
        val newTask = Task(newId, title, description)
        _taskList.update { it + newTask }
        return newId
    }


    fun setCurrentTask(taskId: Int) {
        _currentTask.value = _taskList.value.find { it.id == taskId }
    }

    fun toggleTaskDone(taskId: Int) {
        // First, flip it in the list
        _taskList.update { list ->
            list.map {
                if (it.id == taskId) it.copy(isDone = !it.isDone) else it
            }
        }
        // Then, if that was the currently‐selected task, update _currentTask to match
        if (_currentTask.value?.id == taskId) {
            _currentTask.value = _currentTask.value?.copy(isDone = !_currentTask.value!!.isDone)
        }
    }


    fun removeTask(taskId: Int) {
        _taskList.value = _taskList.value.filter { it.id != taskId }
        if (_currentTask.value?.id == taskId) {
            _currentTask.value = null
        }
    }
    fun updateTask(id: Int, newTitle: String, newDescription: String) {
        _taskList.update { list ->
            list.map {
                if (it.id == id) it.copy(title = newTitle, description = newDescription)
                else it
            }
        }

        // Update currentTask if it's the one being edited
        if (_currentTask.value?.id == id) {
            _currentTask.value = _currentTask.value?.copy(title = newTitle, description = newDescription)
        }
    }


}

