# Authentication and Authorization

This document describes the authentication and authorization mechanisms for the Pwnagotchi Android Super App.

## Client Mode

In Client Mode, the application does not have any authentication or authorization mechanisms. The WebSocket server is open and does not require any authentication. This is a potential security risk and should be addressed in the future.

## Native Mode

In Native Mode, the application requires root access to function. This is a significant security risk, and the user should be made aware of the implications of granting root access.

## Future Enhancements

*   Implement a token-based authentication system for the WebSocket server in Client Mode.
*   Add support for user accounts and roles.
*   Implement a more granular permission model for Native Mode.
