#!/bin/bash

# Rebuild and restart frontend container
# Always uses spring-app:8080 and cmips_app-network

set -e

echo "🛑 Stopping existing container..."
docker stop timesheet-frontend 2>/dev/null || true
docker rm timesheet-frontend 2>/dev/null || true

echo "🔨 Building frontend image (using spring-app:8080)..."
docker build -t timesheet-frontend:latest .

echo "🚀 Starting container on cmips_app-network..."
docker run -d \
  --name timesheet-frontend \
  --restart unless-stopped \
  --network cmips_app-network \
  -p 3000:3000 \
  -e NEXT_PUBLIC_API_URL=http://spring-app:8080 \
  timesheet-frontend:latest

echo "✅ Frontend container started!"
echo "📊 Container status:"
docker ps | grep timesheet-frontend

echo ""
echo "🌐 Frontend available at: http://localhost:3000"
echo "📝 View logs: docker logs -f timesheet-frontend"

