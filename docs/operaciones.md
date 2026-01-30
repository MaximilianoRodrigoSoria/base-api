# Guía Operacional - base-api

## Índice

- [Introducción](#introducción)
- [Despliegue](#despliegue)
- [Monitoreo y Observabilidad](#monitoreo-y-observabilidad)
- [Logging](#logging)
- [Health Checks](#health-checks)
- [Troubleshooting](#troubleshooting)
- [Mantenimiento](#mantenimiento)

---

## Introducción

Este documento proporciona **guías operacionales** para el despliegue, monitoreo, troubleshooting y mantenimiento del proyecto `base-api`.

**Audiencia:** DevOps, SRE, Soporte de Producción.

---

## Despliegue

### Requisitos

- **Java:** 25+
- **Maven:** 3.9+
- **Docker:** 20+
- **Kubernetes:** 1.28+ (opcional)

### Build

```bash
mvn clean package -DskipTests
```

**Artefacto generado:** `target/base-api-0.0.1-SNAPSHOT.jar`

### Ejecución Local

```bash
java -jar target/base-api-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

### Ejecución con Docker

```bash
docker build -t base-api:latest .
docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=dev base-api:latest
```

### Variables de Entorno

| Variable                  | Descripción                          | Default       |
|---------------------------|--------------------------------------|---------------|
| `SPRING_PROFILES_ACTIVE`  | Perfil activo (`dev`, `prod`)        | `default`     |
| `SERVER_PORT`             | Puerto del servidor                  | `8080`        |
| `LOG_FILE`                | Ruta del archivo de logs             | `logs/base-api` |

---

## Monitoreo y Observabilidad

### Spring Boot Actuator

El proyecto expone endpoints de monitoreo vía **Spring Boot Actuator**.

**Base Path:** `/base-api/actuator`

### Endpoints Disponibles

#### Health Check

```bash
curl http://localhost:8080/base-api/actuator/health
```

**Respuesta:**

```json
{
  "status": "UP"
}
```

**Uso:**

- Load balancer health checks
- Kubernetes liveness/readiness probes
- Monitoring tools (Prometheus, Datadog, New Relic)

#### Application Info

```bash
curl http://localhost:8080/base-api/actuator/info
```

**Respuesta:**

```json
{
  "app": {
    "name": "base-api",
    "description": "Base API - Spring Boot Template",
    "version": "0.0.1-SNAPSHOT",
    "java": {
      "version": 25
    }
  }
}
```

#### Métricas

```bash
# Listar todas las métricas
curl http://localhost:8080/base-api/actuator/metrics

# Consultar métrica específica
curl http://localhost:8080/base-api/actuator/metrics/jvm.memory.used
curl http://localhost:8080/base-api/actuator/metrics/http.server.requests
```

**Métricas clave:**

- `jvm.memory.used` / `jvm.memory.max` → Uso de memoria
- `jvm.gc.pause` → Pausa de garbage collection
- `http.server.requests` → Requests HTTP (por URI, status, método)
- `system.cpu.usage` → Uso de CPU
- `process.uptime` → Uptime de la aplicación

#### Cambiar Nivel de Logging

```bash
# Ver nivel actual
curl http://localhost:8080/base-api/actuator/loggers/com.ar.laboratory

# Cambiar a DEBUG (solo en dev)
curl -X POST http://localhost:8080/base-api/actuator/loggers/com.ar.laboratory \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel":"DEBUG"}'
```

### Configuración por Ambiente

#### Development (`dev`)

Endpoints expuestos:

- `health` (con detalles completos)
- `info`
- `metrics`
- `loggers`
- `env`
- `beans`
- `configprops`

**Activar perfil:**

```bash
java -jar base-api.jar --spring.profiles.active=dev
```

#### Production (`prod`)

Endpoints expuestos (mínimos y seguros):

- `health` (sin detalles)
- `info`

**Activar perfil:**

```bash
java -jar base-api.jar --spring.profiles.active=prod
```

**⚠️ Seguridad:** Endpoints sensibles (`env`, `beans`, `heapdump`, `threaddump`) están **deshabilitados en producción**.

### Integración con Prometheus (opcional)

Para exponer métricas en formato Prometheus:

1. Agregar dependencia:

```xml
<dependency>
  <groupId>io.micrometer</groupId>
  <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

2. Exponer endpoint:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

3. Scrape endpoint:

```
GET /base-api/actuator/prometheus
```

---

## Logging

### Configuración de Logback

El logging se gestiona con **Logback** mediante `logback-spring.xml`.

**Características:**

- **Pattern consistente:** Timestamp ISO-8601, nivel, thread, logger, mensaje.
- **Appenders:** Console (async), File (rolling).
- **Niveles por paquete:** Configurables por perfil.
- **Rotación automática:** 10 MB por archivo, 30 días de historia.

### Formato de Log

```
2026-01-30T14:23:45.123 INFO  [http-nio-8080-exec-1] c.a.l.baseapi.MyService - Processing request for ID: 12345
```

### Niveles de Log por Ambiente

| Logger                     | Dev   | Prod |
|----------------------------|-------|------|
| `root`                     | INFO  | WARN |
| `com.ar.laboratory`        | DEBUG | INFO |
| `org.springframework.web`  | DEBUG | INFO |
| `org.hibernate.SQL`        | DEBUG | INFO |

### Cambiar Nivel de Logging en Runtime

Útil para troubleshooting sin reiniciar la aplicación:

```bash
# Activar DEBUG para paquete específico
curl -X POST http://localhost:8080/base-api/actuator/loggers/com.ar.laboratory \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel":"DEBUG"}'

# Volver a INFO
curl -X POST http://localhost:8080/base-api/actuator/loggers/com.ar.laboratory \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel":"INFO"}'
```

### Consultar Logs en Producción

**Ubicación:** `logs/base-api.log`

```bash
# Ver últimas 100 líneas
tail -n 100 logs/base-api.log

# Seguir logs en tiempo real
tail -f logs/base-api.log

# Buscar errores
grep ERROR logs/base-api.log

# Buscar por request ID (si se usa)
grep "request-id=12345" logs/base-api.log
```

### Rotación de Archivos

- **Max File Size:** 10 MB
- **Max History:** 30 días
- **Pattern:** `logs/base-api-YYYY-MM-DD.i.log`

**Ejemplo:**

```
logs/base-api.log               (archivo actual)
logs/base-api-2026-01-30.0.log  (rotado)
logs/base-api-2026-01-30.1.log  (rotado)
```

---

## Health Checks

### Liveness Probe

Verifica que la aplicación está **viva** (no colgada).

**Endpoint:**

```
GET /base-api/actuator/health/liveness
```

**Respuesta:**

```json
{
  "status": "UP"
}
```

**Configuración Kubernetes:**

```yaml
livenessProbe:
  httpGet:
    path: /base-api/actuator/health/liveness
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10
  timeoutSeconds: 5
  failureThreshold: 3
```

### Readiness Probe

Verifica que la aplicación está **lista** para recibir tráfico.

**Endpoint:**

```
GET /base-api/actuator/health/readiness
```

**Respuesta:**

```json
{
  "status": "UP"
}
```

**Configuración Kubernetes:**

```yaml
readinessProbe:
  httpGet:
    path: /base-api/actuator/health/readiness
    port: 8080
  initialDelaySeconds: 10
  periodSeconds: 5
  timeoutSeconds: 3
  failureThreshold: 3
```

### Health Indicators

Spring Boot incluye health indicators automáticos para:

- **Database:** Estado de conexión a base de datos.
- **Disk Space:** Espacio disponible en disco.
- **Ping:** Verificación básica de que la app responde.

**Ver detalles (solo en dev):**

```bash
curl http://localhost:8080/base-api/actuator/health
```

**Respuesta:**

```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "H2",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 500000000000,
        "free": 250000000000,
        "threshold": 10485760
      }
    }
  }
}
```

---

## Troubleshooting

### Problema: Aplicación no inicia

**Síntoma:** Error al arrancar Spring Boot.

**Pasos:**

1. Verificar logs de arranque:
   ```bash
   java -jar base-api.jar 2>&1 | tee startup.log
   ```

2. Verificar puerto disponible:
   ```bash
   netstat -ano | findstr :8080
   ```

3. Verificar perfil activo:
   ```bash
   java -jar base-api.jar --spring.profiles.active=dev
   ```

4. Verificar configuración:
   ```bash
   java -jar base-api.jar --debug
   ```

### Problema: Health Check retorna `DOWN`

**Síntoma:** `/actuator/health` retorna `status: DOWN`

**Pasos:**

1. Verificar detalles (en dev):
   ```bash
   curl http://localhost:8080/base-api/actuator/health
   ```

2. Revisar componente fallido (DB, disk, etc.).

3. Verificar conectividad a dependencias:
   ```bash
   # Si usa DB externa
   telnet db-host 3306
   ```

4. Revisar logs:
   ```bash
   grep ERROR logs/base-api.log
   ```

### Problema: Alto uso de memoria

**Síntoma:** Aplicación consume mucha memoria.

**Pasos:**

1. Consultar métricas JVM:
   ```bash
   curl http://localhost:8080/base-api/actuator/metrics/jvm.memory.used
   curl http://localhost:8080/base-api/actuator/metrics/jvm.memory.max
   ```

2. Verificar garbage collection:
   ```bash
   curl http://localhost:8080/base-api/actuator/metrics/jvm.gc.pause
   ```

3. Generar heap dump (si está expuesto):
   ```bash
   curl http://localhost:8080/base-api/actuator/heapdump -o heapdump.hprof
   ```

4. Analizar con herramientas (VisualVM, MAT).

### Problema: Aplicación lenta

**Síntoma:** Requests tardan mucho.

**Pasos:**

1. Consultar métricas HTTP:
   ```bash
   curl http://localhost:8080/base-api/actuator/metrics/http.server.requests
   ```

2. Activar logs DEBUG:
   ```bash
   curl -X POST http://localhost:8080/base-api/actuator/loggers/com.ar.laboratory \
     -H "Content-Type: application/json" \
     -d '{"configuredLevel":"DEBUG"}'
   ```

3. Revisar logs SQL:
   ```bash
   grep "Hibernate:" logs/base-api.log
   ```

4. Verificar CPU y threads:
   ```bash
   curl http://localhost:8080/base-api/actuator/metrics/system.cpu.usage
   curl http://localhost:8080/base-api/actuator/metrics/jvm.threads.live
   ```

### Problema: Logs no aparecen

**Síntoma:** No se generan logs.

**Pasos:**

1. Verificar nivel de log:
   ```bash
   curl http://localhost:8080/base-api/actuator/loggers/com.ar.laboratory
   ```

2. Cambiar a DEBUG:
   ```bash
   curl -X POST http://localhost:8080/base-api/actuator/loggers/com.ar.laboratory \
     -H "Content-Type: application/json" \
     -d '{"configuredLevel":"DEBUG"}'
   ```

3. Verificar configuración Logback:
   ```bash
   cat src/main/resources/logback-spring.xml
   ```

---

## Mantenimiento

### Actualizar Dependencias

```bash
# Ver actualizaciones disponibles
mvn versions:display-dependency-updates

# Actualizar a última versión minor
mvn versions:use-latest-releases
```

### Limpiar Logs Antiguos

```bash
# Eliminar logs > 30 días
find logs/ -name "*.log" -mtime +30 -delete
```

### Backup de Configuración

```bash
# Backup de configuración
tar -czf config-backup-$(date +%Y%m%d).tar.gz src/main/resources/*.yml src/main/resources/*.xml
```

### Reinicio Seguro

```bash
# Verificar health antes
curl http://localhost:8080/base-api/actuator/health

# Graceful shutdown (si está configurado)
curl -X POST http://localhost:8080/base-api/actuator/shutdown

# Reiniciar servicio (Linux)
systemctl restart base-api
```

---

## Referencias

- [Documentación de Monitoreo](./MONITORING.md)
- [Spring Boot Actuator Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Logback Configuration](http://logback.qos.ch/manual/configuration.html)
- [Kubernetes Health Probes](https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/)

---

**Última actualización:** 2026-01-30  
**Autor:** DevOps Team  
**Versión:** 1.0

