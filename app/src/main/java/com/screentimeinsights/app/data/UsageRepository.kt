package com.screentimeinsights.app.data

import com.screentimeinsights.app.domain.UsageRecord
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/** The boundary between usage collection/storage and the rest of the application. */
interface UsageRepository {
    fun observeUsage(start: Instant, end: Instant): Flow<List<UsageRecord>>
    suspend fun refreshUsage(start: Instant, end: Instant)
}
