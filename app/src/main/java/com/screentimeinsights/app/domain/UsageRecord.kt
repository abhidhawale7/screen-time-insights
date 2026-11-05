package com.screentimeinsights.app.domain

import java.time.Instant

/** Normalized, local representation of one app-usage interval. */
data class UsageRecord(
    val packageName: String,
    val appName: String,
    val startTime: Instant,
    val endTime: Instant,
    val durationMillis: Long,
    val eventType: UsageEventType = UsageEventType.FOREGROUND_SESSION,
    val sessionId: String? = null,
)

enum class UsageEventType {
    FOREGROUND_SESSION,
}
