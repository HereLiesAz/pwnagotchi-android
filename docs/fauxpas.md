# Faux Pas

This document lists common mistakes and how to avoid them when working on the Pwnagotchi Android Super App.

## Client Mode

*   **Forgetting to handle WebSocket errors:** All WebSocket operations should have proper error handling to avoid crashes.
*   **Not unsubscribing from observables:** All observables should be unsubscribed from when they are no longer needed to avoid memory leaks.

## Native Mode

*   **Blocking the main thread:** All shell commands should be executed on a background thread to avoid blocking the main thread.
*   **Forgetting to handle root errors:** All root operations should have proper error handling to avoid crashes.
*   **Making incorrect assumptions about the device:** The app should not make assumptions about the device's hardware or software configuration.
