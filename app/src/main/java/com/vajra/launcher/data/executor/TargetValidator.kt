package com.vajra.launcher.data.executor

import java.util.regex.Pattern

/**
 * Validates and sanitizes target inputs to strictly prevent command injection,
 * shell escape sequences, and parameter tampering.
 */
object TargetValidator {

    private val IPV4_CIDR_PATTERN = Pattern.compile(
        "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(/(3[0-2]|[12]?[0-9]))?$"
    )

    private val HOSTNAME_PATTERN = Pattern.compile(
        "^([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)*[a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?$"
    )

    // Comprehensive blacklist of shell metacharacters, control characters, redirection, and whitespace
    private val DISALLOWED_CHARS = Pattern.compile("[\\s;&|`$<>()\\[\\]{}\\\\\"\'!*?~#%^=+,\\x00-\\x1F]")

    private val IPV6_CHAR_PATTERN = Pattern.compile("^[0-9a-fA-F:]+(/(12[0-8]|1[0-1][0-9]|[1-9]?[0-9]))?$")

    fun isValid(target: String): Boolean {
        val trimmed = target.trim()
        if (trimmed.isEmpty() || trimmed.length > 255) return false
        if (DISALLOWED_CHARS.matcher(trimmed).find()) return false

        if (trimmed.equals("localhost", ignoreCase = true)) return true

        // If target contains only digits, dots, and optional slash, it is an IP/CIDR candidate
        val isNumericDotted = trimmed.contains(".") && trimmed.replace(".", "").replace("/", "").all { it.isDigit() }
        if (isNumericDotted) {
            return IPV4_CIDR_PATTERN.matcher(trimmed).matches()
        }

        if (IPV4_CIDR_PATTERN.matcher(trimmed).matches()) return true
        if (isValidIpv6(trimmed)) return true

        // For hostnames, TLD (if dotted) must not be purely numeric
        if (HOSTNAME_PATTERN.matcher(trimmed).matches()) {
            if (trimmed.contains(".")) {
                val tld = trimmed.substringAfterLast(".")
                return !tld.all { it.isDigit() }
            }
            return true
        }

        return false
    }

    private fun isValidIpv6(target: String): Boolean {
        if (!target.contains(":")) return false
        if (!IPV6_CHAR_PATTERN.matcher(target).matches()) return false
        if (target.contains(":::")) return false

        val base = if (target.contains("/")) target.substringBefore("/") else target
        val colonCount = base.count { it == ':' }
        if (colonCount < 2 || colonCount > 7) return false

        val doubleColonIndex = base.indexOf("::")
        if (doubleColonIndex != -1 && base.indexOf("::", doubleColonIndex + 1) != -1) {
            return false // Multiple "::"
        }

        val parts = base.split(":")
        for (part in parts) {
            if (part.length > 4) return false
        }
        return true
    }

    fun sanitize(target: String): String {
        return target.trim()
    }
}

