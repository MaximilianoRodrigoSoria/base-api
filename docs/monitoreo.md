# Política de Monitoreo y Observabilidad

## Índice

- [Introducción](#introducción)
- [Spring Boot Actuator](#spring-boot-actuator)
- [Configuración por Ambiente](#configuración-por-ambiente)
- [Endpoints Disponibles](#endpoints-disponibles)
- [Seguridad](#seguridad)
- [Logging](#logging)
- [Métricas](#métricas)
- [Health Checks](#health-checks)
- [Troubleshooting](#troubleshooting)

---

## Introducción

Este documento define la **política de exposición** de endpoints de monitoreo, logging y métricas para el proyecto `base-api`. Se utilizan **Spring Boot Actuator** para observabilidad y **Logback** para gestión centralizada de logs.

**Objetivos:**

- Proporcionar visibilidad operacional sin comprometer seguridad.
- Configurar endpoints apropiados por ambiente.
- Estandarizar logging con formato consistente.
- Facilitar diagnóstico y troubleshooting.

---

## Spring Boot Actuator

**Spring Boot Actuator** expone endpoints de gestión que permiten:

- Monitorear salud de la aplicación (`/health`)
- Obtener información de la aplicación (`/info`)
- Consultar métricas de runtime (`/metrics`)
- Inspeccionar configuración (`/env`, `/configprops`)
- Cambiar niveles de log en runtime (`/loggers`)

### Base Path

Todos los endpoints de Actuator están bajo:

```
/base-api/actuator
```

---

## Configuración por Ambiente

### **Default (Base)**

Configuración mínima y segura aplicada por defecto:

| Endpoint    | Expuesto | Show Details |
|-------------|----------|--------------|
| `health`    | ✅        | `when-authorized` |
| `info`      | ✅        | N/A          |
| `metrics`   | ✅        | N/A          |
| `loggers`   | ✅        | N/A          |

**Archivo:** `application.yml`

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,loggers
  endpoint:
    health:
      show-details: when-authorized
```

---

### **Development (`dev`)**

Configuración permisiva para desarrollo local:

| Endpoint        | Expuesto | Show Details |
|-----------------|----------|--------------|
| `health`        | ✅        | `always`     |
| `info`          | ✅        | N/A          |
| `metrics`       | ✅        | N/A          |
| `loggers`       | ✅        | N/A          |
| `env`           | ✅        | N/A          |
| `beans`         | ✅        | N/A          |
| `configprops`   | ✅        | N/A          |

**Archivo:** `application-dev.yml`

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,loggers,env,beans,configprops
  endpoint:
    health:
      show-details: always
```

**⚠️ Importante:** Esta configuración **NO debe usarse en producción**.

---

### **Production (`prod`)**

Configuración mínima y segura para producción:

| Endpoint    | Expuesto | Show Details |
|-------------|----------|--------------|
| `health`    | ✅        | `never`      |
| `info`      | ✅        | N/A          |

**Archivo:** `application-prod.yml`

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: never
```

**Rationale:**

- Solo se exponen endpoints necesarios para healthchecks y documentación.
- No se filtran detalles internos de la aplicación.
- Endpoints sensibles (`env`, `beans`, `heapdump`, `threaddump`) **NO están expuestos**.

---

## Endpoints Disponibles

### `/actuator/health`

**Propósito:** Verifica el estado de salud de la aplicación y sus dependencias.

**Respuesta (prod):**

```json
{
  "status": "UP"
}
```

**Respuesta (dev):**

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

**Uso:**

- Kubernetes liveness/readiness probes
- Load balancer health checks
- Monitoring tools (Prometheus, Datadog, New Relic)

---

### `/actuator/info`

**Propósito:** Retorna información estática de la aplicación.

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

**Configuración:** Se define en `application.yml` bajo la sección `info`.

---

### `/actuator/metrics`

**Propósito:** Expone métricas de runtime (JVM, HTTP, base de datos, custom).

**Uso:**

```bash
# Listar todas las métricas disponibles
GET /base-api/actuator/metrics

# Consultar métrica específica
GET /base-api/actuator/metrics/jvm.memory.used
GET /base-api/actuator/metrics/http.server.requests
```

**Métricas útiles:**

- `jvm.memory.used` / `jvm.memory.max`
- `jvm.gc.pause`
- `http.server.requests`
- `system.cpu.usage`
- `process.uptime`

---

### `/actuator/loggers`

**Propósito:** Consultar y modificar niveles de logging en runtime.

**Consultar nivel de un logger:**

```bash
GET /base-api/actuator/loggers/com.ar.laboratory
```

**Cambiar nivel de logging (POST):**

```bash
POST /base-api/actuator/loggers/com.ar.laboratory
Content-Type: application/json

{
  "configuredLevel": "DEBUG"
}
```

**⚠️ Importante:** Disponible solo en `dev`. En `prod` debe estar protegido o no expuesto.

---

### Endpoints **NO expuestos** en producción

Por razones de seguridad, los siguientes endpoints están **deshabilitados en prod**:

| Endpoint       | Riesgo                                      |
|----------------|---------------------------------------------|
| `env`          | Expone variables de entorno y secrets       |
| `configprops`  | Expone propiedades de configuración         |
| `beans`        | Expone estructura interna de la aplicación  |
| `heapdump`     | Genera volcado de memoria (DoS, leak info)  |
| `threaddump`   | Expone estado de threads (DoS, leak info)   |
| `shutdown`     | Permite apagar la aplicación remotamente    |

---

## Seguridad

### Recomendaciones

1. **Producción:**
    - Exponer **solo** `health` e `info`.
    - Si `metrics` es necesario, hacerlo en red interna o con autenticación.
    - **Nunca** exponer `env`, `beans`, `heapdump`, `threaddump`.

2. **Autenticación (futuro):**
    - Proteger endpoints con **Spring Security**.
    - Usar Basic Auth o Bearer Token para endpoints sensibles.
    - Configurar roles: `ACTUATOR_ADMIN`, `ACTUATOR_READONLY`.

3. **Network Segmentation:**
    - Exponer Actuator en puerto separado (`management.server.port`).
    - Restringir acceso mediante firewall/security groups.

4. **Auditoría:**
    - Loggear accesos a endpoints de Actuator.
    - Integrar con SIEM para detección de anomalías.

### Ejemplo con Spring Security (futuro)

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,loggers
  endpoint:
    health:
      show-details: when-authorized
      roles: ACTUATOR_ADMIN

spring:
  security:
    user:
      name: admin
      password: changeme
      roles: ACTUATOR_ADMIN
```

---

## Logging

### Configuración de Logback

El logging se gestiona con **Logback** mediante `logback-spring.xml`.

**Características:**

- **Pattern consistente** con timestamp ISO-8601, nivel, thread y mensaje.
- **Appenders:** Console (async), File (rolling).
- **Loggers específicos** para Spring, Hibernate, aplicación.
- **Configuración por perfil** (`dev`, `prod`).

### Formato de Log

```
2026-01-30T14:23:45.123 INFO  [http-nio-8080-exec-1] c.a.l.baseapi.MyService - Processing request for ID: 12345
```

**Patrón:**

```
%d{yyyy-MM-dd'T'HH:mm:ss.SSS} %-5level [%thread] %logger{36} - %msg%n
```

### Niveles de Log por Ambiente

| Logger                     | Dev   | Prod |
|----------------------------|-------|------|
| `root`                     | INFO  | WARN |
| `com.ar.laboratory`        | DEBUG | INFO |
| `org.springframework.web`  | DEBUG | INFO |
| `org.hibernate.SQL`        | DEBUG | INFO |

### Cambiar Nivel de Log en Runtime

```bash
# Activar logs DEBUG para troubleshooting
POST /base-api/actuator/loggers/com.ar.laboratory
Content-Type: application/json

{
  "configuredLevel": "DEBUG"
}
```

### Rotación de Archivos de Log

En producción, los logs se rotan automáticamente:

- **Max File Size:** 10 MB
- **Max History:** 30 días
- **Location:** `logs/base-api.log`

---

## Métricas

### Métricas JVM

- `jvm.memory.used` / `jvm.memory.max`
- `jvm.gc.pause` (garbage collection)
- `jvm.threads.live` / `jvm.threads.daemon`
- `system.cpu.usage`

### Métricas HTTP

- `http.server.requests` (por URI, status, método)
- `http.server.requests.active`

### Métricas Custom

Para agregar métricas de negocio:

```java
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final MeterRegistry meterRegistry;

    public void processPayment(PaymentRequest request) {
        Counter.builder("payments.processed")
            .tag("method", request.getMethod())
            .register(meterRegistry)
            .increment();
    }
}
```

---

## Health Checks

### Liveness Probe

Verifica que la aplicación está **viva** (no colgada).

```yaml
management:
  endpoint:
    health:
      probes:
        enabled: true
  health:
    livenessState:
      enabled: true
```

**Endpoint:**

```
GET /base-api/actuator/health/liveness
```

**Uso:** Kubernetes `livenessProbe` para reiniciar pods.

### Readiness Probe

Verifica que la aplicación está **lista** para recibir tráfico.

```yaml
management:
  health:
    readinessState:
      enabled: true
```

**Endpoint:**

```
GET /base-api/actuator/health/readiness
```

**Uso:** Kubernetes `readinessProbe` para controlar balanceo de carga.

---

## Troubleshooting

### Problema: Endpoints no accesibles

**Síntoma:** `404 Not Found` al acceder a `/actuator/health`

**Solución:**

1. Verificar que `spring-boot-starter-actuator` está en el POM.
2. Verificar exposición en `application.yml`:
   ```yaml
   management.endpoints.web.exposure.include: health,info
   ```
3. Verificar base path: `/base-api/actuator/health` (incluir context-path).

### Problema: Health muestra `DOWN`

**Síntoma:** `/actuator/health` retorna `status: DOWN`

**Solución:**

1. Revisar componentes en detalle (dev):
   ```bash
   GET /base-api/actuator/health
   ```
2. Verificar conectividad a dependencias (DB, cache, servicios externos).
3. Revisar logs para excepciones.

### Problema: Logs no aparecen

**Síntoma:** No se generan logs en consola o archivo.

**Solución:**

1. Verificar nivel de log: `logging.level.com.ar.laboratory=DEBUG`
2. Verificar `logback-spring.xml` existe y es válido.
3. Revisar que el logger esté configurado:
   ```java
   @Slf4j
   public class MyService {
       public void process() {
           log.info("Processing...");
       }
   }
   ```

### Problema: Métricas no disponibles

**Síntoma:** `/actuator/metrics` retorna vacío.

**Solución:**

1. Verificar que `micrometer-core` está incluido (vía `spring-boot-starter-actuator`).
2. Verificar exposición: `management.endpoints.web.exposure.include: metrics`
3. Generar tráfico para que se registren métricas HTTP.

---

## Referencias

- [Spring Boot Actuator Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Logback Configuration](http://logback.qos.ch/manual/configuration.html)
- [Micrometer Documentation](https://micrometer.io/docs)
- [Kubernetes Health Probes](https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/)

---

**Última actualización:** 2026-01-30  
**Autor:** DevOps Team  
**Versión:** 1.0

