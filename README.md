# Screen Time Insights

Privacy-first Android usage dashboard.

## Current status

Milestone 2 is in progress. The repository contains:

- A Kotlin + Jetpack Compose Android application skeleton.
- Package ID `com.screentimeinsights.app`.
- Minimum Android version API 29 (Android 10).
- Initial boundaries for usage collection, data access, domain records, and analytics.
- Architecture documentation in [ARCHITECTURE.md](./ARCHITECTURE.md).
- Product scope in [PRODUCT_REQUIREMENTS.md](./PRODUCT_REQUIREMENTS.md).

## Open the project

Open this folder in Android Studio and allow Gradle sync to download the configured dependencies. The local environment currently has Java 17, but Android SDK and Gradle command-line tools are not configured, so the project has not been built in this workspace yet.

The first implementation slice after sync should be:

1. Add the Usage Access onboarding screen.
2. Implement the `UsageCollector` against `UsageStatsManager`.
3. Add Room entities and a repository implementation.
4. Add unit tests for interval normalization and aggregation.
