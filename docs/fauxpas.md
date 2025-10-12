# Faux Pas

This document lists common mistakes and how to avoid them when working on the Pwnagotchi Android application.

*   **Blocking the main thread:** All network and database operations should be performed on a background thread to avoid blocking the main thread.
*   **Forgetting to handle errors:** All network and database operations should have proper error handling to avoid crashes.
*   **Not unsubscribing from observables:** All observables should be unsubscribed from when they are no longer needed to avoid memory leaks.
