# Pwnagotchi Android: A Comprehensive Guide to Production Readiness

## Executive Summary

This document provides an exhaustive, step-by-step engineering guide for transitioning the Pwnagotchi Android application from its current prototype state to a production-ready release suitable for the Google Play Store. The directive focuses on four critical pillars: foundational modernization and architectural refactoring, core functionality implementation and security hardening, user experience refinement and performance optimization, and final release preparations.

## Phase 1: Foundational Modernization and Architectural Refactoring

### Section 1.1: Build Environment Upgrade and Dependency Modernization
- [ ] **Upgrade Gradle Wrapper:** The project must be updated to use Gradle 8.14.
- [ ] **Update Android Gradle Plugin (AGP):** Modify the top-level `build.gradle.kts` to specify the target AGP version 8.14.0.
- [x] **Dependency Audit and Update:** A comprehensive audit of the dependencies has been completed.
- [x] **Ktor Engine Optimization:** The Ktor client has been migrated to the `android` engine.

### Section 1.2: Implementing Dual-Mode Architecture
- [x] **Define Data Source Abstraction:** The `PwnagotchiDataSource.kt` interface has been created.
- [x] **Create `RemotePwnagotchiSource`:** All WebSocket logic has been migrated from the service to this new data source.
- [x] **Create `LocalPwnagotchiSource`:** A placeholder class has been created for the local agent mode.
- [x] **Refactor `PwnagotchiService` as a Mode Controller:** The service has been refactored to delegate all operations to the active data source.

## Phase 2: UI Refactoring for Dual-Mode Operation

### Section 2.1: Implementing AzNavRail and NavHost
- [x] **Define Navigation Graph:** A `Screen.kt` sealed class has been created to define all navigation routes.
- [x] **Refactor `MainActivity`:** The `setContent` block has been overhauled to host the new `MainScreen` composable.
- [x] **Create `MainScreen` Composable:** The main UI structure has been built with `AzNavRail` and a `NavHost`.
- [x] **Refactor Screen Composables:** All primary screens have been refactored to be stateless and driven by their own ViewModels.

### Section 2.2: Implementing Mode Selection UI
- [x] **Add Mode Selection State:** State for managing the `AppMode` has been added to the `PwnagotchiViewModel`.
- [x] **Create Mode Switch Composable:** A `ModeSelector` composable has been built and integrated into the `HomeScreen`.
- [x] **Update `HomeScreen`:** The `HomeScreen` now conditionally displays the remote or local agent controls based on the selected mode.

## Phase 3: Core Functionality Implementation and Security Hardening

### Section 3.1: Activating the Local Agent (Root Mode)
- [ ] **Create LocalAgentManager:** Develop a new class to encapsulate all logic related to root operations for the local agent.
- [ ] **Implement Monitor Mode Strategy:**
    - [ ] Define a sequence of methods to attempt enabling monitor mode.
    - [ ] Use `libsu`'s `Shell.cmd()` to execute commands and capture both `stdout` and `stderr`.
    - [ ] After each attempt, verify the outcome by parsing the output of `iwconfig wlan0`.
- [ ] **Integrate with UI and Service:** Add a "Start Local Agent" button, provide clear real-time feedback on the process, and handle success or failure gracefully.

### Section 3.2: Finalizing oPwngrid Integration via Web Scraping
- [ ] **Add Jsoup Dependency:** Include the `org.jsoup:jsoup` library for HTML parsing.
- [ ] **Refactor OpwngridClient.getLeaderboard():** Rewrite the function to fetch and parse the HTML.
- [ ] **Implement Robust Error Handling:** Handle web scraping failures gracefully.

### Section 3.3: Comprehensive Application Hardening
- [ ] **Enable R8 and Resource Shrinking:** In `app/build.gradle.kts`, set `isMinifyEnabled = true` and `isShrinkResources = true` for the `release` build type.
- [ ] **Populate proguard-rules.pro:** Add the necessary Proguard "keep" rules.
- [ ] **Secure Android Manifest:** Set `android:allowBackup="false"` in `AndroidManifest.xml`.

## Phase 4: User Experience Polish and Performance Optimization

### Section 4.1: Designing an Intuitive Onboarding Experience
- [ ] **Add Pager Dependency:** Ensure the foundation library, which includes `HorizontalPager`, is up-to-date.
- [ ] **Create Onboarding Screen and Data Model:** Define a new `OnboardingScreen.kt`.
- [ ] **Implement Navigation and State Management:** Use `rememberPagerState` to track the current page.
- [ ] **Integrate Permission Request:** Add a button to trigger the `POST_NOTIFICATIONS` runtime permission request.
- [ ] **Integrate with Main Navigation:** Use `SharedPreferences` or `DataStore` to track onboarding completion.

### Section 4.2: Enhancing Ambient UX
- [ ] **Implement Persistent E-Ink Notification:** Refactor the service's notification to use a custom `RemoteViews` layout.
- [ ] **Implement Home Screen Widgets:**
    - [ ] Set up the core infrastructure for Jetpack Glance widgets.
    - [ ] Architect the `WidgetStateRepository` with `DataStore`.
    - [ ] Implement the four widgets: Status, Handshake Log, Quick Actions, and Leaderboard.

### Section 4.3: Finalizing for Production
- [ ] **Remove All Logging and TODOs:** Conduct a project-wide search to remove all debug logging and resolve all `// TODO` comments.