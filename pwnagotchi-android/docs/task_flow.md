# Task Flow

This document describes the task flow of the Pwnagotchi Android Super App.

## Client Mode

1.  The user opens the application.
2.  The application automatically connects to the last used IP address.
3.  If the user wants to change the IP address, they can navigate to the Settings screen.
4.  The main screen displays the connection status, data from the Pwnagotchi, and a list of captured handshakes.
5.  The user can manually connect or disconnect from the Pwnagotchi.
6.  The user can navigate to the Plugins screen to manage 3rd-party plugins.
7.  The user can navigate to the oPwngrid screen to view the leaderboard.

## Native Mode

1.  The user opens the application.
2.  The Compatibility Screen checks for root access and a compatible WiFi chipset.
3.  If the device is compatible, the user can proceed to the Setup Screen.
4.  The Setup Screen guides the user through the process of setting up their device to run as a Pwnagotchi.
5.  Once the setup is complete, the Main Screen displays the live status information from the `bettercap` service.
