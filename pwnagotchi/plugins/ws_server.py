import logging
import asyncio
import websockets
import json
import ssl
import pathlib
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
                    elif command == "install_community_plugin":
                        plugin_name = data.get("plugin_name")
                        self._install_community_plugin(plugin_name)
                        await self._send_plugin_list(websocket)
                except json.JSONDecodeError:
                    logging.error("ws_server: Invalid JSON received.")
                except Exception as e:
                    logging.error(f"ws_server: Error processing message: {e}")
        except websockets.exceptions.ConnectionClosed:
            pass

    def _start_server_thread(self):
        self.loop = asyncio.new_event_loop()
        asyncio.set_event_loop(self.loop)
        host = self.options.get('host', '127.0.0.1')
        port = self.options.get('port', 8765)

        ssl_context = ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER)
        cert_pem = pathlib.Path(__file__).with_name("cert.pem")
        key_pem = pathlib.Path(__file__).with_name("key.pem")
        ssl_context.load_cert_chain(cert_pem, key_pem)

        self.server = websockets.serve(self._server_handler, host, port, ssl=ssl_context)
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
            # NOTE: Accessing the private _state member is not ideal, but it's the most
            # direct way to get the full UI state. This could break in future versions
            # of Pwnagotchi if the internal implementation changes.
            state = ui._state._state
            safe_fields = ['channel', 'aps', 'uptime', 'shakes', 'mode', 'face', 'status']
            sanitized_state = {k: state[k]['value'] for k in safe_fields if k in state}
            data = {'type': 'ui_update', 'data': sanitized_state}
            asyncio.run_coroutine_threadsafe(self._broadcast(json.dumps(data)), self.loop)

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

    def _install_community_plugin(self, plugin_name):
        import subprocess
        import sys
        import re
        # Sanitize the plugin name to prevent command injection
        if not re.match(r"^[a-zA-Z0-9_-]+/[a-zA-Z0-9_-]+$", plugin_name):
            logging.error(f"Invalid plugin name: {plugin_name}")
            return
        plugin_url = f"https://github.com/{plugin_name}.git"
        plugin_path = f"/opt/pwnagotchi/custom-plugins/{plugin_name.split('/')[1]}"
        try:
            subprocess.check_call([sys.executable, "-m", "pip", "install", "--target", plugin_path, f"git+{plugin_url}"])
            logging.info(f"Successfully installed community plugin: {plugin_name}")
        except subprocess.CalledProcessError as e:
            logging.error(f"Failed to install community plugin: {plugin_name}. Error: {e}")
