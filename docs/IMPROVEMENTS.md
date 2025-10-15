# Pwnagotchi Android: Improvement Roadmap

This document outlines the next steps for improving the Pwnagotchi Android application, focusing on enhancing stability, user experience, and feature completeness.

- [ ] **Step 1: Simplify Reconnection Logic**
    - [ ] Refactor `PwnagotchiService` to simplify the reconnection mechanism.
    - [ ] Remove the `needsReconnect` flag and rely on a more streamlined approach.

- [ ] **Step 2: Streamline Settings**
    - [ ] Remove the redundant `ip_address` from `SharedPreferences` and the `onSaveSettings` function.
    - [ ] The app should only use the `host` for WebSocket connections.

- [ ] **Step 3: Add API Key Input for oPwngrid**
    - [ ] Add a field to the `SettingsScreen` for users to enter their oPwngrid API key.
    - [ ] Store the API key securely in `SharedPreferences`.
    - [ ] Update `OpwngridClient` to use the stored API key.

- [ ] **Step 4: Implement `OpwngridViewModel`**
    - [ ] Create a new `OpwngridViewModel` to manage the state of the oPwngrid feature.
    - [ ] The ViewModel should handle fetching the leaderboard, managing loading and error states, and exposing the data to the UI.

- [ ] **Step 5: Add Loading Indicator to oPwngrid**
    - [ ] Update the `OpwngridScreen` to display a loading indicator while the leaderboard is being fetched.
    - [ ] The screen should also handle and display error states.
