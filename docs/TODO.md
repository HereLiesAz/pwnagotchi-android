# Pwnagotchi Android: Production Readiness Roadmap

This document outlines a comprehensive, multi-phase roadmap to transform the Pwnagotchi Android application from its current prototype state into a stable, maintainable, and production-ready product. This roadmap is derived from a detailed project audit.

## Phase 1: Foundational Stability - Build & Dependencies

This initial phase focuses on stabilizing the project's foundation by updating its build environment and dependencies. This is a prerequisite for all subsequent work.

- [x] **Step 3.1.1: Update Gradle and Android Gradle Plugin (AGP)**
    - [x] Modify `gradle/wrapper/gradle-wrapper.properties` to use a modern, stable Gradle version (e.g., 8.13).
    - [x] Ensure the root `build.gradle.kts` specifies a compatible, stable AGP version (e.g., 8.13.0).

- [x] **Step 3.1.2: Correct SDK and Tooling Versions**
    - [x] In `app/build.gradle.kts`, change `compileSdk` and `targetSdk` from preview version 36 to the latest stable version (e.g., 35).
    - [x] Remove the explicit `buildToolsVersion` definition from `app/build.gradle.kts`.

- [x] **Step 3.1.3: Overhaul Project Dependencies**
    - [x] Replace the outdated Compose BOM `2023.03.00` with the latest stable version (e.g., `2025.10.00`).
    - [x] Remove explicit versions from all `androidx.compose` dependencies to let the BOM manage them.
    - [x] Update all other outdated dependencies (AndroidX, Ktor, Java-WebSocket) to their latest stable versions as detailed in the audit.
    - [x] Remove the legacy Material 2 dependency: `com.google.android.material:material`.

- [x] **Step 3.1.4: Sync and Resolve Breaking Changes**
    - [x] Perform a Gradle sync.
    - [x] Systematically compile and address all breaking API changes resulting from the dependency upgrades, paying close attention to Ktor (2.x to 3.x) and Jetpack libraries.

## Phase 2: Codebase Refactoring & Modernization

With a stable build environment, this phase focuses on improving code quality, eliminating anti-patterns, and increasing the application's robustness.

- [x] **Step 3.2.1: Centralize Constants**
    - [x] Create a new `Constants.kt` file for application-wide constants.
    - [x] Move hardcoded values (IP addresses, ports, API URLs) into this file.
    - [x] Refactor the codebase to reference these constants.

- [x] **Step 3.2.2: Implement Type-Safe Serialization**
    - [x] Define `@Serializable` data classes for all WebSocket message structures.
    - [x] Refactor `PwnagotchiService.kt`'s `onMessage` callback to use `kotlinx.serialization` for JSON parsing, removing all `org.json.JSONObject` usage.

- [x] **Step 3.2.3: Refactor State Management to a Single Source of Truth**
    - [x] In `PwnagotchiViewModel.kt`, remove logic that directly modifies UI state from `fetchLeaderboard`. The ViewModel should only delegate calls to the service. *(Note: This issue was already resolved in the codebase.)*
    - [x] In `PwnagotchiService.kt`, create a new `fetchLeaderboard` function that performs the network call and updates the service's internal state, which will then emit an updated `uiState`.

- [x] **Step 3.2.4: Implement Robust Reconnection Logic**
    - [x] Refactor `scheduleReconnect` in `PwnagotchiService.kt` to be network-aware.
    - [x] Use `ConnectivityManager` to listen for network availability and only attempt reconnection when a valid network is present.

- [x] **Step 3.2.5: Externalize All UI Strings**
    - [x] Move all hardcoded user-facing strings from Composables into `app/src/main/res/values/strings.xml`.
    - [x] Replace hardcoded strings in the code with `stringResource()` calls.

## Phase 3: Architectural and UI Refinement

This phase addresses major architectural and user interface flaws, focusing on navigation, theming, and resource correctness.

- [x] **Step 3.3.1: Implement Jetpack Navigation Compose**
    - [x] Add the `androidx.navigation:navigation-compose` dependency.
    - [x] Create a sealed class to define navigation routes.
    - [x] Replace the boolean-flag-based navigation in `MainActivity.kt` with a `NavHost` and composable destinations for a proper back stack.

- [x] **Step 3.3.2: Unify Theming to Material 3**
    - [x] Delete legacy M2 theme and color files (`themes.xml`, `colors.xml`).
    - [x] Update `AndroidManifest.xml` to use a non-MaterialComponents base theme.
    - [x] Ensure `PwnagotchiAndroidTheme.kt` exclusively uses `androidx.compose.material3` APIs.

- [x] **Step 3.3.3: Fix Theme Switching Logic**
    - [x] Correct the status bar appearance logic in `PwnagotchiAndroidTheme.kt` to correctly reflect the chosen light/dark theme.

- [x] **Step 3.3.4: Add Missing Drawable Resources**
    - [x] Create the missing vector drawable files (`face_happy.xml`, `face_sad.xml`, `face_bored.xml`) to prevent runtime crashes.

- [x] **Step 3.3.5: Address the Incomplete Root Feature**
    - [x] **Decision:** Remove the feature.
    - [x] Delete the `com.github.topjohnwu.libsu` dependency.
    - [x] Remove the `RootControls` composable and all related root-checking logic.

## Phase 4: Production Hardening

The final phase involves preparing the application for a public release.

- [x] **Step 3.4.1: Enable Code Shrinking and Obfuscation**
    - [x] In `app/build.gradle.kts`, set `isMinifyEnabled` to `true` for the `release` build type.

- [x] **Step 3.4.2: Configure ProGuard/R8 Rules**
    - [x] Add necessary `-keep` rules to `proguard-rules.pro` to prevent R8 from removing classes used via reflection by `kotlinx.serialization`.

- [x] **Step 3.4.3: Final API Endpoint Verification**
    - [x] Investigate and verify a stable, public API endpoint for oPwngrid leaderboard data.
    - [x] If no reliable endpoint is found, disable or remove the feature from the UI.

- [x] **Step 3.4.4: Remove All Logging and TODOs**
    - [x] Conduct a project-wide search to remove all debug logging (`Log.d`, `e.printStackTrace()`).
    - [x] Resolve or remove all remaining `// TODO` comments.

## Phase 5: Production Readiness Protocol

### I. Foundational Upgrade: Aligning the Build Environment

- [ ] **Step 5.1.1: Gradle Wrapper Upgrade to Version 8.14**
    - [ ] Modify `gradle/wrapper/gradle-wrapper.properties` to set `distributionUrl` to `https\://services.gradle.org/distributions/gradle-8.14-bin.zip`.
- [ ] **Step 5.1.2: Android Gradle Plugin and Kotlin Plugin Update**
    - [ ] In the root `build.gradle.kts`, update the `com.android.application` plugin to version `8.14.0`.
- [ ] **Step 5.1.3: Dependency Modernization and Addition**
    - [ ] In `app/build.gradle.kts`, update core, Compose, Material, Ktor, and testing dependencies to the specified new versions.
    - [ ] Add new dependencies for Jetpack Glance (`androidx.glance:glance-appwidget:1.1.1`, `androidx.glance:glance-material3:1.1.1`).
    - [ ] Add new dependency for Jetpack Navigation Compose (`androidx.navigation:navigation-compose:2.8.0-beta05`).

### II. Architectural Refactoring: Implementing a Scalable Navigation Rail

- [ ] **Step 5.2.1: Define Navigation Routes and Graph**
    - [ ] Create `app/src/main/java/com/pwnagotchi/pwnagotchiandroid/ui/navigation/AppNavigation.kt` with a `Screen` sealed class for navigation destinations.
    - [ ] Add new string resources for screen titles (`screen_home`, `screen_opwngrid`) to `app/src/main/res/values/strings.xml`.
- [ ] **Step 5.2.2: Refactor MainActivity with Scaffold, AzNavRail, and NavHost**
    - [ ] Create `app/src/main/java/com/pwnagotchi/pwnagotchiandroid/ui/MainScreen.kt` to house the main UI structure with `NavigationRail` and `NavHost`.
    - [ ] Create new screen wrapper composables: `HomeScreen.kt`, `PluginsScreenNav.kt`, `OpwngridScreenNav.kt`, `SettingsScreenNav.kt`.
    - [ ] Refactor `PluginsScreen.kt`, `OpwngridScreen.kt`, and `SettingsScreen.kt` to remove `onBack` callbacks.
    - [ ] Completely replace the content of `MainActivity.kt` to use the new `MainScreen` composable and handle service binding.

### III. Enhancing Ambient UX Part 1: The Persistent E-Ink Display Notification

- [ ] **Step 5.3.1: Design the Custom Notification Layout**
    - [ ] Create `app/src/main/res/layout/notification_pwnagotchi.xml` with an `ImageView` and `TextViews` for the custom notification.
- [ ] **Step 5.3.2: Integrate RemoteViews into PwnagotchiService**
    - [ ] In `PwnagotchiService.kt`, refactor `onStartCommand` to create the initial notification.
    - [ ] In the `onMessage` `ui_update` case, call `updateNotification` with the new data.
    - [ ] Replace `createNotification` and `updateNotification` methods to use `RemoteViews`.
    - [ ] Add a `createRemoteViews` helper method to populate the custom layout.
    - [ ] Modify the `disconnect` method to update the notification to a disconnected state.

### IV. Enhancing Ambient UX Part 2: Home Screen Integration via Jetpack Glance Widgets

- [ ] **Step 5.4.1: Core Widget Infrastructure Setup**
    - [ ] Create a new package `com.pwnagotchi.pwnagotchiandroid.widgets`.
    - [ ] Create eight new widget files: `StatusWidget.kt` & `StatusWidgetReceiver.kt`, `HandshakeLogWidget.kt` & `HandshakeLogWidgetReceiver.kt`, `QuickActionsWidget.kt` & `QuickActionsWidgetReceiver.kt`, `LeaderboardWidget.kt` & `LeaderboardWidgetReceiver.kt`.
    - [ ] Create four XML provider info files in `res/xml/`: `status_widget_info.xml`, `handshake_log_widget_info.xml`, `quick_actions_widget_info.xml`, `leaderboard_widget_info.xml`.
    - [ ] Register the four new widget receivers in `AndroidManifest.xml`.
- [ ] **Step 5.4.2: Architect the Widget Data Synchronization Mechanism**
    - [ ] Create `app/src/main/java/com/pwnagotchi/pwnagotchiandroid/widgets/WidgetStateRepository.kt` to define and manage a `PreferencesDataStore`.
    - [ ] Modify `PwnagotchiService.kt` to write UI and handshake updates to the `WidgetStateRepository`.
    - [ ] Add a new method to `PwnagotchiViewModel` to update leaderboard widgets.
- [ ] **Step 5.4.3: Implement the Four Widgets**
    - [ ] Implement the `StatusWidget.kt` and `StatusWidgetReceiver.kt` using Jetpack Glance to display status from the DataStore.
    - [ ] Implement the `HandshakeLogWidget`, `QuickActionsWidget`, and `LeaderboardWidget` following a similar pattern.

### V. Finalizing for Production: Code Hardening and Optimization

- [ ] **Step 5.5.1: Enable R8 and Resource Shrinking**
    - [ ] In `app/build.gradle.kts`, enable `isMinifyEnabled` and `shrinkResources` for the `release` build type.
- [ ] **Step 5.5.2: Construct the Comprehensive ProGuard Configuration**
    - [ ] Create or update `app/proguard-rules.pro` with the provided comprehensive keep rules for Kotlin, kotlinx.serialization, Ktor, libsu, Jetpack Compose, and Jetpack Glance.
