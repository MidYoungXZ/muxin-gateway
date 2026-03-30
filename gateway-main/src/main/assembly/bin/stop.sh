#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
BASE_DIR="$SCRIPT_DIR"

APP_NAME="gateway-main"
PID_FILE="$BASE_DIR/$APP_NAME.pid"
SHUTDOWN_TIMEOUT=30

if [ ! -f "$PID_FILE" ]; then
    echo "PID file not found at $PID_FILE"
    echo "Application may not be running or was not started with run.sh"
    
    PIDS=$(pgrep -f "$APP_NAME.*\.jar")
    if [ -n "$PIDS" ]; then
        echo "Found running processes: $PIDS"
        read -p "Do you want to kill these processes? (y/N): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            for PID in $PIDS; do
                kill "$PID" && echo "Killed process $PID"
            done
        fi
    fi
    exit 0
fi

PID=$(cat "$PID_FILE")

if ! ps -p "$PID" > /dev/null 2>&1; then
    echo "Process with PID $PID is not running"
    rm -f "$PID_FILE"
    exit 0
fi

echo "Stopping $APP_NAME with PID $PID..."
kill "$PID"

COUNT=0
while ps -p "$PID" > /dev/null 2>&1; do
    sleep 1
    COUNT=$((COUNT + 1))
    if [ $COUNT -ge $SHUTDOWN_TIMEOUT ]; then
        echo "Application did not stop gracefully within ${SHUTDOWN_TIMEOUT}s, forcing shutdown..."
        kill -9 "$PID"
        break
    fi
    echo "Waiting for application to stop... ($COUNT/$SHUTDOWN_TIMEOUT)"
done

if ps -p "$PID" > /dev/null 2>&1; then
    echo "Error: Failed to stop application"
    exit 1
else
    echo "Application stopped successfully"
    rm -f "$PID_FILE"
fi