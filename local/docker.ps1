# ===================================
# Script de Gestión Docker - Base API
# ===================================
# Uso: .\docker.ps1 [comando]
#
# Comandos disponibles:
#   start      - Inicia los servicios en background
#   stop       - Detiene los servicios
#   restart    - Reinicia los servicios
#   logs       - Muestra logs en tiempo real
#   build      - Reconstruye la imagen
#   clean      - Limpia contenedores y volúmenes
#   status     - Muestra estado de servicios
#   health     - Verifica health check
#   shell      - Accede al contenedor
# ===================================

param(
    [Parameter(Position=0)]
    [string]$Command = "help"
)

$ComposeFile = "docker-compose.yml"
$ServiceName = "base-api"
$ContainerName = "base-api-app"

function Show-Help {
    Write-Host "╔═══════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
    Write-Host "║         Base API - Docker Management Script              ║" -ForegroundColor Cyan
    Write-Host "╚═══════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Uso: " -NoNewline -ForegroundColor Yellow
    Write-Host ".\docker.ps1 [comando]" -ForegroundColor White
    Write-Host ""
    Write-Host "Comandos disponibles:" -ForegroundColor Green
    Write-Host "  start       " -NoNewline -ForegroundColor Yellow
    Write-Host "- Inicia los servicios en background"
    Write-Host "  stop        " -NoNewline -ForegroundColor Yellow
    Write-Host "- Detiene los servicios"
    Write-Host "  restart     " -NoNewline -ForegroundColor Yellow
    Write-Host "- Reinicia los servicios"
    Write-Host "  logs        " -NoNewline -ForegroundColor Yellow
    Write-Host "- Muestra logs en tiempo real"
    Write-Host "  build       " -NoNewline -ForegroundColor Yellow
    Write-Host "- Reconstruye la imagen desde cero"
    Write-Host "  rebuild     " -NoNewline -ForegroundColor Yellow
    Write-Host "- Rebuild y reinicia servicios"
    Write-Host "  clean       " -NoNewline -ForegroundColor Yellow
    Write-Host "- Limpia contenedores, volúmenes y red"
    Write-Host "  status      " -NoNewline -ForegroundColor Yellow
    Write-Host "- Muestra estado de servicios"
    Write-Host "  health      " -NoNewline -ForegroundColor Yellow
    Write-Host "- Verifica health check de la aplicación"
    Write-Host "  shell       " -NoNewline -ForegroundColor Yellow
    Write-Host "- Accede al shell del contenedor"
    Write-Host "  stats       " -NoNewline -ForegroundColor Yellow
    Write-Host "- Muestra uso de recursos en tiempo real"
    Write-Host ""
    Write-Host "Ejemplos:" -ForegroundColor Green
    Write-Host "  .\docker.ps1 start" -ForegroundColor Gray
    Write-Host "  .\docker.ps1 logs" -ForegroundColor Gray
    Write-Host "  .\docker.ps1 health" -ForegroundColor Gray
}

function Start-Services {
    Write-Host "🚀 Iniciando servicios..." -ForegroundColor Green
    docker-compose -f $ComposeFile up -d --build
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Servicios iniciados correctamente" -ForegroundColor Green
        Write-Host ""
        Show-Endpoints
    } else {
        Write-Host "❌ Error al iniciar servicios" -ForegroundColor Red
    }
}

function Stop-Services {
    Write-Host "🛑 Deteniendo servicios..." -ForegroundColor Yellow
    docker-compose -f $ComposeFile down
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Servicios detenidos correctamente" -ForegroundColor Green
    } else {
        Write-Host "❌ Error al detener servicios" -ForegroundColor Red
    }
}

function Restart-Services {
    Write-Host "🔄 Reiniciando servicios..." -ForegroundColor Yellow
    docker-compose -f $ComposeFile restart
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Servicios reiniciados correctamente" -ForegroundColor Green
    } else {
        Write-Host "❌ Error al reiniciar servicios" -ForegroundColor Red
    }
}

function Show-Logs {
    Write-Host "📋 Mostrando logs (Ctrl+C para salir)..." -ForegroundColor Cyan
    docker-compose -f $ComposeFile logs -f
}

function Build-Image {
    Write-Host "🔨 Reconstruyendo imagen (sin caché)..." -ForegroundColor Yellow
    docker-compose -f $ComposeFile build --no-cache
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Imagen reconstruida correctamente" -ForegroundColor Green
    } else {
        Write-Host "❌ Error al reconstruir imagen" -ForegroundColor Red
    }
}

function Rebuild-Services {
    Write-Host "🔨 Reconstruyendo y reiniciando servicios..." -ForegroundColor Yellow
    docker-compose -f $ComposeFile up -d --build --force-recreate
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Servicios reconstruidos y reiniciados" -ForegroundColor Green
        Show-Endpoints
    } else {
        Write-Host "❌ Error en rebuild" -ForegroundColor Red
    }
}

function Clean-All {
    Write-Host "🧹 Limpiando contenedores, volúmenes y red..." -ForegroundColor Yellow
    docker-compose -f $ComposeFile down -v --remove-orphans
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Limpieza completada" -ForegroundColor Green
    } else {
        Write-Host "❌ Error en limpieza" -ForegroundColor Red
    }
}

function Show-Status {
    Write-Host "📊 Estado de servicios:" -ForegroundColor Cyan
    docker-compose -f $ComposeFile ps
}

function Check-Health {
    Write-Host "🏥 Verificando health check..." -ForegroundColor Cyan
    $health = docker inspect --format='{{.State.Health.Status}}' $ContainerName 2>$null

    if ($LASTEXITCODE -eq 0) {
        Write-Host "Estado: " -NoNewline
        switch ($health) {
            "healthy" {
                Write-Host "✅ HEALTHY" -ForegroundColor Green
                Write-Host ""
                Write-Host "Probando endpoint de health..." -ForegroundColor Cyan
                curl.exe -s http://localhost:8080/base-api/actuator/health | ConvertFrom-Json | ConvertTo-Json
            }
            "unhealthy" { Write-Host "❌ UNHEALTHY" -ForegroundColor Red }
            "starting" { Write-Host "🔄 STARTING" -ForegroundColor Yellow }
            default { Write-Host "⚠️ $health" -ForegroundColor Yellow }
        }
    } else {
        Write-Host "❌ Contenedor no encontrado o no está ejecutándose" -ForegroundColor Red
    }
}

function Enter-Shell {
    Write-Host "🐚 Accediendo al contenedor..." -ForegroundColor Cyan
    docker exec -it $ContainerName sh
}

function Show-Stats {
    Write-Host "📈 Uso de recursos (Ctrl+C para salir):" -ForegroundColor Cyan
    docker stats $ContainerName
}

function Show-Endpoints {
    Write-Host ""
    Write-Host "🌐 Endpoints disponibles:" -ForegroundColor Cyan
    Write-Host "  API Base:     " -NoNewline
    Write-Host "http://localhost:8080/base-api" -ForegroundColor Blue
    Write-Host "  Health:       " -NoNewline
    Write-Host "http://localhost:8080/base-api/actuator/health" -ForegroundColor Blue
    Write-Host "  Metrics:      " -NoNewline
    Write-Host "http://localhost:8080/base-api/actuator/metrics" -ForegroundColor Blue
    Write-Host "  H2 Console:   " -NoNewline
    Write-Host "http://localhost:8080/base-api/h2-console" -ForegroundColor Blue
    Write-Host ""
}

# Main script logic
switch ($Command.ToLower()) {
    "start" { Start-Services }
    "stop" { Stop-Services }
    "restart" { Restart-Services }
    "logs" { Show-Logs }
    "build" { Build-Image }
    "rebuild" { Rebuild-Services }
    "clean" { Clean-All }
    "status" { Show-Status }
    "health" { Check-Health }
    "shell" { Enter-Shell }
    "stats" { Show-Stats }
    "help" { Show-Help }
    default {
        Write-Host "❌ Comando no reconocido: $Command" -ForegroundColor Red
        Write-Host ""
        Show-Help
        exit 1
    }
}

