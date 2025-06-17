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

    // Currently selected (focused) task
    private val _currentTask = MutableStateFlow<Task?>(null)
    val currentTask: StateFlow<Task?> = _currentTask

    fun addTask(title: String, description: String = "") {
        val newTask = Task(
            id = (_taskList.value.maxOfOrNull { it.id } ?: 0) + 1,
            title = title,
            description = description,
            isDone = false
        )
        _taskList.update { it + newTask }
    }

    fun setCurrentTask(taskId: Int) {
        _currentTask.value = _taskList.value.find { it.id == taskId }
    }

    fun toggleTaskDone(taskId: Int) {
        _taskList.update { list ->
            list.map {
                if (it.id == taskId) it.copy(isDone = !it.isDone) else it
            }
        }
    }
}

