#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
BASE_DIR="$SCRIPT_DIR"

APP_NAME="gateway-main"
APP_JAR="$BASE_DIR/$APP_NAME-${project.version}.jar"
LOG_DIR="$BASE_DIR/logs"
PID_FILE="$BASE_DIR/$APP_NAME.pid"

JAVA_OPTS="${JAVA_OPTS:--Xms512m -Xmx1024m -XX:+UseG1GC}"
SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-sqlite}"

if [ ! -f "$APP_JAR" ]; then
    echo "Error: Application JAR not found at $APP_JAR"
    exit 1
fi

if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if ps -p "$PID" > /dev/null 2>&1; then
        echo "Application is already running with PID $PID"
        exit 1
    else
        rm -f "$PID_FILE"
    fi
fi

mkdir -p "$LOG_DIR"
mkdir -p "$BASE_DIR/data"

echo "Starting $APP_NAME..."
echo "Java Options: $JAVA_OPTS"
echo "Spring Profile: $SPRING_PROFILES_ACTIVE"

cd "$BASE_DIR"

nohup java $JAVA_OPTS \
    -jar "$APP_JAR" \
    --spring.config.additional-location="file:$BASE_DIR/" \
    --spring.profiles.active="$SPRING_PROFILES_ACTIVE" \
    --server.port=9191 \
    --muxin.gateway.netty.server.port=9292 \
    --spring.cloud.nacos.discovery.enabled=false \
    --spring.cloud.nacos.discovery.register-enabled=false \
    > "$LOG_DIR/console.log" 2>&1 &

PID=$!
echo $PID > "$PID_FILE"

echo "Application started with PID $PID"
echo "Console log: $LOG_DIR/console.log"

sleep 3

if ps -p "$PID" > /dev/null 2>&1; then
    echo "Application is running successfully"
    echo "Admin UI: http://localhost:9191"
    echo "Gateway Port: 9292"
else
    echo "Error: Application failed to start. Check logs at $LOG_DIR/console.log"
    rm -f "$PID_FILE"
    exit 1
fi