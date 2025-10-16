# Pwnagotchi Android: Production Readiness Roadmap

This document outlines the engineering tasks required to transition the Pwnagotchi Android application to a production-ready, dual-mode application, as detailed in the "Production Readiness Protocol."

## Phase 1: Foundational Upgrade & Architectural Refactoring

### Section 1.1: Build Environment and Dependency Modernization
- [x] **Upgrade Gradle Wrapper:** Modify `gradle-wrapper.properties` to use Gradle version 8.14. *(Note: This was already completed in the baseline source.)*
- [ ] **Update Android Gradle Plugin (AGP):** Set AGP to version `8.14.0` in the root `build.gradle.kts`.
- [ ] **Dependency Overhaul:**
    - [ ] Update all existing libraries in `app/build.gradle.kts` to their specified modern versions.
    - [ ] Add new dependencies for Jetpack Navigation Compose (`androidx.navigation:navigation-compose`).
    - [ ] Add new dependencies for Jetpack Glance (`androidx.glance:glance-appwidget`, `androidx.glance:glance-material3`).
    - [ ] Add the new dependency for `AzNavRail`.

### Section 1.2: Implementing Dual-Mode Architecture
- [ ] **Define Data Source Abstraction:**
    - [ ] Create the `PwnagotchiDataSource.kt` interface defining the contract for `uiState`, `start`, `stop`, and `sendCommand`.
- [ ] **Create `RemotePwnagotchiSource`:**
    - [ ] Create the `datasources` package.
    - [ ] Create the `RemotePwnagotchiSource.kt` class.
    - [ ] Move all WebSocket connection and communication logic from `PwnagotchiService` into this new class.
- [ ] **Create `LocalPwnagotchiSource`:**
    - [ ] Create the `LocalPwnagotchiSource.kt` class as a placeholder that implements the `PwnagotchiDataSource` interface.
- [ ] **Refactor `PwnagotchiService` as a Mode Controller:**
    - [ ] Introduce an `AppMode` enum (`LOCAL`, `REMOTE`).
    - [ ] Modify the service to hold instances of `LocalPwnagotchiSource` and `RemotePwnagotchiSource`.
    - [ ] Implement a `setMode` function to switch the `activeDataSource`.
    - [ ] Delegate `connect`, `disconnect`, and `sendCommand` calls to the `activeDataSource`.

## Phase 2: UI Refactoring for Dual-Mode Operation

### Section 2.1: Implementing AzNavRail and NavHost
- [ ] **Define Navigation Graph:** Create a `Screen.kt` sealed class to define all navigation routes, titles, and icons.
- [ ] **Refactor `MainActivity`:** Overhaul the `setContent` block to host a new `MainScreen` composable and initialize the `PwnagotchiViewModel`.
- [ ] **Create `MainScreen` Composable:**
    - [ ] Build the main UI structure using `Scaffold`, `AzNavRail`, and a `NavHost`.
    - [ ] The `NavHost` should contain composable destinations for each screen.
- [ ] **Refactor Screen Composables:** Adapt all screens (`PwnagotchiScreen`, `PluginsScreen`, etc.) to be stateless, receiving the `ViewModel` and navigation callbacks as parameters.

### Section 2.2: Implementing Mode Selection UI
- [ ] **Add Mode Selection State:** Add state in `PwnagotchiViewModel` to track the current `AppMode`.
- [ ] **Create Mode Switch Composable:** Build a `ModeSelector` composable using `SegmentedButtonRow` for the `HomeScreen`.
- [ ] **Update `HomeScreen`:**
    - [ ] Integrate the `ModeSelector`.
    - [ ] Conditionally display connection controls for `REMOTE` mode.
    - [ ] Conditionally display root agent controls for `LOCAL` mode.

## Phase 3: Local Host Mode Implementation

### Section 3.1: Bundling and Deploying Native Binaries
- [ ] **Acquire `bettercap` Binary:** Obtain or compile a `bettercap` binary for the `arm64-v8a` architecture.
- [ ] **Add Binary to Assets:** Place the `bettercap` binary in `app/src/main/assets/`.
- [ ] **Implement Binary Deployment Logic:** In `LocalPwnagotchiSource`, create a function to copy the binary to the app's private storage and make it executable using a root shell.

### Section 3.2: Implementing Wi-Fi Monitor Mode Activation
- [ ] **Create `MonitorModeManager`:** Develop a new class to encapsulate the logic for enabling/disabling monitor mode.
- [ ] **Implement Activation Strategy:**
    - [ ] Implement the primary method of writing to `/sys/module/wlan/parameters/con_mode`.
    - [ ] Implement the fallback method of using `iwconfig`.
    - [ ] Add a `verify` function to check if monitor mode is active.

### Section 3.3: Launching and Managing the `bettercap` Process
- [ ] **Implement Process Launch:** In `LocalPwnagotchiSource`, orchestrate the `start` sequence: deploy binary, enable monitor mode, and launch the `bettercap` process using a root shell.
- [ ] **Manage Process:** Capture the process and its output streams for logging and control.
- [ ] **Connect to Local WebSocket:** Implement logic to connect the app's WebSocket client to `bettercap`'s local event stream.
- [ ] **Implement `stop` Logic:** Ensure the `bettercap` process is terminated and monitor mode is disabled when the local agent is stopped.

## Phase 4: Ambient UX and Production Hardening

- [ ] **Implement Persistent E-Ink Notification:** Refactor the service's notification to use a custom `RemoteViews` layout that mimics the Pwnagotchi's e-ink display.
- [ ] **Implement Home Screen Widgets:**
    - [ ] Set up the core infrastructure for Jetpack Glance widgets.
    - [ ] Architect the `WidgetStateRepository` with `DataStore`.
    - [ ] Implement the four widgets: Status, Handshake Log, Quick Actions, and Leaderboard.
- [ ] **Finalize for Production (R8/ProGuard):**
    - [ ] Enable R8 minification and resource shrinking for release builds.
    - [ ] Add comprehensive ProGuard rules to `proguard-rules.pro` to prevent issues with reflection-based libraries.
    - [ ] Set `android:allowBackup="false"` in the `AndroidManifest.xml`.