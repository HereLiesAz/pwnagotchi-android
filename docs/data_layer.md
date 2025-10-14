# Data Layer

This document describes the data layer of the Pwnagotchi Android Super App.

## Client Mode

In Client Mode, the data layer is responsible for handling all data-related operations with the external Pwnagotchi device. This includes:

*   **WebSocket Client:** The WebSocket client is responsible for communicating with the Pwnagotchi's WebSocket server. It is implemented using the `Java-WebSocket` library.
*   **ViewModel:** The ViewModel is responsible for managing the UI state and exposing it to the UI. It is implemented using the `androidx.lifecycle.ViewModel` class.
*   **Service:** The Service is responsible for managing the WebSocket connection in the background. It is implemented using the `android.app.Service` class.

## Native Mode

In Native Mode, the data layer is responsible for handling all data-related operations with the `bettercap` process running on the Android device. This includes:

*   **Bettercap Service:** The Bettercap Service is responsible for managing the `bettercap` process in the background. It is implemented using the `android.app.Service` class.
*   **ViewModel:** The ViewModel is responsible for managing the UI state and exposing it to the UI. It is implemented using the `androidx.lifecycle.ViewModel` class.
*   **Shell:** The Shell is responsible for executing the `bettercap` command and reading its output. It is implemented using the `libsu` library.
