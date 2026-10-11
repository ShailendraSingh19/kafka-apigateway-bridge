from http.server import BaseHTTPRequestHandler, HTTPServer
from cryptography.hazmat.primitives import serialization, hashes
from cryptography.hazmat.primitives.serialization import pkcs12
from cryptography.hazmat.primitives.asymmetric import padding
from cryptography.hazmat.primitives.ciphers.aead import AESGCM
import base64
import json


# ============================================================
# Configuration
# ============================================================

KEYSTORE_PATH = r"E:\webs\iObservability\kafka-apigateway-bridge\certs\bridge-test.p12"
KEYSTORE_PASSWORD = b"changeit"


# ============================================================
# Load RSA private key from PKCS#12 keystore
# ============================================================

def load_private_key():

    with open(KEYSTORE_PATH, "rb") as f:
        p12_data = f.read()

    private_key, certificate, additional_certificates = (
        pkcs12.load_key_and_certificates(
            p12_data,
            KEYSTORE_PASSWORD
        )
    )

    if private_key is None:
        raise RuntimeError("No private key found in PKCS#12 keystore")

    print("RSA private key loaded successfully")

    return private_key


PRIVATE_KEY = load_private_key()


# ============================================================
# Mock Apigee HTTP Handler
# ============================================================

class MockApigeeHandler(BaseHTTPRequestHandler):

    def do_POST(self):

        print(f"\nReceived POST: {self.path}")

        # ----------------------------------------------------
        # Read HTTP request body
        # ----------------------------------------------------

        content_length = int(self.headers.get("Content-Length", 0))
        raw_body = self.rfile.read(content_length)

        print("Content-Length:", content_length)

        # ----------------------------------------------------
        # OAuth endpoint
        # ----------------------------------------------------

        if self.path == "/oauth/token":

            body = raw_body.decode("utf-8")

            print("OAuth request received")
            print("Grant type:", body)

            response = {
                "access_token": "LOCAL_MOCK_ACCESS_TOKEN",
                "token_type": "Bearer",
                "expires_in": 3599,
                "status": "approved"
            }

            self.send_json(200, response)

            return

        # ----------------------------------------------------
        # Alerts endpoint
        # ----------------------------------------------------

        elif self.path == "/api/alerts":

            print(
                "Authorization:",
                self.headers.get("Authorization")
            )

            print(
                "API Key:",
                self.headers.get("apikey")
            )

            # ------------------------------------------------
            # Validate request body
            # ------------------------------------------------

            if not raw_body:
                print("Request body is empty")

                self.send_json(
                    400,
                    {"error": "Request body is empty"}
                )

                return

            # ------------------------------------------------
            # Parse JSON request
            # ------------------------------------------------

            try:

                request = json.loads(
                    raw_body.decode("utf-8")
                )

            except (UnicodeDecodeError, json.JSONDecodeError) as e:

                print("Invalid JSON:", e)

                self.send_json(
                    400,
                    {"error": "Invalid JSON request body"}
                )

                return

            print("\nEncrypted request received:")
            print(json.dumps(request, indent=2))

            # ------------------------------------------------
            # Extract encrypted fields
            # ------------------------------------------------

            encrypted_key_b64 = request.get("encryptedKey")
            encrypted_data_b64 = request.get("encryptedData")

            if not encrypted_key_b64:
                self.send_json(
                    400,
                    {"error": "encryptedKey is missing"}
                )

                return

            if not encrypted_data_b64:
                self.send_json(
                    400,
                    {"error": "encryptedData is missing"}
                )

                return

            # ------------------------------------------------
            # Decrypt AES key using RSA private key
            # ------------------------------------------------

            try:

                encrypted_key = base64.b64decode(
                    encrypted_key_b64
                )

                aes_key = PRIVATE_KEY.decrypt(
                    encrypted_key,
                    padding.OAEP(
                        mgf=padding.MGF1(
                            algorithm=hashes.SHA256()
                        ),
                        algorithm=hashes.SHA256(),
                        label=None
                    )
                )

                print("\nRSA-OAEP decryption successful")
                print(
                    "Recovered AES key length:",
                    len(aes_key),
                    "bytes"
                )

            except Exception as e:

                print("\nRSA decryption failed:")
                print(e)

                self.send_json(
                    400,
                    {"error": "RSA decryption failed"}
                )

                return

            # ------------------------------------------------
            # Decode encrypted data
            # ------------------------------------------------

            try:

                encrypted_data = base64.b64decode(
                    encrypted_data_b64
                )

                # Our Java encryption format is:
                #
                # [12-byte IV][AES-GCM ciphertext + 16-byte tag]

                iv = encrypted_data[:12]
                ciphertext = encrypted_data[12:]

                print("IV length:", len(iv))
                print(
                    "Encrypted data length:",
                    len(ciphertext)
                )

            except Exception as e:

                print("\nEncrypted data decoding failed:")
                print(e)

                self.send_json(
                    400,
                    {"error": "Invalid encryptedData"}
                )

                return

            # ------------------------------------------------
            # AES-256-GCM decryption
            # ------------------------------------------------

            try:

                aesgcm = AESGCM(aes_key)

                decrypted_data = aesgcm.decrypt(
                    iv,
                    ciphertext,
                    None
                )

                decrypted_text = decrypted_data.decode(
                    "utf-8"
                )

                print("\n========================================")
                print("DECRYPTION SUCCESSFUL")
                print("========================================")

                print("\nDecrypted payload:")

                try:

                    decrypted_json = json.loads(
                        decrypted_text
                    )

                    print(
                        json.dumps(
                            decrypted_json,
                            indent=2
                        )
                    )

                except json.JSONDecodeError:

                    print(decrypted_text)

                print("\n========================================")

            except Exception as e:

                print("\nAES-GCM decryption failed:")
                print(e)

                self.send_json(
                    400,
                    {"error": "AES-GCM decryption failed"}
                )

                return

            # ------------------------------------------------
            # Successful mock response
            # ------------------------------------------------

            response = {
                "status": "success",
                "message": "Mock incident accepted"
            }

            self.send_json(200, response)

            return

        # ----------------------------------------------------
        # Unknown endpoint
        # ----------------------------------------------------

        else:

            self.send_json(
                404,
                {"error": "Endpoint not found"}
            )


    # ========================================================
    # JSON response helper
    # ========================================================

    def send_json(self, status, data):

        response = json.dumps(data).encode("utf-8")

        self.send_response(status)

        self.send_header(
            "Content-Type",
            "application/json"
        )

        self.send_header(
            "Content-Length",
            str(len(response))
        )

        self.end_headers()

        self.wfile.write(response)


    # ========================================================
    # Suppress default HTTP logging
    # ========================================================

    def log_message(self, format, *args):

        print(format % args)


# ============================================================
# Start server
# ============================================================

server = HTTPServer(
    ("localhost", 8081),
    MockApigeeHandler
)

print("Mock Apigee running at http://localhost:8081")
print("Waiting for requests...")

try:

    server.serve_forever()

except KeyboardInterrupt:

    print("\nMock Apigee stopped.")
    server.server_close()