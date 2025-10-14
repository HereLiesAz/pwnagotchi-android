# Pwnagotchi Android Super App: Production Roadmap

This document outlines the step-by-step roadmap to take the Pwnagotchi Android Super App from its current state to a polished, production-ready product.

---

## Phase 1: Stabilize Core Features & Address Feedback

### 1.1: Robustness & Error Handling
- [x] **Native Mode:** Implement a dynamic check for WiFi interface names instead of hardcoding `wlan0`.
- [x] **Native Mode:** Provide a selection of monitor mode commands or detect chipset to improve compatibility.
- [x] **Native Mode:** Add comprehensive error handling to the `bettercap` service, capturing stderr and displaying user-friendly error messages.
- [ ] **Client Mode:** Implement a limit on WebSocket reconnection attempts to prevent infinite loops.
- [ ] **Client Mode:** Add checks to ensure WebSocket is open before sending any commands.

### 1.2: Security Hardening
- [ ] **Client Mode:** Implement `wss://` (secure WebSockets) support in the `ws_server.py` plugin and the Android client. This will require generating self-signed SSL certificates.
- [ ] **Client Mode:** Add a configuration option to the Python plugin to bind the WebSocket server to `localhost` by default for enhanced security.
- [ ] **Native Mode:** Thoroughly validate all user-provided input (like interface names) to prevent any potential for command injection.

### 1.3: UI Polish & UX Refinement
- [ ] Implement a proper navigation architecture using the **Jetpack Navigation Component** to replace the current boolean-based routing.
- [ ] Refine the layout of all screens for better visual consistency and usability.
- [ ] Add loading indicators for all asynchronous operations (e.g., fetching plugin lists).
- [ ] Create a more intuitive settings screen, grouping related options.

---

## Phase 2: Complete Feature Implementation

### 2.1: Pwnagotchi Character Engine (Advanced)
- [ ] Replace the current text-based face with **image-based sprites** for a more visually appealing character.
- [ ] Implement support for custom themes and faces, potentially loading them from user storage.
- [ ] Animate face transitions to make the character feel more alive.

### 2.2: Plugin Community Store (Full Implementation)
- [ ] **Python Plugin:** Implement the server-side logic to actually download and install community plugins. This will involve fetching plugin files from their repositories.
- [ ] **Android App:** Build the UI for the plugin detail screen, showing more information about each community plugin.
- [ ] **Android App:** Implement the "Install" button functionality, which will send a command to the Python plugin to download and enable the selected community plugin.

### 2.3: oPwngrid Integration (Full Implementation)
- [ ] Research and find the correct, stable API endpoints for the oPwngrid leaderboard and statistics.
- [ ] Build out the UI to display all available oPwngrid data.
- [ ] Implement the functionality for the app to submit data (e.g., handshakes, location data) to oPwngrid.

---

## Phase 3: New Feature Development

### 3.1: Automatic Remote Instance Setup
- [ ] **Research:** Investigate the best technologies for remote device provisioning (e.g., SSH libraries for Android, Ansible, or custom scripts).
- [ ] **UI:** Design a user-friendly wizard to guide users through the process of connecting to a new Raspberry Pi.
- [ ] **Implementation:** Build the logic to remotely execute setup commands on the Raspberry Pi over SSH.

### 3.2: Android Home Screen Widgets
- [ ] Design 4 more distinct widgets (in addition to the current status widget). Ideas include:
    - A widget dedicated to the last captured handshake.
    - A widget showing a summary of the current session (APs, handshakes).
    - A widget with quick-action buttons (e.g., restart pwnagotchi).
    - A resizable widget showing a log of recent events.
- [ ] Implement the background logic to keep all widgets updated.

### 3.3: Wearable Widgets
- [ ] **Research:** Investigate the development process for Android Wear and Samsung Wear (Tizen/Wear OS).
- [ ] **Design:** Create a simple and effective UI for a watch face or widget that displays essential Pwnagotchi status.
- [ ] **Implementation:** Build the wearable app/widget.

---

## Phase 4: Release Preparation

### 4.1: Comprehensive Testing
- [ ] Write unit tests for ViewModels and client logic.
- [ ] Write instrumented tests for UI components and navigation.
- [ ] Conduct manual end-to-end testing on a variety of real devices (if possible) for both Client and Native modes.

### 4.2: Final Documentation and Release
- [ ] Update all documentation in the `/docs` folder to be complete and accurate.
- [ ] Prepare screenshots and promotional materials for the app.
- [ ] Create a signing key and build a release version of the APK.
- [ ] Publish the application to the Google Play Store or another app store.
