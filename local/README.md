# Base API - Docker Setup

## 🚀 Inicio Rápido

### Levantar la aplicación

```bash
# Navegar al directorio local
cd local

# Build y levantar servicios
docker-compose up --build

# O en modo detached (background)
docker-compose up -d --build
```

### Detener la aplicación

```bash
# Detener y eliminar contenedores
docker-compose down

# Detener, eliminar contenedores y volúmenes
docker-compose down -v
```

## 📋 Endpoints Disponibles

| Endpoint | URL | Descripción |
|----------|-----|-------------|
| **API Base** | http://localhost:8080/base-api | Context path de la aplicación |
| **Health Check** | http://localhost:8080/base-api/actuator/health | Estado de salud de la aplicación |
| **Metrics** | http://localhost:8080/base-api/actuator/metrics | Métricas de la aplicación |
| **Info** | http://localhost:8080/base-api/actuator/info | Información de la aplicación |
| **H2 Console** | http://localhost:8080/base-api/h2-console | Consola de base de datos H2 |

### Credenciales H2 Console
- **JDBC URL:** `jdbc:h2:mem:testdb`
- **User:** `sa`
- **Password:** _(vacío)_

## 🔧 Comandos Útiles

### Ver logs
```bash
# Logs en tiempo real
docker-compose logs -f

# Logs de un servicio específico
docker-compose logs -f base-api
```

### Reconstruir imagen
```bash
# Forzar rebuild sin caché
docker-compose build --no-cache

# Rebuild y reiniciar
docker-compose up --build --force-recreate
```

### Verificar estado
```bash
# Ver servicios en ejecución
docker-compose ps

# Ver uso de recursos
docker stats base-api-app
```

### Acceder al contenedor
```bash
# Shell interactivo
docker exec -it base-api-app sh

# Ver logs internos
docker exec -it base-api-app cat /app/logs/application.log
```

## 🏗️ Arquitectura

### Multi-stage Build
El Dockerfile utiliza construcción en dos etapas:

1. **Stage 1 (builder):** Compila la aplicación con Maven
   - Imagen: `eclipse-temurin:25-jdk-alpine`
   - Cachea dependencias para builds más rápidos
   - Genera artefactos optimizados

2. **Stage 2 (runtime):** Ejecuta la aplicación
   - Imagen: `eclipse-temurin:25-jre-alpine`
   - Usuario no-root para seguridad
   - Optimizaciones JVM para contenedores
   - Health checks integrados

### Optimizaciones JVM
```properties
-XX:+UseContainerSupport          # Detecta límites del contenedor
-XX:MaxRAMPercentage=75.0         # Usa hasta 75% de RAM disponible
-XX:+UseG1GC                      # Garbage Collector G1
-XX:+ExitOnOutOfMemoryError       # Failfast ante OOM
```

### Límites de Recursos
- **CPU:** 2 cores (max) / 0.5 cores (reservado)
- **Memoria:** 1GB (max) / 512MB (reservado)

## 🔍 Monitoreo

### Health Check Automático
Docker ejecuta health checks cada 30 segundos:
```bash
wget --spider http://localhost:8080/base-api/actuator/health
```

### Ver estado de health
```bash
docker inspect --format='{{.State.Health.Status}}' base-api-app
```

## 🛠️ Personalización

### Variables de Entorno
Editar `docker-compose.yml` para modificar:
- Perfiles de Spring (`SPRING_PROFILES_ACTIVE`)
- Configuración de base de datos
- Niveles de logging
- Endpoints de Actuator

### Agregar Servicios
Para agregar bases de datos u otros servicios:
```yaml
services:
  base-api:
    # ...configuración existente...
    depends_on:
      - postgres
  
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: basedb
      POSTGRES_USER: user
      POSTGRES_PASSWORD: password
    ports:
      - "5432:5432"
```

## 📊 Troubleshooting

### La aplicación no inicia
```bash
# Ver logs detallados
docker-compose logs base-api

# Verificar puerto en uso
netstat -ano | findstr :8080

# Reiniciar servicios
docker-compose restart
```

### Problemas de memoria
```bash
# Ajustar límites en docker-compose.yml
resources:
  limits:
    memory: 2G  # Aumentar límite
```

### Build lento
```bash
# Limpiar caché de Docker
docker builder prune

# Limpiar imágenes antiguas
docker image prune -a
```

## 📝 Notas

- Los logs se persisten en `./logs` del host
- La aplicación usa base de datos H2 en memoria (los datos se pierden al reiniciar)
- El health check tarda ~40 segundos en iniciar (configurado en `start_period`)
- Network mode: bridge con red dedicada `base-api-network`

## 🔗 Referencias

- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Docker Multi-stage Builds](https://docs.docker.com/build/building/multi-stage/)
- [Docker Compose](https://docs.docker.com/compose/)
- [Eclipse Temurin](https://adoptium.net/)

