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
        self.community_plugins = [
            {"name": "GPS-plus", "description": "Displays GPS coordinates, like the original plugin, but with added options."},
            {"name": "Net-Pos", "description": "Saves a json file with the access points nearby whenever a handshake is captured."},
            {"name": "f0xtr0t", "description": "A plugin that shows a openstreetmap with positions of ap-handshakes in your webbrowser."},
            {"name": "Wardriver", "description": "Wardriving plugin for wigle!"},
        ]

    async def _server_handler(self, websocket, path):
        try:
            async for message in websocket:
                try:
                    data = json.loads(message)
                    command = data.get("command")
                    if command == "list_plugins":
                        await self._send_plugin_list(websocket)
                    elif command == "toggle_plugin":
                        plugin_name = data.get("plugin_name")
                        enabled = data.get("enabled")
                        self._toggle_plugin(plugin_name, enabled)
                        await self._send_plugin_list(websocket)
                    elif command == "get_community_plugins":
                        await self._send_community_plugin_list(websocket)
                except json.JSONDecodeError:
                    logging.error("ws_server: Invalid JSON received.")
                except Exception as e:
                    logging.error(f"ws_server: Error processing message: {e}")
        except websockets.exceptions.ConnectionClosed:
            pass

    def _start_server_thread(self):
        self.loop = asyncio.new_event_loop()
        asyncio.set_event_loop(self.loop)
        self.server = websockets.serve(self._server_handler, "0.0.0.0", 8765)
        self.loop.run_until_complete(self.server)
        self.loop.run_forever()

    def on_loaded(self):
        logging.info("ws_server plugin loaded.")
        self.server_thread = threading.Thread(target=self._start_server_thread)
        self.server_thread.start()

    def on_unload(self, ui):
        if self.loop:
            self.loop.call_soon_threadsafe(self.loop.stop)
        if self.server_thread:
            self.server_thread.join(timeout=5)
            if self.server_thread.is_alive():
                logging.warning("WebSocket server thread did not exit within timeout.")
        logging.info("WebSocket server stopped.")

    def on_ui_update(self, ui):
        if self.loop and self.server:
            data = {'type': 'ui_update', 'data': ui._state._state}
            asyncio.run_coroutine_threadsafe(self._broadcast(json.dumps(data, default=lambda o: '<not serializable>')), self.loop)

    def on_handshake(self, agent, filename, ap, sta):
        if self.loop and self.server:
            data = {'type': 'handshake', 'data': {'ap': ap, 'sta': sta, 'filename': filename}}
            asyncio.run_coroutine_threadsafe(self._broadcast(json.dumps(data, default=lambda o: '<not serializable>')), self.loop)

    async def _broadcast(self, message):
        if self.server and self.server.websockets:
            await asyncio.wait([ws.send(message) for ws in self.server.websockets])

    async def _send_plugin_list(self, websocket):
        plugin_list = [
            {"name": name, "enabled": name in plugins.loaded}
            for name, loaded in plugins.database.items()
        ]
        await websocket.send(json.dumps({"type": "plugin_list", "data": plugin_list}))

    def _toggle_plugin(self, plugin_name, enabled):
        plugins.toggle_plugin(plugin_name, enabled)

    async def _send_community_plugin_list(self, websocket):
        await websocket.send(json.dumps({"type": "community_plugin_list", "data": self.community_plugins}))
