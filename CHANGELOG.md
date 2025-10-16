# Changelog

This document logs the history of major improvements, bug fixes, and refactoring efforts for the Pwnagotchi Android application.

## Feature Enhancements & Core Improvements

-   **oPwngrid Integration:**
    -   Added a field in the Settings screen for users to enter their oPwngrid API key.
    -   Created `OpwngridViewModel` to manage the state of the oPwngrid feature, including loading and error states.
    -   The `OpwngridScreen` now displays a loading indicator while fetching the leaderboard.

-   **Simplified Settings:**
    -   Removed the redundant `ip_address` from `SharedPreferences` and the `onSaveSettings` function to streamline the settings. The app now exclusively uses the `host` for WebSocket connections.

-   **Simplified Reconnection Logic:**
    -   Refactored `PwnagotchiService` to simplify the reconnection mechanism by removing the `needsReconnect` flag.

## Home Screen Widget Refactoring (Jetpack Glance)

-   **Architecture:**
    -   Added Jetpack Glance dependencies.
    -   Implemented a `WidgetStateRepository` with a Preferences DataStore to act as the single source of truth for widget data.
-   **Data Flow:**
    -   Updated `PwnagotchiService` to write UI state updates to the `WidgetStateRepository`.
-   **UI & Implementation:**
    -   Refactored the home screen widget using the Jetpack Glance API (`PwnagotchiGlanceWidget`).
    -   Replaced the old XML-based widget with the new `PwnagotchiGlanceWidgetReceiver`.

## Code Review Fixes & Stability Improvements

-   **ViewModel Stability:** Moved `PwnagotchiViewModel` to be an Activity-level property and called `setService` from the `onServiceConnected` callback to prevent side effects from recomposition.
-   **UI Thread Safety:** Removed `runBlocking` from `StatusWidget.kt` to prevent blocking the UI thread, setting the initial state to an empty string instead.
-   **Crash Prevention:** Wrapped JSON decoding in `try-catch` blocks in `LeaderboardWidget.kt` and `HandshakeLogWidget.kt` to handle malformed data gracefully.
-   **Settings Persistence:** Fixed a bug in `SettingsScreen.kt` where theme selection was not being persisted to `SharedPreferences`.
-   **Navigation & Resources:** Updated the `Screen` sealed class to use string resource IDs for titles, ensuring correct and translatable screen labels in the navigation rail.