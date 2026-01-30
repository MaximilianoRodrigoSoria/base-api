# Containerización y Despliegue

## Objetivo

Definir la estrategia de containerización, empaquetado y despliegue de la aplicación Base API mediante Docker, garantizando eficiencia, seguridad y reproducibilidad en todos los ambientes.

## Alcance

Este documento cubre:
- Estrategia de construcción de imágenes Docker
- Optimizaciones de build y runtime
- Configuración de orquestación local
- Consideraciones de seguridad en contenedores
- Mejores prácticas de despliegue

## Estrategia de Containerización

### Multi-Stage Build

La construcción de imágenes utiliza estrategia multi-stage para optimizar tamaño y seguridad:

```
Etapa 1: Builder                Etapa 2: Runtime
━━━━━━━━━━━━━━━━━━━            ━━━━━━━━━━━━━━━━━━━
eclipse-temurin:25-jdk-alpine   eclipse-temurin:25-jre-alpine
Maven Build Completo            Solo JRE + Artefactos
~300 MB                         ~150 MB
```

**Beneficios**:
- Imagen final reducida en ~50%
- Separación de herramientas de build y runtime
- Cache de capas para builds incrementales rápidos
- Ausencia de herramientas de desarrollo en producción

### Arquitectura de Capas

```dockerfile
# Etapa Builder
FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /build
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN ./mvnw dependency:go-offline -B    # Capa cacheada
COPY src src
RUN ./mvnw clean package -DskipTests -B
RUN jar -xf target/*.jar                # Extracción para layering

# Etapa Runtime
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=builder /build/target/dependency/BOOT-INF/lib /app/lib
COPY --from=builder /build/target/dependency/META-INF /app/META-INF
COPY --from=builder /build/target/dependency/BOOT-INF/classes /app
```

**Justificación del layering**:
- Dependencias cambian con menor frecuencia que código de aplicación
- Docker reutiliza capas sin cambios
- Despliegues más rápidos al solo actualizar capas modificadas

## Optimizaciones

### Build Time

| Optimización | Descripción | Impacto |
|--------------|-------------|---------|
| Dependency caching | `dependency:go-offline` en capa separada | Builds incrementales 5-10x más rápidos |
| .dockerignore | Exclusión de archivos innecesarios del contexto | Reducción de contexto transferido a daemon |
| Parallel downloads | Maven con `-B` (batch mode) | Descarga paralela de dependencias |
| Skip tests | `-DskipTests` en build de imagen | Build 30-40% más rápido |

### Runtime

| Optimización | Configuración | Propósito |
|--------------|---------------|-----------|
| JRE Alpine | `eclipse-temurin:25-jre-alpine` | Imagen base minimal (~40MB vs ~200MB JDK) |
| Container awareness | `-XX:+UseContainerSupport` | JVM detecta límites de CPU/RAM del contenedor |
| Dynamic memory | `-XX:MaxRAMPercentage=75.0` | Uso óptimo de memoria disponible |
| G1GC | `-XX:+UseG1GC` | Garbage collector de baja latencia |
| Failfast OOM | `-XX:+ExitOnOutOfMemoryError` | Reinicio automático ante falta de memoria |
| Entropy source | `-Djava.security.egd=file:/dev/./urandom` | Arranque rápido de SecureRandom |

### Seguridad en Contenedores

#### Usuario No-Root

```dockerfile
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
```

**Razón**: Principio de mínimo privilegio. La aplicación no requiere permisos root.

#### Imagen Base Verificada

- **Proveedor**: Eclipse Adoptium (Temurin)
- **Validación**: Imágenes firmadas y escaneadas por vulnerabilidades
- **Actualizaciones**: Seguimiento de CVEs y parches de seguridad

#### Exposición Mínima

- Solo puerto 8080 expuesto
- No se incluyen herramientas de debugging en producción
- Filesystem read-only para aplicación (recomendado para Kubernetes)

## Orquestación Local

### Docker Compose

Configuración declarativa para ambiente local de desarrollo:

```yaml
services:
  base-api:
    build:
      context: ..
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: local
    networks:
      - base-api-network
    healthcheck:
      test: wget --spider http://localhost:8080/base-api/actuator/health
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s
```

### Límites de Recursos

```yaml
deploy:
  resources:
    limits:
      cpus: '2.0'
      memory: 1G
    reservations:
      cpus: '0.5'
      memory: 512M
```

**Criterios**:
- **Limits**: Máximo que el contenedor puede consumir
- **Reservations**: Garantía mínima de recursos
- **Ratio 1:2**: Permite burst temporal sin afectar otros servicios

## Health Checks

### Nivel Dockerfile

```dockerfile
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --spider http://localhost:8080/base-api/actuator/health
```

### Nivel Docker Compose

Configuración más flexible con timeout extendido para desarrollo:

```yaml
healthcheck:
  test: ["CMD", "wget", "--spider", "http://localhost:8080/base-api/actuator/health"]
  interval: 30s
  timeout: 10s
  retries: 3
  start_period: 40s
```

**Parámetros**:
- **interval**: Frecuencia de verificación
- **timeout**: Tiempo máximo de espera por respuesta
- **retries**: Intentos antes de marcar unhealthy
- **start_period**: Gracia durante arranque de aplicación

## Variables de Entorno

### Configuración por Ambiente

```yaml
environment:
  # Spring Boot
  SPRING_PROFILES_ACTIVE: ${ENVIRONMENT:-local}
  SPRING_APPLICATION_NAME: base-api
  SERVER_PORT: 8080
  
  # Datasource
  SPRING_DATASOURCE_URL: ${DB_URL}
  SPRING_DATASOURCE_USERNAME: ${DB_USER}
  SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
  
  # Observability
  MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE: health,info,metrics
  
  # Logging
  LOGGING_LEVEL_ROOT: ${LOG_LEVEL:-INFO}
  LOGGING_LEVEL_COM_AR_LABORATORY: ${LOG_LEVEL_APP:-DEBUG}
```

### Externalización de Secretos

**Desarrollo Local**:
- Archivo `.env` (no commiteado)
- Variables en docker-compose.yml

**Ambientes Superiores**:
- Kubernetes Secrets
- HashiCorp Vault
- AWS Secrets Manager / Azure Key Vault

## Volúmenes y Persistencia

### Logs

```yaml
volumes:
  - ./logs:/app/logs
```

**Propósito**: Acceso a logs desde host sin exec al contenedor.

**Consideraciones**:
- En producción: Usar solución de logging centralizado (ELK, Splunk, CloudWatch)
- No persistir logs en volúmenes en Kubernetes (usar stdout/stderr)

### Datos H2 (Desarrollo)

Por defecto H2 en memoria. Para persistencia local:

```yaml
environment:
  SPRING_DATASOURCE_URL: jdbc:h2:file:/app/data/testdb
volumes:
  - h2-data:/app/data
```

## Redes

```yaml
networks:
  base-api-network:
    driver: bridge
    name: base-api-network
```

**Aislamiento**: Red dedicada para comunicación entre servicios sin exposición directa al host.

## Estrategias de Despliegue

### Rolling Update

Para ambientes con múltiples instancias:

```yaml
deploy:
  replicas: 3
  update_config:
    parallelism: 1
    delay: 10s
    order: start-first
```

**Garantía**: Zero-downtime mediante arranque de nueva instancia antes de terminar la anterior.

### Blue-Green

Despliegue de nueva versión en ambiente paralelo, switch de tráfico tras validación.

**Ventajas**:
- Rollback instantáneo
- Testing en producción sin afectar usuarios
- Requiere infraestructura duplicada temporalmente

### Canary

Despliegue gradual a subconjunto de usuarios:

```
v1.0: 90% tráfico
v1.1: 10% tráfico → Monitorear → Incrementar gradualmente
```

## Construcción de Imágenes

### CI/CD Pipeline

```bash
# Build
docker build -t base-api:${VERSION} .

# Tag
docker tag base-api:${VERSION} registry.example.com/base-api:${VERSION}
docker tag base-api:${VERSION} registry.example.com/base-api:latest

# Push
docker push registry.example.com/base-api:${VERSION}
docker push registry.example.com/base-api:latest
```

### Versionado

**Esquema semántico**:
- `major.minor.patch` (ej: `1.2.3`)
- `latest` siempre apunta a última versión estable
- Tags inmutables: Una vez publicada, una versión no se sobrescribe

**Metadata**:
```dockerfile
LABEL version="0.0.1-SNAPSHOT"
LABEL description="Base API - Spring Boot Application"
LABEL maintainer="laboratory"
```

## Monitoreo de Contenedores

### Métricas Expuestas

- **cAdvisor**: Uso de CPU, memoria, I/O por contenedor
- **Actuator**: Métricas específicas de aplicación (JVM, HTTP, custom)
- **Docker Stats**: Comando nativo para inspección en tiempo real

### Logging

**Estrategia**: Todo a stdout/stderr.

```yaml
logging:
  driver: "json-file"
  options:
    max-size: "10m"
    max-file: "3"
```

**Aggregation**: Fluentd, Filebeat o Cloud-native logging.

## Consideraciones de Producción

### Límites Estrictos

```yaml
deploy:
  resources:
    limits:
      cpus: '4.0'
      memory: 2G
    reservations:
      cpus: '2.0'
      memory: 1G
```

### Restart Policies

```yaml
restart: unless-stopped
deploy:
  restart_policy:
    condition: on-failure
    delay: 5s
    max_attempts: 3
```

### Security Scanning

Integración en pipeline CI/CD:

```bash
# Trivy
trivy image base-api:${VERSION}

# Snyk
snyk container test base-api:${VERSION}
```

**Criterio**: Bloqueo de despliegue si se detectan vulnerabilidades HIGH o CRITICAL.

## Troubleshooting

### Inspección de Contenedor

```bash
# Logs
docker logs -f base-api-app

# Stats en vivo
docker stats base-api-app

# Exec shell
docker exec -it base-api-app sh

# Inspect health
docker inspect --format='{{.State.Health.Status}}' base-api-app
```

### Problemas Comunes

| Síntoma | Causa Probable | Solución |
|---------|----------------|----------|
| OOMKilled | Límite de memoria insuficiente | Incrementar `memory` o reducir `-XX:MaxRAMPercentage` |
| Unhealthy | Aplicación no arranca en start_period | Aumentar `start_period` o investigar logs |
| Build lento | Cache de capas inválido | Verificar orden de COPY en Dockerfile |
| Startup lento | Entropy bloqueante | Verificar flag `-Djava.security.egd` |

## Scripts de Gestión

### start.ps1

Script automatizado de inicio con verificación de health y presentación de endpoints.

### docker.ps1

Script de gestión con comandos:
- `start`: Inicia servicios en background
- `stop`: Detiene servicios
- `logs`: Muestra logs en tiempo real
- `health`: Verifica health check
- `shell`: Accede al contenedor
- `clean`: Limpia contenedores y volúmenes

## Referencias

- [Dockerfile Best Practices](https://docs.docker.com/develop/develop-images/dockerfile_best-practices/)
- [Multi-stage Builds](https://docs.docker.com/build/building/multi-stage/)
- [Docker Compose Specification](https://docs.docker.com/compose/compose-file/)
- [Arquitectura General](./arquitectura.md)
- [Monitoreo y Observabilidad](./monitoreo.md)

