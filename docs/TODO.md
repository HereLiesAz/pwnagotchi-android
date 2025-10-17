# Pwnagotchi Android: A Comprehensive Guide to Production Readiness

## Executive Summary

This document provides an exhaustive, step-by-step engineering guide for transitioning the Pwnagotchi Android application from its current prototype state to a production-ready release suitable for the Google Play Store. The directive focuses on four critical pillars: foundational modernization and architectural refactoring, core functionality implementation and security hardening, user experience refinement and performance optimization, and final release preparations.

The execution plan is predicated on a mandatory build environment consisting of Android API 36, Android Gradle Plugin (AGP) 8.13.0, and Kotlin 2.2.20. The provided project source code serves as the baseline, and this guide systematically addresses its architectural deficiencies, incomplete features, security vulnerabilities, and outdated configurations. Each phase and its constituent steps are designed to be atomic and actionable, providing the necessary context, code modifications, and validation procedures to ensure a high-quality, secure, and performant final product.

## Phase 1: Foundational Modernization and Architectural Refactoring

The initial phase is critical for establishing a stable and modern foundation. It addresses underlying architectural and configuration issues that would otherwise compromise the stability, security, and maintainability of the final product. The objective is to rectify these foundational problems before implementing new features or undertaking performance optimizations.

### Section 1.1: Build Environment Upgrade and Dependency Modernization

The target environment—AGP 8.13.0 and Kotlin 2.2.20—necessitates a corresponding upgrade of the Gradle build tool itself, as major AGP versions are tightly coupled with major Gradle versions.

The Kotlin Gradle Plugin (KGP) version 2.2.20 is compatible with Gradle versions up to 8.13, making the specified configuration technically viable and stable. The following steps will bring the project into compliance.

**Actionable Steps:**

1.  **Upgrade Gradle Wrapper:** The project must be updated to use Gradle 8.14. This is accomplished by running the wrapper task from the project's root directory: `./gradlew wrapper --gradle-version=8.14`
2.  **Update Android Gradle Plugin (AGP):** Modify the top-level `build.gradle.kts` file to specify the target AGP version.
    *   **File:** `./build.gradle.kts`
    *   **Modification:** Change the version from `8.13.0` to `8.14.0`.
3.  **Dependency Audit and Update:** A comprehensive audit of the dependencies listed in `app/build.gradle.kts` reveals several outdated libraries. These must be updated to their latest stable versions to ensure compatibility with API 36 and the new build tools.
4.  **Ktor Engine Optimization:** The current implementation uses the `ktor-client-cio` engine. For Android, Ktor provides a dedicated `ktor-client-android` engine that leverages the platform's native networking stack. Migrating to this engine offers better performance and battery efficiency.
    *   **File:** `app/build.gradle.kts` - Replace `ktor-client-cio` with `ktor-client-android`.
    *   **File:** `app/src/main/java/com/pwnagotchi/pwnagotchiandroid/OpwngridClient.kt` - Update the `HttpClient` instantiation to use the `Android` engine.

### Section 1.2: Navigation Architecture Overhaul with AzNavRail

The application's current navigation mechanism, implemented within `MainActivity.kt` using mutable boolean state flags, is a significant architectural liability. A complete refactoring to a modern, decoupled navigation architecture using the AzNavRail component is a prerequisite for a production-quality application.

**Actionable Steps:**

1.  **Add AzNavRail Dependency:** Incorporate the `AzNavRail` library from `https://github.com/HereLiesAz/AzNavRail`.
2.  **Define Navigation Routes:** Create a centralized, type-safe definition for all navigation destinations.
3.  **Refactor Screens to be Stateless:** Modify each screen composable to accept a `NavController` instance or specific navigation lambda functions as parameters.
4.  **Implement AzNavRail:** Overhaul `MainActivity.kt` to use the `AzNavRail` composable to manage screen transitions and the navigation rail, completely removing the previous `when` block based on boolean flags.

## Phase 2: Core Functionality Implementation and Security Hardening

This phase focuses on implementing the application's most complex and unique features while simultaneously establishing a robust security posture.

### Section 2.1: Activating the Local Agent (Root Mode)

The "local agent" mode, designed to leverage the Android device's own hardware for Wi-Fi monitoring, is contingent on root access and is currently unimplemented. A production-ready implementation must adopt a strategy pattern, attempting a sequence of known methods to enable monitor mode.

**Actionable Steps:**

1.  **Create LocalAgentManager:** Develop a new class to encapsulate all logic related to root operations for the local agent.
2.  **Implement Monitor Mode Strategy:**
    *   Define a sequence of methods to attempt enabling monitor mode (e.g., `iwconfig`, `airmon-ng`, Qualcomm `con_mode`).
    *   Use `libsu`'s `Shell.cmd()` to execute commands and capture both `stdout` and `stderr`.
    *   After each attempt, verify the outcome by parsing the output of `iwconfig wlan0`.
3.  **Integrate with UI and Service:** Add a "Start Local Agent" button, provide clear real-time feedback on the process, and handle success or failure gracefully.

### Section 2.2: Finalizing oPwngrid Integration via Web Scraping

- [x] **Step 3.2.3: Refactor State Management to a Single Source of Truth**
    - [x] In `PwnagotchiViewModel.kt`, remove logic that directly modifies UI state from `fetchLeaderboard`. The ViewModel should only delegate calls to the service. *(Note: This issue was already resolved in the codebase.)*
    - [x] In `PwnagotchiService.kt`, create a new `fetchLeaderboard` function that performs the network call and updates the service's internal state, which will then emit an updated `uiState`.

**Actionable Steps:**

1.  **Add Jsoup Dependency:** Include the `org.jsoup:jsoup` library for HTML parsing.
2.  **Refactor OpwngridClient.getLeaderboard():** Rewrite the function to:
    *   Fetch the raw HTML of the leaderboard page using Ktor.
    *   Parse the HTML using Jsoup to traverse the DOM and extract the required data.
3.  **Implement Robust Error Handling:** Web scraping is fragile. The implementation must handle potential failures gracefully and communicate them to the user.

### Section 2.3: Comprehensive Application Hardening

The current release build configuration is insecure, with code shrinking and obfuscation disabled. Enabling R8 (Android's default shrinker and obfuscator) is the single most important security measure to implement.

**Actionable Steps:**

1.  **Enable R8 and Resource Shrinking:** In `app/build.gradle.kts`, set `isMinifyEnabled = true` and `isShrinkResources = true` for the `release` build type.
2.  **Populate proguard-rules.pro:** Add the necessary Proguard "keep" rules to prevent R8 from breaking code that relies on reflection, especially for libraries like Kotlinx Serialization, Ktor, libsu, and Jetpack Compose.
3.  **Secure Android Manifest:** Disable unencrypted backups by setting `android:allowBackup="false"` in `AndroidManifest.xml`.

## Phase 3: User Experience Polish and Performance Optimization

This phase focuses on refining the application to be intuitive, helpful, and efficient for the end-user.

### Section 3.1: Designing an Intuitive Onboarding Experience

A guided, multi-page onboarding flow is essential to manage user expectations, explain the app's purpose and requirements, and handle initial permission requests.

**Actionable Steps:**

1.  **Add Pager Dependency:** Ensure the foundation library, which includes `HorizontalPager`, is up-to-date.
2.  **Create Onboarding Screen and Data Model:** Define a new `OnboardingScreen.kt` that uses a `HorizontalPager` to display a series of informative pages.
3.  **Implement Navigation and State Management:** Use `rememberPagerState` to track the current page and include UI elements for navigation ("Next," "Skip").
4.  **Integrate Permission Request:** On one of the pages, explain why notifications are used and include a button to trigger the `POST_NOTIFICATIONS` runtime permission request.
5.  **Integrate with Main Navigation:** Use `SharedPreferences` or `DataStore` to track whether the user has completed the onboarding flow. The app's start destination will be determined by this flag.

### Section 3.2: Performance Profiling and Optimization

- [x] **Step 3.4.3: Final API Endpoint Verification**
    - [x] Investigate and verify a stable, public API endpoint for oPwngrid leaderboard data.
    - [x] If no reliable endpoint is found, disable or remove the feature from the UI.

- [x] **Step 3.4.4: Remove All Logging and TODOs**
    - [x] Conduct a project-wide search to remove all debug logging (`Log.d`, `e.printStackTrace()`).
    - [x] Resolve or remove all remaining `// TODO` comments.

## Phase 5: Production Readiness Protocol

### I. Foundational Upgrade: Aligning the Build Environment

- [x] **Step 5.1.1: Gradle Wrapper Upgrade to Version 8.14**
    - [x] Modify `gradle/wrapper/gradle-wrapper.properties` to set `distributionUrl` to `https\://services.gradle.org/distributions/gradle-8.14-bin.zip`.
- [x] **Step 5.1.2: Android Gradle Plugin and Kotlin Plugin Update**
    - [x] In the root `build.gradle.kts`, update the `com.android.application` plugin to version `8.13.0`.
- [x] **Step 5.1.3: Dependency Modernization and Addition**
    - [x] In `app/build.gradle.kts`, update core, Compose, Material, Ktor, and testing dependencies to the specified new versions.
    - [x] Add new dependencies for Jetpack Glance (`androidx.glance:glance-appwidget:1.1.1`, `androidx.glance:glance-material3:1.1.1`).
    - [x] Add new dependency for Jetpack Navigation Compose (`androidx.navigation:navigation-compose:2.8.0-beta05`).

### II. Architectural Refactoring: Implementing a Scalable Navigation Rail

- [x] **Step 5.2.1: Define Navigation Routes and Graph**
    - [x] Create `app/src/main/java/com/pwnagotchi/pwnagotchiandroid/ui/navigation/AppNavigation.kt` with a `Screen` sealed class for navigation destinations.
    - [x] Add new string resources for screen titles (`screen_home`, `screen_opwngrid`) to `app/src/main/res/values/strings.xml`.
- [x] **Step 5.2.2: Refactor MainActivity with Scaffold, AzNavRail, and NavHost**
    - [x] Create `app/src/main/java/com/pwnagotchi/pwnagotchiandroid/ui/MainScreen.kt` to house the main UI structure with `NavigationRail` and `NavHost`.
    - [x] Create new screen wrapper composables: `HomeScreen.kt`, `PluginsScreenNav.kt`, `OpwngridScreenNav.kt`, `SettingsScreenNav.kt`.
    - [x] Refactor `PluginsScreen.kt`, `OpwngridScreen.kt`, and `SettingsScreen.kt` to remove `onBack` callbacks.
    - [x] Completely replace the content of `MainActivity.kt` to use the new `MainScreen` composable and handle service binding.

### III. Enhancing Ambient UX Part 1: The Persistent E-Ink Display Notification

- [x] **Step 5.3.1: Design the Custom Notification Layout**
    - [x] Create `app/src/main/res/layout/notification_pwnagotchi.xml` with an `ImageView` and `TextViews` for the custom notification.
- [ ] **Step 5.3.2: Integrate RemoteViews into PwnagotchiService**
    - [ ] In `PwnagotchiService.kt`, refactor `onStartCommand` to create the initial notification.
    - [ ] In the `onMessage` `ui_update` case, call `updateNotification` with the new data.
    - [ ] Replace `createNotification` and `updateNotification` methods to use `RemoteViews`.
    - [ ] Add a `createRemoteViews` helper method to populate the custom layout.
    - [ ] Modify the `disconnect` method to update the notification to a disconnected state.

### IV. Enhancing Ambient UX Part 2: Home Screen Integration via Jetpack Glance Widgets

- [x] **Step 5.4.1: Core Widget Infrastructure Setup**
    - [x] Create a new package `com.pwnagotchi.pwnagotchiandroid.widgets`.
    - [x] Create eight new widget files: `StatusWidget.kt` & `StatusWidgetReceiver.kt`, `HandshakeLogWidget.kt` & `HandshakeLogWidgetReceiver.kt`, `QuickActionsWidget.kt` & `QuickActionsWidgetReceiver.kt`, `LeaderboardWidget.kt` & `LeaderboardWidgetReceiver.kt`.
    - [x] Create four XML provider info files in `res/xml/`: `status_widget_info.xml`, `handshake_log_widget_info.xml`, `quick_actions_widget_info.xml`, `leaderboard_widget_info.xml`.
    - [x] Register the four new widget receivers in `AndroidManifest.xml`.
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
