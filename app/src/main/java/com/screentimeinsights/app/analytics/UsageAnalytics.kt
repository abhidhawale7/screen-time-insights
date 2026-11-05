package com.screentimeinsights.app.analytics

import com.screentimeinsights.app.domain.UsageRecord
import java.time.LocalDate

/** Pure business logic for deriving dashboard values from normalized records. */
interface UsageAnalytics {
    fun totalUsage(records: List<UsageRecord>): Long
    fun usageByApp(records: List<UsageRecord>): Map<String, Long>
    fun usageByHour(records: List<UsageRecord>, date: LocalDate): Map<Int, Long>
}
