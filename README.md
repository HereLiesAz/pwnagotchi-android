# Pwnagotchi: The Android Super App

This repository contains the source code for the official Pwnagotchi Android application, a "Super App" designed to serve as the ultimate mobile companion for the Pwnagotchi project. The application is architected to operate in two distinct modes, providing a comprehensive mobile solution for both interacting with and *becoming* a Pwnagotchi.

## Project Vision & Modes of Operation

The application's development is guided by a detailed engineering roadmap. For a complete list of planned features and the project's current status, please refer to the official [**Production Readiness Roadmap**](./docs/TODO.md).

### 1. Remote Client Mode (Companion App)
In this mode, the app functions as a sophisticated, feature-rich client and control panel for an external Pwnagotchi device (e.g., a Raspberry Pi). This allows you to monitor, manage, and interact with your Pwnagotchi seamlessly from your Android device.

**Key Features:**
-   **Live Status Dashboard:** A real-time, dynamic view of your Pwnagotchi's mood, status messages, and on-going activity.
-   **Handshake & Plugin Management:** Effortlessly view captured handshakes and manage the plugins running on your remote device.
-   **System Notifications:** Receive timely alerts for important events, such as when a new handshake is captured or when your Pwnagotchi's state changes.
-   **oPwngrid Integration:** Connect to the global Pwnagotchi community by viewing leaderboards and network statistics.
-   **Home Screen Widgets:** Keep an eye on your Pwnagotchi's status, recent handshakes, and more, directly from your phone's home screen using modern Jetpack Glance widgets.

### 2. Local Host Mode (Android as a Pwnagotchi)
This is the most ambitious goal of the project. In this mode, the application aims to transform a compatible, **rooted** Android device *into* a fully functional, standalone Pwnagotchi. This mode leverages the phone's own hardware to run the core Pwnagotchi functionalities natively, untethered from any external device.

**Core Functionality:**
-   **Native Wi-Fi Monitor Mode:** Utilizes the device's Wi-Fi chipset to scan for networks by activating monitor mode through privileged commands.
-   **Embedded `bettercap` Engine:** Runs the powerful `bettercap` toolset directly on the Android OS to perform Wi-Fi scanning, de-authentication attacks, and handshake capture.
-   **Full Pwnagotchi Experience:** Emulates the complete Pwnagotchi software experience, including the AI-driven character engine, the iconic e-ink display (replicated in a persistent notification), and background operation—all on your phone.

<p align="center">
  <small>Join the project community on our server!</small>
  <br/><br/>
  <a href="https://discord.gg/https://discord.gg/btZpkp45gQ" target="_blank" title="Join our community!">
    <img src="https://dcbadge.limes.pink/api/server/https://discord.gg/btZpkp45gQ"/>
  </a>
</p>
<hr/>