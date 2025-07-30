#!/bin/bash

# 启动Muxin Gateway后端服务

echo "Starting Muxin Gateway backend service..."

# 启动gateway-admin模块
cd gateway-admin
mvn spring-boot:run &
ADMIN_PID=$!

echo "Gateway Admin started with PID: $ADMIN_PID"

# 等待服务启动
echo "Waiting for services to start..."
sleep 10

# 检查服务是否启动成功
curl -s http://localhost:8080/admin/api/auth/login > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo "Gateway Admin service is running successfully!"
else
    echo "Failed to start Gateway Admin service!"
fi

# 保持脚本运行
echo "Press Ctrl+C to stop all services"
wait $ADMIN_PID 