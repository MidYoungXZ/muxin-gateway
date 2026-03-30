#!/bin/bash

PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
FRONTEND_DIR="$PROJECT_ROOT/gateway-admin-ui"

echo "=== Muxin Gateway Build Script ==="
echo ""

if [ "$1" == "frontend" ]; then
    echo "Building frontend only..."
    cd "$FRONTEND_DIR"
    npm run build
    echo "Frontend build completed"
    exit 0
fi

if [ "$1" == "backend" ]; then
    echo "Building backend only (skip frontend build)..."
    cd "$PROJECT_ROOT"
    mvn clean package -pl gateway-main -am -DskipTests
    echo "Backend build completed"
    exit 0
fi

echo "Step 1: Building frontend..."
cd "$FRONTEND_DIR"

if [ ! -d "node_modules" ]; then
    echo "Installing npm dependencies..."
    npm install
fi

npm run build

if [ ! -d "dist" ]; then
    echo "Error: Frontend build failed"
    exit 1
fi

echo ""
echo "Step 2: Building backend..."
cd "$PROJECT_ROOT"
mvn clean package -pl gateway-main -am -DskipTests

if [ -f "gateway-main/target/gateway-main-1.0-SNAPSHOT-dist.zip" ]; then
    echo ""
    echo "=== Build Successful ==="
    echo ""
    echo "Distribution package created:"
    echo "  gateway-main/target/gateway-main-1.0-SNAPSHOT-dist.zip"
    echo ""
echo "Deployment steps:"
    echo "  1. Extract gateway-main-1.0-SNAPSHOT-dist.zip"
    echo "  2. cd gateway-main-1.0-SNAPSHOT"
    echo "  3. ./run.sh"
    echo "  4. Open http://localhost:9191"
    echo "     Login: admin / admin123"
else
    echo "Error: Backend build failed"
    exit 1
fi