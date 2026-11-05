# Screen Time Insights — MVP Product Requirements

## 1. Product definition

Screen Time Insights is a privacy-first Android application that helps users understand how they spend time on their phone.

The MVP answers:

> How much time did I spend on my phone, which apps did I use, and how is that changing over time?

The product should present usage data clearly enough for a user to understand their phone habits within a few seconds of opening the app.

## 2. Target user

The initial user is a person who wants to understand and improve their phone usage without creating an account or sending personal usage data to a server.

## 3. MVP goals

- Collect reliable app-usage data from Android.
- Store the data locally on the device.
- Show total usage for a selected day.
- Show usage broken down by app.
- Show the most-used apps.
- Show usage by hour.
- Show daily and weekly trends.
- Compare the current period with the previous equivalent period.
- Show sessions and unlock counts where Android provides reliable data.
- Make the data understandable without requiring technical knowledge.

## 4. MVP non-goals

The MVP will not include:

- Cloud synchronization or user accounts.
- Social sharing or public profiles.
- Advertising or third-party analytics SDKs.
- AI-generated recommendations.
- Custom app limits or blocking.
- User-defined goals and reminders.
- App categories and category-level insights.
- Notification-content collection.

Categories and insights are planned for a later milestone, after the usage data and aggregation are trustworthy.

## 5. Functional requirements

### 5.1 Onboarding and permission

- Explain what data the app reads and why.
- Guide the user to Android's Usage Access settings.
- Detect whether Usage Access is granted.
- Provide a clear recovery path when permission is missing or revoked.
- Do not treat missing permission as an application crash or an unexplained empty dashboard.

### 5.2 Usage collection

- Use Android's `UsageStatsManager` as the primary data source.
- Collect app package name, display name, start time, end time, and duration where available.
- Normalize records into a consistent internal format.
- Collect foreground/session events where available.
- Collect unlock counts only where the Android version and available events support them.
- Clearly distinguish unavailable data from a measured value.
- Avoid collecting contacts, messages, photos, location, microphone data, or notification contents.

### 5.3 Dashboard

The home screen must show:

- Total phone usage for the selected day.
- Change compared with the previous day.
- Top-used apps with duration.
- Usage by hour.
- Session count when available.
- Unlock count when available.
- A link to the app breakdown and trends.

The dashboard must support these initial states:

- First use with no collected data.
- Usage Access not granted.
- Partial or unavailable metrics.
- A normal day with collected data.

### 5.4 App usage breakdown

- List apps ordered by usage duration.
- Show each app's name, icon where available, duration, and share of total usage.
- Allow the user to select an app for details.
- Support at least today, yesterday, and the last seven days as date ranges.

### 5.5 App details

For a selected app, show:

- Usage for the selected day.
- Usage for the previous equivalent period.
- Usage for the last seven days.
- A simple daily usage chart.
- Session count where available.

### 5.6 Trends and comparison

- Show daily totals for the last seven days.
- Show the current week compared with the previous week when enough data exists.
- Calculate absolute and percentage change.
- Avoid misleading percentage changes when the comparison period is zero or incomplete.
- Use the device's local date and time zone for day boundaries.

### 5.7 Local data

- Store normalized usage records locally.
- Retain usage history indefinitely by default.
- Derive daily, app, hourly, and trend summaries from stored records.
- Keep data collection separate from aggregation so summaries can be recalculated.
- Provide a future-compatible path for data export and deletion, even if the UI is not part of the MVP.

## 6. Privacy requirements

- No account is required.
- No usage data leaves the device in the MVP.
- No server or cloud database is required.
- No advertising or analytics SDKs are included.
- Request only the permissions required for usage statistics.
- Explain the privacy model during onboarding and in settings.

## 7. Product behavior and quality bar

- The dashboard should be understandable within 3–5 seconds.
- Usage totals must not double-count overlapping records.
- Records crossing midnight must be split correctly between dates.
- The app must handle missing, delayed, or incomplete usage events gracefully.
- The app should not consume enough battery to become a meaningful source of phone usage.
- The UI must remain useful when only partial metrics are available.

## 8. MVP screens

1. **Onboarding / Usage Access** — Explains privacy and obtains permission.
2. **Today Dashboard** — Total usage, comparison, top apps, hourly usage, sessions, and unlocks.
3. **App Usage List** — Usage ranking for the selected range.
4. **App Details** — Historical usage and trend for one app.
5. **Trends** — Daily and weekly usage comparisons.
6. **Settings** — Permission status, privacy information, and future data-management entry points.

## 9. Success criteria

Milestone 1 is complete when:

- The MVP scope is fixed as documented here.
- The app has a clear privacy and permission model.
- Each MVP screen has a defined purpose.
- The collected data fields and unavailable-data behavior are defined.
- The MVP can be evaluated on a real Android device using real usage data.

## 10. Decisions for later milestones

The following decisions are intentionally deferred:

- Minimum Android version and target SDK.
- Exact app package/application ID.
- App categorization rules.
- Goal and limit behavior.
- Whether exported data should use CSV, JSON, or another format.
- Whether optional encrypted backup should ever be added.
