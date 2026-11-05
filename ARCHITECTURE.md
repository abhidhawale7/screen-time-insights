# Screen Time Insights — Technical Architecture

## 1. Architecture decision

The MVP starts as one Android application module with explicit package boundaries. This keeps setup and iteration simple while preventing Android framework code, storage, analytics, and UI from becoming tightly coupled.

The dependency direction is:

```text
UI → ViewModel → Analytics / Repository interfaces → Data implementations → Android APIs / Room
```

The domain layer remains independent of Android APIs wherever possible.

## 2. Planned package structure

```text
com.screentimeinsights.app
├── analytics/       Derived totals, trends, comparisons, and later insights
├── data/            Repository implementations and database entities/DAOs
├── domain/          Normalized records and business models
├── ui/              Compose screens, navigation, theme, and UI state
├── usage/           UsageStatsManager integration and event normalization
└── MainActivity.kt  Application entry point
```

## 3. Runtime data flow

```text
Android UsageStatsManager
        ↓
UsageCollector
        ↓ normalize and split intervals
UsageRepository
        ↓ persist locally
Room database
        ↓ query raw records
UsageAnalytics
        ↓ calculate dashboard state
ViewModel
        ↓ expose StateFlow
Jetpack Compose UI
```

## 4. Core boundaries

### Usage collector

`UsageCollector` is the only component that talks directly to Android usage APIs. It is responsible for:

- Checking the requested time range.
- Reading usage statistics/events.
- Resolving app metadata through `PackageManager`.
- Normalizing records.
- Preventing invalid or negative intervals.
- Splitting intervals at local midnight when required.

### Repository

`UsageRepository` coordinates collection and local persistence. The UI and analytics code must not call `UsageStatsManager` directly.

### Analytics

`UsageAnalytics` contains deterministic, testable calculations for:

- Total duration.
- App totals.
- Hourly totals.
- Daily totals.
- Previous-period comparisons.
- Session and unlock availability.

### UI

Compose screens consume immutable UI state from ViewModels. UI code should not contain date arithmetic, aggregation, or permission logic.

## 5. Initial data model

The first persistent model will include:

```text
UsageRecord
├── id
├── packageName
├── appName
├── startTime
├── endTime
├── durationMillis
├── eventType
└── sessionId (nullable)

AppMetadata
├── packageName
├── displayName
└── icon reference (nullable)
```

Daily and hourly summaries should be derived from records initially. Materialized summaries can be added only if profiling shows that they are needed.

## 6. Permission and unavailable data model

Usage Access is a user-controlled special access permission, not a normal runtime permission. The application should model permission state explicitly:

```text
UsageAccessState
├── Granted
└── NotGranted
```

Metrics that Android cannot provide should be represented as unavailable rather than zero. This prevents the dashboard from claiming that the user had zero sessions or unlocks when the device simply did not expose the data.

## 7. Initial screen navigation

```text
Onboarding / Usage Access
            ↓ permission granted
Today Dashboard
       ├── App Usage List
       │       └── App Details
       ├── Trends
       └── Settings
```

The first implementation can use a simple screen state in `MainActivity`; navigation will be introduced with the first multi-screen slice.

## 8. Build decisions

- Application ID: `com.screentimeinsights.app`
- Minimum SDK: 29 (Android 10)
- Target SDK: 36
- Java/Kotlin JVM target: 17
- UI: Jetpack Compose
- Persistence: Room, to be added with the data-layer implementation
- Background work: WorkManager, to be added after the first collector works
- Network: none in the MVP

## 9. Architectural risks

- Android usage events may be incomplete or differ by device/vendor.
- Unlock information may not be available consistently across Android versions.
- Usage intervals can overlap and cross midnight.
- Usage Access can be revoked at any time.
- Long-term local history may require indexing and retention-management decisions later.

These risks are why collection and analytics are separate interfaces and why the first technical milestone should validate real device data before building advanced UI.
