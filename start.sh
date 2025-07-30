#!/bin/bash
echo "Starting Muxin Gateway..."
echo ""
echo "Building project..."
./mvnw clean package -DskipTests
echo ""
echo "Starting gateway..."
cd gateway
java -jar target/gateway-1.0-SNAPSHOT.jar 