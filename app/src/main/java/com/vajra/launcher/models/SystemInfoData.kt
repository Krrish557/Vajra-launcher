package com.vajra.launcher.models

data class SystemMetric(
    val label: String,
    val value: String
)

data class SystemSection(
    val title: String,
    val metrics: List<SystemMetric>
)
