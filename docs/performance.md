# Performance

This document provides information about the performance of the Pwnagotchi Android Super App.

## Client Mode

In Client Mode, the application is designed to be lightweight and efficient. The WebSocket connection is managed in a background service to avoid blocking the main thread. The UI is built using Jetpack Compose, which is a modern and efficient UI toolkit.

## Native Mode

In Native Mode, the application's performance will depend on the device's hardware. Running `bettercap` can be CPU-intensive, so the app may consume a significant amount of battery.

## Future Enhancements

*   Implement a caching mechanism to reduce network requests in Client Mode.
*   Optimize the UI for better performance.
*   Optimize the `bettercap` process for better performance in Native Mode.
