package com.vajra.launcher.data.environment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class TermuxResultReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_TERMUX_RESULT = "com.vajra.launcher.ACTION_TERMUX_RESULT"
        const val EXTRA_EXECUTION_ID = "execution_id"

        // Termux RunCommandService result extras
        const val EXTRA_STDOUT = "stdout"
        const val EXTRA_STDERR = "stderr"
        const val EXTRA_EXIT_CODE = "exitCode"
        const val EXTRA_ERR_CODE = "errCode"
        const val EXTRA_ERRMSG = "errmsg"

        private const val TAG = "TermuxResultReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) return
        val executionId = intent.getStringExtra(EXTRA_EXECUTION_ID) ?: return

        val resultBundle = intent.getBundleExtra("result")
            ?: intent.getBundleExtra("resultBundle")
            ?: intent.extras?.getBundle("result")
            ?: intent.extras?.getBundle("resultBundle")
        val bundle = resultBundle ?: intent.extras

        Log.d(TAG, "Received broadcast for id=$executionId")
        Log.d(TAG, "Intent extras keys: ${intent.extras?.keySet()}")
        Log.d(TAG, "resultBundle keys: ${resultBundle?.keySet()}")

        val stdout = bundle?.getString(EXTRA_STDOUT)
            ?: bundle?.getString("com.termux.RUN_COMMAND_RESULT_STDOUT")
            ?: intent.getStringExtra(EXTRA_STDOUT)
            ?: ""

        val stderr = bundle?.getString(EXTRA_STDERR)
            ?: bundle?.getString("com.termux.RUN_COMMAND_RESULT_STDERR")
            ?: intent.getStringExtra(EXTRA_STDERR)
            ?: ""

        val hasExitCode = bundle?.containsKey(EXTRA_EXIT_CODE) == true ||
                bundle?.containsKey("com.termux.RUN_COMMAND_RESULT_EXIT_CODE") == true ||
                intent.hasExtra(EXTRA_EXIT_CODE)

        val exitCode = when {
            bundle?.containsKey(EXTRA_EXIT_CODE) == true -> bundle.getInt(EXTRA_EXIT_CODE)
            bundle?.containsKey("com.termux.RUN_COMMAND_RESULT_EXIT_CODE") == true -> bundle.getInt("com.termux.RUN_COMMAND_RESULT_EXIT_CODE")
            intent.hasExtra(EXTRA_EXIT_CODE) -> intent.getIntExtra(EXTRA_EXIT_CODE, 0)
            else -> 0
        }

        val errCode = when {
            bundle?.containsKey("err") == true -> bundle.getInt("err")
            bundle?.containsKey(EXTRA_ERR_CODE) == true -> bundle.getInt(EXTRA_ERR_CODE)
            bundle?.containsKey("com.termux.RUN_COMMAND_RESULT_ERR_CODE") == true -> bundle.getInt("com.termux.RUN_COMMAND_RESULT_ERR_CODE")
            intent.hasExtra("err") -> intent.getIntExtra("err", 0)
            intent.hasExtra(EXTRA_ERR_CODE) -> intent.getIntExtra(EXTRA_ERR_CODE, 0)
            else -> 0
        }

        val errmsg = bundle?.getString(EXTRA_ERRMSG)
            ?: bundle?.getString("com.termux.RUN_COMMAND_RESULT_ERRMSG")
            ?: intent.getStringExtra(EXTRA_ERRMSG)

        val finalExitCode = if (hasExitCode) {
            exitCode
        } else if (errCode > 0) {
            errCode
        } else {
            0
        }

        Log.d(TAG, "Extracted exitCode=$exitCode, hasExitCode=$hasExitCode, errCode=$errCode, finalExitCode=$finalExitCode, stdout length=${stdout.length}, stderr length=${stderr.length}")
        if (stdout.isNotEmpty()) {
            Log.d(TAG, "stdout snippet: ${stdout.take(200)}")
        }
        if (stderr.isNotEmpty()) {
            Log.d(TAG, "stderr snippet: ${stderr.take(200)}")
        }

        TermuxBridge.notifyResult(
            executionId = executionId,
            stdout = stdout,
            stderr = if (errCode > 0 && !errmsg.isNullOrEmpty()) "$errmsg\n$stderr".trim() else stderr,
            exitCode = finalExitCode
        )
    }
}
