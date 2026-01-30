#!/bin/bash

# ===================================
# Quick Start Script - Base API
# ===================================

set -e

echo "🚀 Starting Base API with Docker Compose..."
echo ""

# Change to local directory
cd "$(dirname "$0")"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Build and start services
echo "📦 Building and starting services..."
docker-compose up -d --build

# Wait for health check
echo ""
echo "⏳ Waiting for application to be healthy..."
MAX_ATTEMPTS=30
ATTEMPT=0

while [ $ATTEMPT -lt $MAX_ATTEMPTS ]; do
    HEALTH=$(docker inspect --format='{{.State.Health.Status}}' base-api-app 2>/dev/null || echo "starting")

    if [ "$HEALTH" = "healthy" ]; then
        echo ""
        echo "✅ Application is healthy!"
        break
    fi

    echo -n "."
    sleep 2
    ATTEMPT=$((ATTEMPT + 1))
done

if [ $ATTEMPT -eq $MAX_ATTEMPTS ]; then
    echo ""
    echo "⚠️  Health check timeout. Check logs with: docker-compose logs"
    exit 1
fi

# Show endpoints
echo ""
echo "╔═══════════════════════════════════════════════════════════╗"
echo "║         Base API - Running Successfully                   ║"
echo "╚═══════════════════════════════════════════════════════════╝"
echo ""
echo "🌐 Available Endpoints:"
echo "  API Base:     http://localhost:8080/base-api"
echo "  Health:       http://localhost:8080/base-api/actuator/health"
echo "  Metrics:      http://localhost:8080/base-api/actuator/metrics"
echo "  H2 Console:   http://localhost:8080/base-api/h2-console"
echo ""
echo "📋 Useful Commands:"
echo "  View logs:    docker-compose logs -f"
echo "  Stop app:     docker-compose down"
echo "  Restart:      docker-compose restart"
echo ""
echo "💡 Use './docker.ps1 help' for more commands (Windows PowerShell)"
echo ""

