@echo off
echo Starting Muxin Gateway...
echo.
echo Building project...
call mvnw.cmd clean package -DskipTests
echo.
echo Starting gateway...
cd gateway
java -jar target/gateway-1.0-SNAPSHOT.jar
pause 