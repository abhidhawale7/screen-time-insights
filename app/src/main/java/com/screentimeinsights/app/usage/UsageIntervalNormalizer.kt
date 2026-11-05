package com.screentimeinsights.app.usage

import com.screentimeinsights.app.domain.UsageEventType
import com.screentimeinsights.app.domain.UsageRecord
import java.time.Instant

class UsageIntervalNormalizer {
    fun normalize(
        events: List<UsageEvent>,
        rangeStartMillis: Long,
        rangeEndMillis: Long,
        appName: (String) -> String,
    ): List<UsageRecord> {
        require(rangeStartMillis <= rangeEndMillis) { "The usage range must be ordered" }

        val activeStarts = mutableMapOf<String, Long>()
        val records = mutableListOf<UsageRecord>()

        fun close(packageName: String, endMillis: Long) {
            val startMillis = activeStarts.remove(packageName) ?: return
            val clampedStart = startMillis.coerceIn(rangeStartMillis, rangeEndMillis)
            val clampedEnd = endMillis.coerceIn(rangeStartMillis, rangeEndMillis)
            if (clampedEnd <= clampedStart) return

            records += UsageRecord(
                packageName = packageName,
                appName = appName(packageName),
                startTime = Instant.ofEpochMilli(clampedStart),
                endTime = Instant.ofEpochMilli(clampedEnd),
                durationMillis = clampedEnd - clampedStart,
                eventType = UsageEventType.FOREGROUND_SESSION,
                sessionId = "$packageName:$clampedStart",
            )
        }

        events
            .asSequence()
            .filter { it.timestampMillis in rangeStartMillis..rangeEndMillis }
            .sortedBy { it.timestampMillis }
            .forEach { event ->
                when (event.type) {
                    UsageEvent.Type.FOREGROUND -> {
                        if (activeStarts.containsKey(event.packageName)) {
                            close(event.packageName, event.timestampMillis)
                        }
                        activeStarts[event.packageName] = event.timestampMillis
                    }

                    UsageEvent.Type.BACKGROUND -> {
                        close(event.packageName, event.timestampMillis)
                    }
                }
            }

        activeStarts.keys.toList().forEach { packageName ->
            close(packageName, rangeEndMillis)
        }

        return records.sortedWith(compareBy<UsageRecord> { it.startTime }.thenBy { it.packageName })
    }
}
