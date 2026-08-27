#!/bin/sh

set -u

echo "======================================"
echo "Starting ClamAV daemon"
echo "======================================"

clamd --foreground --config-file=/etc/clamav/clamd.conf &
CLAMD_PID=$!

cleanup() {
    echo "Stopping ClamAV..."

    if kill -0 "$CLAMD_PID" 2>/dev/null; then
        kill -TERM "$CLAMD_PID" 2>/dev/null || true
        wait "$CLAMD_PID" 2>/dev/null || true
    fi
}

trap cleanup EXIT INT TERM

echo "Waiting for ClamAV to become ready..."

ATTEMPT=0

while true
do
    RESPONSE="$(echo "PING" | nc 127.0.0.1 3310 2>/dev/null || true)"

    if [ "$RESPONSE" = "PONG" ]; then
        break
    fi

    if ! kill -0 "$CLAMD_PID" 2>/dev/null; then
        echo "ERROR: ClamAV terminated during startup."
        exit 2
    fi

    ATTEMPT=$((ATTEMPT + 1))

    if [ "$ATTEMPT" -ge 90 ]; then
        echo "ERROR: ClamAV did not become ready in time."
        exit 2
    fi

    sleep 2
done

echo "ClamAV is ready."

echo "======================================"
echo "Starting SecureDrop Scanner"
echo "======================================"

java -jar /app/app.jar

SCANNER_EXIT_CODE=$?

echo "SecureDrop Scanner finished with exit code: $SCANNER_EXIT_CODE"

exit "$SCANNER_EXIT_CODE"