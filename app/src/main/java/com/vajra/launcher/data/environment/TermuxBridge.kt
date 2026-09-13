package com.vajra.launcher.data.environment

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.vajra.launcher.models.CommunicationStatus
import com.vajra.launcher.models.EnvironmentType
import com.vajra.launcher.models.ExecutionResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class TermuxBridge(private val context: Context) {

    companion object {
        const val TERMUX_PACKAGE = "com.termux"
        const val TERMUX_SERVICE = "com.termux.app.RunCommandService"
        const val ACTION_RUN_COMMAND = "com.termux.RUN_COMMAND"
        const val PERMISSION_RUN_COMMAND = "com.termux.permission.RUN_COMMAND"

        const val EXTRA_PATH = "com.termux.RUN_COMMAND_PATH"
        const val EXTRA_ARGUMENTS = "com.termux.RUN_COMMAND_ARGUMENTS"
        const val EXTRA_WORKDIR = "com.termux.RUN_COMMAND_WORKDIR"
        const val EXTRA_BACKGROUND = "com.termux.RUN_COMMAND_BACKGROUND"
        const val EXTRA_SESSION_ACTION = "com.termux.RUN_COMMAND_SESSION_ACTION"
        const val EXTRA_PENDING_INTENT = "com.termux.RUN_COMMAND_PENDING_INTENT"

        const val DEFAULT_WORKDIR = "/data/data/com.termux/files/home"
        private const val TAG = "TermuxBridge"

        private val pendingExecutions = ConcurrentHashMap<String, CompletableDeferred<RawResult>>()

        data class RawResult(
            val stdout: String,
            val stderr: String,
            val exitCode: Int
        )

        fun notifyResult(executionId: String, stdout: String, stderr: String, exitCode: Int) {
            val deferred = pendingExecutions.remove(executionId)
            if (deferred != null) {
                deferred.complete(RawResult(stdout, stderr, exitCode))
            } else {
                Log.w(TAG, "No pending execution found for id: $executionId")
            }
        }
    }

    fun isTermuxInstalled(): Boolean {
        return try {
            context.packageManager.getPackageInfo(TERMUX_PACKAGE, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        } catch (_: Exception) {
            false
        }
    }

    fun hasRunPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            PERMISSION_RUN_COMMAND
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun checkCommunicationStatus(): CommunicationStatus {
        if (!isTermuxInstalled()) {
            return CommunicationStatus.NOT_INSTALLED
        }
        if (!hasRunPermission()) {
            return CommunicationStatus.NOT_CONFIGURED
        }
        return CommunicationStatus.READY
    }

    suspend fun executeControlled(
        toolId: String,
        actionId: String,
        target: String,
        environment: EnvironmentType,
        executablePath: String,
        arguments: Array<String>,
        workDir: String = DEFAULT_WORKDIR,
        background: Boolean = true,
        timeoutMs: Long = 45000L
    ): ExecutionResult {
        val status = checkCommunicationStatus()
        if (status != CommunicationStatus.READY) {
            return ExecutionResult(
                id = UUID.randomUUID().toString(),
                toolId = toolId,
                actionId = actionId,
                target = target,
                environment = environment,
                success = false,
                exitCode = -1,
                stdout = "",
                stderr = "Termux communication not ready: ${status.label}. Ensure permission $PERMISSION_RUN_COMMAND is granted and allow-external-apps=true in Termux.",
                durationMs = 0L
            )
        }

        val executionId = UUID.randomUUID().toString()
        val deferred = CompletableDeferred<RawResult>()
        pendingExecutions[executionId] = deferred

        val startTime = System.currentTimeMillis()

        return try {
            val intent = Intent().apply {
                setClassName(TERMUX_PACKAGE, TERMUX_SERVICE)
                action = ACTION_RUN_COMMAND
                putExtra(EXTRA_PATH, executablePath)
                putExtra(EXTRA_ARGUMENTS, arguments)
                putExtra(EXTRA_WORKDIR, workDir)
                putExtra(EXTRA_BACKGROUND, background)
                putExtra(EXTRA_SESSION_ACTION, "0")

                val resultIntent = Intent(context, TermuxResultReceiver::class.java).apply {
                    action = TermuxResultReceiver.ACTION_TERMUX_RESULT
                    putExtra(TermuxResultReceiver.EXTRA_EXECUTION_ID, executionId)
                }
                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_MUTABLE
                } else {
                    PendingIntent.FLAG_ONE_SHOT
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    executionId.hashCode(),
                    resultIntent,
                    flags
                )
                putExtra(EXTRA_PENDING_INTENT, pendingIntent)
            }

            context.startService(intent)

            val rawResult = withTimeoutOrNull(timeoutMs) {
                deferred.await()
            }

            val durationMs = System.currentTimeMillis() - startTime

            if (rawResult == null) {
                pendingExecutions.remove(executionId)
                ExecutionResult(
                    id = executionId,
                    toolId = toolId,
                    actionId = actionId,
                    target = target,
                    environment = environment,
                    success = false,
                    exitCode = -1,
                    stdout = "",
                    stderr = "Execution timed out after ${timeoutMs / 1000}s.",
                    durationMs = durationMs
                )
            } else {
                ExecutionResult(
                    id = executionId,
                    toolId = toolId,
                    actionId = actionId,
                    target = target,
                    environment = environment,
                    success = (rawResult.exitCode == 0),
                    exitCode = rawResult.exitCode,
                    stdout = rawResult.stdout,
                    stderr = rawResult.stderr,
                    durationMs = durationMs
                )
            }
        } catch (e: Exception) {
            pendingExecutions.remove(executionId)
            val durationMs = System.currentTimeMillis() - startTime
            ExecutionResult(
                id = executionId,
                toolId = toolId,
                actionId = actionId,
                target = target,
                environment = environment,
                success = false,
                exitCode = -1,
                stdout = "",
                stderr = "Failed to dispatch command to Termux: ${e.localizedMessage ?: "Unknown error"}",
                durationMs = durationMs
            )
        }
    }
}
