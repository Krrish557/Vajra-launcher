package com.vajra.launcher.data.executor

import java.util.regex.Pattern

object TargetValidator {

    private val IPV4_PATTERN = Pattern.compile(
        "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    )

    private val HOSTNAME_PATTERN = Pattern.compile(
        "^([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)*[a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?$"
    )

    private val DISALLOWED_CHARS = Pattern.compile("[\\s;&|`$<>()\\[\\]{}\\\\\"\'!*?~#]")

    fun isValid(target: String): Boolean {
        val trimmed = target.trim()
        if (trimmed.isEmpty() || trimmed.length > 255) return false
        if (DISALLOWED_CHARS.matcher(trimmed).find()) return false

        if (trimmed.equals("localhost", ignoreCase = true)) return true
        if (IPV4_PATTERN.matcher(trimmed).matches()) return true
        if (HOSTNAME_PATTERN.matcher(trimmed).matches()) return true
        if (trimmed == "::1" || (trimmed.contains(":") && !trimmed.contains(" "))) return true

        return false
    }

    fun sanitize(target: String): String {
        return target.trim()
    }
}
