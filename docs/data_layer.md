# Data Layer

This document describes the data layer of the Pwnagotchi Android application.

The data layer is responsible for handling all data-related operations, including network requests, database operations, and caching.

## Components

*   **WebSocket Client:** The WebSocket client is responsible for communicating with the Pwnagotchi's WebSocket server. It is implemented using the `Java-WebSocket` library.
*   **ViewModel:** The ViewModel is responsible for managing the UI state and exposing it to the UI. It is implemented using the `androidx.lifecycle.ViewModel` class.
*   **Service:** The Service is responsible for managing the WebSocket connection in the background. It is implemented using the `android.app.Service` class.
