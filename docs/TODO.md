# Pwnagotchi Android: A Comprehensive Guide to Production Readiness

## Executive Summary

This document provides an exhaustive, step-by-step engineering guide for transitioning the Pwnagotchi Android application from its current prototype state to a production-ready release suitable for the Google Play Store. The directive focuses on four critical pillars: foundational modernization and architectural refactoring, core functionality implementation and security hardening, user experience refinement and performance optimization, and final release preparations.

The execution plan is predicated on a mandatory build environment consisting of Android API 36, Android Gradle Plugin (AGP) 8.14.0, and Kotlin 2.2.20. The provided project source code serves as the baseline, and this guide systematically addresses its architectural deficiencies, incomplete features, security vulnerabilities, and outdated configurations. Each phase and its constituent steps are designed to be atomic and actionable, providing the necessary context, code modifications, and validation procedures to ensure a high-quality, secure, and performant final product.

## Phase 1: Foundational Modernization and Architectural Refactoring

The initial phase is critical for establishing a stable and modern foundation. It addresses underlying architectural and configuration issues that would otherwise compromise the stability, security, and maintainability of the final product. The objective is to rectify these foundational problems before implementing new features or undertaking performance optimizations.

### Section 1.1: Build Environment Upgrade and Dependency Modernization

The first priority is to align the project's build environment with the specified modern toolchain. This ensures access to the latest features, security patches, and performance improvements, while also mitigating risks associated with outdated libraries. The target environment—AGP 8.14.0 and Kotlin 2.2.20—necessitates a corresponding upgrade of the Gradle build tool itself, as major AGP versions are tightly coupled with major Gradle versions.

The Kotlin Gradle Plugin (KGP) version 2.2.20 is compatible with Gradle versions up to 8.14, making the specified configuration technically viable and stable. The following steps will bring the project into compliance.

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

The `OpwngridClient` is currently non-functional as it attempts to call a non-existent API endpoint. The strategy must pivot from API consumption to HTML web scraping.

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

A production-ready application must be responsive and efficient. Use the Android Studio Profiler to investigate potential performance hotspots.

**Key Areas for Profiling:**

*   **UI Rendering (Jank):** The `LazyColumn` for the handshake list.
*   **Background Service (Remote Mode):** The `PwnagotchiService`'s WebSocket connection and data processing.
*   **Background Service (Local Mode):** The execution of root commands.

**Actionable Steps:**

1.  **Profile UI Rendering Performance:** Use "System Trace" to find frames that exceed the 16ms rendering budget and use the Layout Inspector's "Recomposition Counts" to identify and fix unnecessary recompositions.
2.  **Profile Background Service (Remote Mode):** Use the "Energy Profiler" to monitor the app's battery consumption and the CPU Profiler to find sources of unnecessary work.
3.  **Profile Background Service (Local Mode):** Repeat the Energy and CPU profiling process to identify optimizations, such as batching commands or reducing the frequency of status checks.

## Phase 4: Production Release and Deployment

This final phase covers all technical and logistical steps required to prepare, sign, and publish the application to the Google Play Store.

### Section 4.1: Pre-Release Asset and Configuration Checklist

A polished and professional store presence is crucial for user trust and adoption.

**Actionable Steps:**

1.  **Finalize Versioning:** Set the initial production release to `versionCode = 1` and `versionName = "1.0.0"`. Increment `versionCode` for every subsequent release.
2.  **Review and Refine All User-Facing Strings:** Audit every string in `strings.xml` for clarity, grammar, and consistency.
3.  **Prepare Google Play Store Listing Assets:** Create high-resolution screenshots, a feature graphic, and a detailed app description.

### Section 4.2: Google Play Store Deployment Guide

The final step is to publish the signed AAB to the Google Play Store.

**Final Release Checklist:**

1.  **Asset Finalization:**
    *   [ ] Finalize `versionCode=1` and `versionName="1.0.0"`.
    *   [ ] Review all user-facing strings.
    *   [ ] Prepare store listing text and screenshots.
2.  **Play Console:**
    *   [ ] Create a new application in the Google Play Console.
    *   [ ] Complete all required store listing sections.
    *   [ ] Upload the signed AAB to the "Internal Testing" track.
    *   [ ] Add internal testers and distribute the release.
4.  **Staged Rollout:**
    *   [ ] Promote the release from "Internal Testing" to "Production."
    *   [ ] Select the "Staged rollout" option, starting with a small percentage (e.g., 5%).
    *   [ ] Monitor crash reports and user feedback in the Play Console.
    *   [ ] Gradually increase the rollout percentage to 100%.
