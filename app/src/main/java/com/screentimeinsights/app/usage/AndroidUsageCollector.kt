package com.screentimeinsights.app.usage

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import com.screentimeinsights.app.domain.UsageRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant

class AndroidUsageCollector(context: Context) : UsageCollector {
    private val appContext = context.applicationContext
    private val usageStatsManager =
        appContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val packageManager = appContext.packageManager
    private val normalizer = UsageIntervalNormalizer()

    override suspend fun collect(start: Instant, end: Instant): List<UsageRecord> =
        withContext(Dispatchers.Default) {
            require(!end.isBefore(start)) { "The usage range must be ordered" }
            if (!UsageAccess.isGranted(appContext) || start == end) return@withContext emptyList()

            val startMillis = start.toEpochMilli()
            val endMillis = end.toEpochMilli()
            val events = usageStatsManager.queryEvents(startMillis, endMillis)
            val normalizedEvents = mutableListOf<UsageEvent>()
            val event = UsageEvents.Event()

            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                val type = when (event.eventType) {
                    UsageEvents.Event.MOVE_TO_FOREGROUND -> UsageEvent.Type.FOREGROUND
                    UsageEvents.Event.MOVE_TO_BACKGROUND -> UsageEvent.Type.BACKGROUND
                    else -> null
                }
                if (type != null && !event.packageName.isNullOrBlank()) {
                    normalizedEvents += UsageEvent(
                        packageName = event.packageName,
                        type = type,
                        timestampMillis = event.timeStamp,
                    )
                }
            }

            normalizer.normalize(
                events = normalizedEvents,
                rangeStartMillis = startMillis,
                rangeEndMillis = endMillis,
                appName = ::resolveAppName,
            )
        }

    private fun resolveAppName(packageName: String): String =
        knownAppNames[packageName]
            ?: runCatching {
                val applicationInfo: ApplicationInfo =
                    packageManager.getApplicationInfo(packageName, 0)
                packageManager.getApplicationLabel(applicationInfo)
                    .toString()
                    .takeIf { it.isNotBlank() && it != packageName }
            }.getOrNull()
            ?: readablePackageName(packageName)

    private fun readablePackageName(packageName: String): String =
        packageName
            .substringAfterLast('.')
            .replace('-', ' ')
            .replace('_', ' ')
            .split(' ')
            .filter(String::isNotBlank)
            .joinToString(" ") { word ->
                word.replaceFirstChar { character -> character.uppercase() }
            }
            .ifBlank { packageName }

    private companion object {
        val knownAppNames = mapOf(
            "com.google.android.gm" to "Gmail",
            "com.google.android.apps.nexuslauncher" to "Nexus Launcher",
            "com.android.chrome" to "Chrome",
            "com.android.vending" to "Google Play Store",
        )
    }
}
