package com.screentimeinsights.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.screentimeinsights.app.domain.UsageRecord
import com.screentimeinsights.app.usage.AndroidUsageCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

sealed interface UsageDashboardState {
    data object Idle : UsageDashboardState
    data object Loading : UsageDashboardState
    data class Loaded(val records: List<UsageRecord>) : UsageDashboardState
    data class Error(val message: String) : UsageDashboardState
}

class UsageDashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val collector = AndroidUsageCollector(application)
    private val _state = MutableStateFlow<UsageDashboardState>(UsageDashboardState.Idle)
    val state: StateFlow<UsageDashboardState> = _state.asStateFlow()

    fun loadToday() {
        if (_state.value is UsageDashboardState.Loading) return

        viewModelScope.launch {
            _state.value = UsageDashboardState.Loading
            try {
                val zone = ZoneId.systemDefault()
                val start = LocalDate.now(zone).atStartOfDay(zone).toInstant()
                val end = Instant.now()
                _state.value = UsageDashboardState.Loaded(collector.collect(start, end))
            } catch (exception: Exception) {
                _state.value = UsageDashboardState.Error(
                    exception.message ?: "Unable to read usage data",
                )
            }
        }
    }
}
