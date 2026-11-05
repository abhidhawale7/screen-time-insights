package com.screentimeinsights.app

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.screentimeinsights.app.usage.UsageAccess
import com.screentimeinsights.app.domain.UsageRecord
import com.screentimeinsights.app.ui.UsageDashboardState
import com.screentimeinsights.app.ui.UsageDashboardViewModel
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private var usageAccessGranted by mutableStateOf(false)
    private lateinit var dashboardViewModel: UsageDashboardViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dashboardViewModel = ViewModelProvider(this)[UsageDashboardViewModel::class.java]
        setContent {
            val dashboardState by dashboardViewModel.state.collectAsStateWithLifecycle()
            ScreenTimeInsightsApp(
                usageAccessGranted = usageAccessGranted,
                dashboardState = dashboardState,
                onRequestUsageAccess = {
                    startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                },
            )
        }
    }

    override fun onResume() {
        super.onResume()
        usageAccessGranted = UsageAccess.isGranted(this)
        if (usageAccessGranted) dashboardViewModel.loadToday()
    }
}

@Composable
private fun ScreenTimeInsightsApp(
    usageAccessGranted: Boolean,
    dashboardState: UsageDashboardState,
    onRequestUsageAccess: () -> Unit,
) {
    MaterialTheme {
        Surface {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Screen Time Insights", style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = if (usageAccessGranted) {
                        "Usage Access is enabled. The app is ready to collect usage data."
                    } else {
                        "Usage Access is required to show your app usage. Your data stays on this device."
                    },
                    modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
                )
                if (!usageAccessGranted) {
                    Button(onClick = onRequestUsageAccess) {
                        Text("Grant Usage Access")
                    }
                } else {
                    UsageDashboardContent(state = dashboardState)
                }
            }
        }
    }
}

@Composable
private fun UsageDashboardContent(state: UsageDashboardState) {
    when (state) {
        UsageDashboardState.Idle -> Text("Preparing today's usage…")
        UsageDashboardState.Loading -> {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CircularProgressIndicator()
                Text("Reading today's usage…")
            }
        }

        is UsageDashboardState.Error -> Text("Could not load usage: ${state.message}")
        is UsageDashboardState.Loaded -> {
            val usageByApp = state.records
                .groupBy(UsageRecord::packageName)
                .mapValues { (_, records) -> records.sumOf(UsageRecord::durationMillis) }
                .entries
                .sortedByDescending(Map.Entry<String, Long>::value)
            val totalUsage = state.records.sumOf(UsageRecord::durationMillis)

            Text(
                text = "Today's usage: ${formatDuration(totalUsage)}",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 24.dp, bottom = 16.dp),
            )
            if (usageByApp.isEmpty()) {
                Text("No app usage has been recorded yet today.")
            } else {
                Text("Most-used apps", style = MaterialTheme.typography.titleMedium)
                usageByApp.take(5).forEach { (packageName, duration) ->
                    val appName = state.records.first { it.packageName == packageName }.appName
                    Text(
                        text = "$appName — ${formatDuration(duration)}",
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
    }
}

private fun formatDuration(durationMillis: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis)
    val hours = minutes / 60
    val remainingMinutes = minutes % 60
    return if (hours > 0) "${hours}h ${remainingMinutes}m" else "${remainingMinutes}m"
}
