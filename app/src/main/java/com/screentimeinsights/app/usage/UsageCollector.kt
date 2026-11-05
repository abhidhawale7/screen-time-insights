package com.screentimeinsights.app.usage

import com.screentimeinsights.app.domain.UsageRecord
import java.time.Instant

/** Reads Android usage APIs and converts their events into normalized records. */
interface UsageCollector {
    suspend fun collect(start: Instant, end: Instant): List<UsageRecord>
}
