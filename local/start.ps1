# ===================================
# Quick Start Script - Base API
# ===================================

Write-Host "╔═══════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║         Base API - Docker Quick Start                    ║" -ForegroundColor Cyan
Write-Host "╚═══════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Check if Docker is running
Write-Host "🔍 Verificando Docker..." -ForegroundColor Yellow
try {
    docker info | Out-Null
    Write-Host "✅ Docker está ejecutándose" -ForegroundColor Green
} catch {
    Write-Host "❌ Docker no está ejecutándose. Por favor inicia Docker Desktop primero." -ForegroundColor Red
    exit 1
}

# Build and start services
Write-Host ""
Write-Host "📦 Construyendo y levantando servicios..." -ForegroundColor Yellow
docker-compose up -d --build

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Error al iniciar servicios" -ForegroundColor Red
    exit 1
}

# Wait for health check
Write-Host ""
Write-Host "⏳ Esperando a que la aplicación esté saludable..." -ForegroundColor Yellow
$maxAttempts = 30
$attempt = 0
$healthy = $false

while ($attempt -lt $maxAttempts -and -not $healthy) {
    Start-Sleep -Seconds 2
    $health = docker inspect --format='{{.State.Health.Status}}' base-api-app 2>$null

    if ($health -eq "healthy") {
        $healthy = $true
        break
    }

    Write-Host "." -NoNewline -ForegroundColor Gray
    $attempt++
}

Write-Host ""

if ($healthy) {
    Write-Host "✅ ¡Aplicación saludable!" -ForegroundColor Green
} else {
    Write-Host "⚠️  Timeout en health check. Verifica logs con: docker-compose logs" -ForegroundColor Yellow
}

# Show endpoints
Write-Host ""
Write-Host "╔═══════════════════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║         Base API - Running Successfully                   ║" -ForegroundColor Green
Write-Host "╚═══════════════════════════════════════════════════════════╝" -ForegroundColor Green
Write-Host ""
Write-Host "🌐 Endpoints Disponibles:" -ForegroundColor Cyan
Write-Host "  API Base:     " -NoNewline
Write-Host "http://localhost:8080/base-api" -ForegroundColor Blue
Write-Host "  Health:       " -NoNewline
Write-Host "http://localhost:8080/base-api/actuator/health" -ForegroundColor Blue
Write-Host "  Metrics:      " -NoNewline
Write-Host "http://localhost:8080/base-api/actuator/metrics" -ForegroundColor Blue
Write-Host "  H2 Console:   " -NoNewline
Write-Host "http://localhost:8080/base-api/h2-console" -ForegroundColor Blue
Write-Host ""
Write-Host "🔑 Credenciales H2:" -ForegroundColor Cyan
Write-Host "  JDBC URL:     jdbc:h2:mem:testdb"
Write-Host "  User:         sa"
Write-Host "  Password:     (vacío)"
Write-Host ""
Write-Host "📋 Comandos Útiles:" -ForegroundColor Cyan
Write-Host "  Ver logs:     " -NoNewline
Write-Host "docker-compose logs -f" -ForegroundColor Yellow
Write-Host "  Detener:      " -NoNewline
Write-Host "docker-compose down" -ForegroundColor Yellow
Write-Host "  Reiniciar:    " -NoNewline
Write-Host "docker-compose restart" -ForegroundColor Yellow
Write-Host "  Gestión:      " -NoNewline
Write-Host ".\docker.ps1 help" -ForegroundColor Yellow
Write-Host ""
Write-Host "🎉 ¡La aplicación está lista para usar!" -ForegroundColor Green
Write-Host ""

