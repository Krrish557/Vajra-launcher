package com.vajra.launcher.data.executor

import com.vajra.launcher.data.environment.EnvironmentRouter
import com.vajra.launcher.models.ExecutionResult
import com.vajra.launcher.models.JobStatus
import com.vajra.launcher.models.ToolExecutionJob
import com.vajra.launcher.models.ToolExecutionRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

class ToolExecutor(
    private val router: EnvironmentRouter
) {

    private val mutex = Mutex()
    private val _currentJob = MutableStateFlow<ToolExecutionJob?>(null)
    val currentJob: StateFlow<ToolExecutionJob?> = _currentJob.asStateFlow()

    fun isBusy(): Boolean {
        val job = _currentJob.value
        return job?.status == JobStatus.RUNNING || job?.status == JobStatus.QUEUED
    }

    suspend fun execute(request: ToolExecutionRequest): ExecutionResult {
        mutex.withLock {
            if (isBusy()) {
                val activeJob = _currentJob.value
                return ExecutionResult(
                    id = UUID.randomUUID().toString(),
                    toolId = request.toolId,
                    actionId = request.actionId,
                    target = request.target,
                    environment = com.vajra.launcher.models.EnvironmentType.UNKNOWN,
                    success = false,
                    exitCode = -1,
                    stdout = "",
                    stderr = "Execution rejected: Tool '${activeJob?.toolId}' is currently running.",
                    durationMs = 0L
                )
            }

            val jobId = UUID.randomUUID().toString()
            val startTime = System.currentTimeMillis()

            val initialJob = ToolExecutionJob(
                id = jobId,
                toolId = request.toolId,
                actionId = request.actionId,
                target = request.target,
                status = JobStatus.RUNNING,
                startedAt = startTime
            )
            _currentJob.value = initialJob

            try {
                val result = router.routeAndExecute(request)
                val finishedTime = System.currentTimeMillis()

                val finalStatus = if (result.success) JobStatus.COMPLETED else JobStatus.FAILED
                _currentJob.value = initialJob.copy(
                    status = finalStatus,
                    finishedAt = finishedTime,
                    result = result,
                    errorMessage = if (!result.success) result.stderr else null
                )

                return result
            } catch (e: Exception) {
                val finishedTime = System.currentTimeMillis()
                val errResult = ExecutionResult(
                    id = jobId,
                    toolId = request.toolId,
                    actionId = request.actionId,
                    target = request.target,
                    environment = com.vajra.launcher.models.EnvironmentType.UNKNOWN,
                    success = false,
                    exitCode = -1,
                    stdout = "",
                    stderr = "Execution error: ${e.localizedMessage ?: "Unexpected error"}",
                    durationMs = finishedTime - startTime
                )

                _currentJob.value = initialJob.copy(
                    status = JobStatus.FAILED,
                    finishedAt = finishedTime,
                    result = errResult,
                    errorMessage = e.localizedMessage
                )

                return errResult
            }
        }
    }

    fun clearJob() {
        _currentJob.value = null
    }
}
