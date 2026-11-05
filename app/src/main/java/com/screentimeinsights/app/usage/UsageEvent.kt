package com.screentimeinsights.app.usage

data class UsageEvent(
    val packageName: String,
    val type: Type,
    val timestampMillis: Long,
) {
    enum class Type {
        FOREGROUND,
        BACKGROUND,
    }
}
