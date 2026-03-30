#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
BASE_DIR="$SCRIPT_DIR"

APP_NAME="gateway-main"
PID_FILE="$BASE_DIR/$APP_NAME.pid"

echo "=== $APP_NAME Status ==="

if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if ps -p "$PID" > /dev/null 2>&1; then
        echo "Status: RUNNING"
        echo "PID: $PID"
        
        ps -p "$PID" -o pid,vsz,rss,etime,cmd | tail -n 1
        
        echo ""
        echo "=== Recent Logs ==="
        tail -n 20 "$BASE_DIR/logs/console.log" 2>/dev/null || echo "No console log found"
    else
        echo "Status: STOPPED (stale PID file)"
        rm -f "$PID_FILE"
    fi
else
    PIDS=$(pgrep -f "$APP_NAME.*\.jar")
    if [ -n "$PIDS" ]; then
        echo "Status: RUNNING (without PID file)"
        echo "PIDs: $PIDS"
    else
        echo "Status: STOPPED"
    fi
fi

echo ""
echo "=== Health Check ==="
curl -s -o /dev/null -w "Admin UI (9191): %{http_code}\n" http://localhost:9191 2>/dev/null || echo "Admin UI (9191): Not reachable"
curl -s -o /dev/null -w "Gateway (9292): %{http_code}\n" http://localhost:9292 2>/dev/null || echo "Gateway (9292): Not reachable"