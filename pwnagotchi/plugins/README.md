## ws_server Plugin

The `ws_server` plugin exposes pwnagotchi data over a secure WebSocket. To use this plugin, you will need to generate a self-signed SSL certificate and key. You can do this with the following command:

```bash
openssl req -x509 -newkey rsa:2048 -keyout pwnagotchi/key.pem -out pwnagotchi/cert.pem -days 365 -nodes -subj "/C=US/ST=California/L=San Francisco/O=Pwnagotchi/OU=Pwnagotchi/CN=pwnagotchi.local"
```

This will create `key.pem` and `cert.pem` files in the `pwnagotchi` directory. These files are ignored by git.
