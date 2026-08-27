#!/bin/sh

set -u

echo "======================================"
echo "Starting ClamAV"
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

echo "======================================"
echo "Starting SecureDrop Scanner"
echo "======================================"

java -jar /app/app.jar

SCANNER_EXIT_CODE=$?

echo "SecureDrop Scanner finished with exit code: $SCANNER_EXIT_CODE"

exit "$SCANNER_EXIT_CODE"