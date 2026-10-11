from http.server import BaseHTTPRequestHandler, HTTPServer
import json


class MockApigeeHandler(BaseHTTPRequestHandler):

    def do_POST(self):
        print(f"\nReceived POST: {self.path}")

        # Read request body
        content_length = int(self.headers.get("Content-Length", 0))
        raw_body = self.rfile.read(content_length)

        print("Content-Length:", content_length)
        print("Raw body:", repr(raw_body))

        # Decode body safely
        try:
            body = raw_body.decode("utf-8")
        except UnicodeDecodeError as e:
            print("Unable to decode request body:", e)
            self.send_json(400, {"error": "Invalid UTF-8 request body"})
            return

        # OAuth token endpoint
        if self.path == "/oauth/token":
            print("OAuth request received")
            print("Grant type:", body)

            response = {
                "access_token": "LOCAL_MOCK_ACCESS_TOKEN",
                "token_type": "Bearer",
                "expires_in": 3599,
                "status": "approved"
            }

            self.send_json(200, response)

        # Alerts API endpoint
        elif self.path == "/api/alerts":
            print("Authorization:", self.headers.get("Authorization"))
            print("API Key:", self.headers.get("apikey"))

            print("\nEncrypted request received:")

            if not body.strip():
                print("Request body is empty")
                self.send_json(400, {"error": "Request body is empty"})
                return

            try:
                payload = json.loads(body)
                print(json.dumps(payload, indent=2))

            except json.JSONDecodeError as e:
                print("Request body is not valid JSON:", e)
                self.send_json(400, {"error": "Invalid JSON request body"})
                return

            response = {
                "status": "success",
                "message": "Mock incident accepted"
            }

            self.send_json(200, response)

        else:
            self.send_json(404, {"error": "Endpoint not found"})

    def send_json(self, status, data):
        response = json.dumps(data).encode("utf-8")

        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(response)))
        self.end_headers()
        self.wfile.write(response)

    def log_message(self, format, *args):
        print(format % args)


server = HTTPServer(("localhost", 8081), MockApigeeHandler)

print("Mock Apigee running at http://localhost:8081")

try:
    server.serve_forever()
except KeyboardInterrupt:
    print("\nMock Apigee stopped.")
    server.server_close()