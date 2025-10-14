# TODO

This file outlines the pending tasks and future enhancements for the Pwnagotchi Android Super App.

## Client Mode
- [ ] Implement a more robust error handling mechanism for the WebSocket connection.
- [ ] Add a settings screen to configure the Pwnagotchi's IP address and other options.
- [ ] Implement a mechanism to display the captured handshakes.
- [ ] Improve the UI/UX of the application.
- [ ] Add support for notifications when a new handshake is captured.
- [ ] Implement a mechanism to automatically reconnect to the WebSocket server.
- [ ] Enable the user to add 3rd party plugins.
- [ ] Create a persistent notification that displays all the things you would see on the original pi zero with that tiny little eink screen.
- [ ] Add support for themes.
- [ ] Add support for multiple languages.

## Native Mode
- [ ] Build a "Device Compatibility" screen.
- [ ] Implement a "Setup" process.
- [ ] Create a background service to manage `bettercap`.
- [ ] Build the main UI.

## In Progress
- [ ] **Plugin Community Store:** The basic UI and mock data are implemented. Next steps are to implement the installation logic.
- [ ] **Android Home Screen Widgets:** The first status widget has been implemented. More widgets are planned.

## Known Issues
- The Gradle wrapper is not fully configured, which prevents building the project from the command line. The project can still be built using Android Studio.
