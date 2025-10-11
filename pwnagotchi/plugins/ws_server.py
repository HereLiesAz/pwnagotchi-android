import logging
import asyncio
import websockets
import json
import pwnagotchi.plugins as plugins
import threading

class WSServer(plugins.Plugin):
    __author__ = 'Your Name'
    __version__ = '1.0.0'
    __license__ = 'GPL3'
    __description__ = 'A plugin to expose pwnagotchi data over a websocket.'

    def __init__(self):
        self.loop = None
        self.server_thread = None
        self.server = None

    def _server_handler(self, websocket, path):
        # This function will be called whenever a client connects to the server.
        # For now, it just keeps the connection open.
        try:
            while True:
                asyncio.sleep(1)
        except websockets.exceptions.ConnectionClosed:
            pass

    def _start_server_thread(self):
        self.loop = asyncio.new_event_loop()
        asyncio.set_event_loop(self.loop)
        self.server = websockets.serve(self._server_handler, "0.0.0.0", 8765)
        self.loop.run_until_complete(self.server)
        self.loop.run_forever()

    def on_loaded(self):
        # This method is called when the plugin is loaded.
        logging.info("ws_server plugin loaded.")
        self.server_thread = threading.Thread(target=self._start_server_thread)
        self.server_thread.start()

    def on_unload(self, ui):
        # This method is called when the plugin is unloaded.
        if self.loop:
            self.loop.call_soon_threadsafe(self.loop.stop)
        if self.server_thread:
            self.server_thread.join()
        logging.info("WebSocket server stopped.")

    def on_ui_update(self, ui):
        # This method is called when the UI is updated.
        # We can send the UI data to the clients.
        if self.loop and self.server:
            asyncio.run_coroutine_threadsafe(self._broadcast(json.dumps(ui.get_data())), self.loop)

    async def _broadcast(self, message):
        # Send a message to all connected clients.
        if self.server and self.server.websockets:
            for websocket in self.server.websockets:
                await websocket.send(message)
