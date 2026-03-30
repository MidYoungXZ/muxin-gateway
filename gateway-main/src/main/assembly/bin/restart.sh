#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
BASE_DIR="$SCRIPT_DIR"

APP_NAME="gateway-main"
PID_FILE="$BASE_DIR/$APP_NAME.pid"
LOG_DIR="$BASE_DIR/logs"

echo "Stopping $APP_NAME..."
"$SCRIPT_DIR/stop.sh"

echo ""
echo "Cleaning up logs..."
rm -rf "$LOG_DIR"/*.log 2>/dev/null

echo ""
echo "Starting $APP_NAME..."
"$SCRIPT_DIR/run.sh"