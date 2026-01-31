# Checklist de Features de Calidad – Spring Boot

## Estado general
- **Fecha de revisión:** 2026-01-31 03:51 UTC-3
- **Proyecto:** base-api (Spring Boot 4.0.2 / Java 25)
- **Rama / commit analizado:** 765897b7acd18e5be72ac329f5dba6f85283b82a
- **Arquitectura:** Hexagonal (Ports & Adapters)

---

## Checklist

### 1. Testing y Calidad

#### 1.1. Testcontainers (DB real en tests)
- **Estado:** NO IMPLEMENTADA
- **Evidencia encontrada:**
  - Mención en documentación (`docs/redis-integration.md`) pero sin implementación
  - No existe dependencia `org.testcontainers:*` en `pom.xml`
  - Tests actuales usan `@WebMvcTest` y mocks, no containers reales
- **Observaciones técnicas:**
  - Tests actuales son unitarios con MockMvc y Mockito
  - No hay tests de integración con base de datos PostgreSQL real
- **Recomendación:**
  - Agregar `testcontainers-postgresql` y `testcontainers-junit-jupiter`
  - Crear tests de integración para adapters JPA (`ExampleJpaAdapter`)
  - Validar migraciones Flyway con contenedor real

#### 1.2. WireMock / MockWebServer para integraciones HTTP
- **Estado:** IMPLEMENTADA
- **Evidencia encontrada:**
  - Docker Compose con servicio WireMock (`local/docker-compose.yml`)
  - Mappings configurados: `local/wiremock/mappings/cuit-service-*.json`
  - Stubs JSON: `local/wiremock/__files/cuit-*.json`
  - Configuración de URL en `application.yml`: `wiremock.url: http://localhost:8090`
  - Adapter externo: `CuitServiceAdapter.java`
- **Observaciones técnicas:**
  - WireMock standalone (Docker) para desarrollo local
  - Sin evidencia de WireMock en tests automatizados
- **Recomendación:**
  - Integrar `wiremock-standalone` o `mockwebserver` en tests de `CuitServiceAdapter`

#### 1.3. Builders / Fixtures de dominio para tests
- **Estado:** NO IMPLEMENTADA
- **Evidencia encontrada:**
  - Tests crean objetos manualmente con `.builder()` de Lombok
  - No existen clases `*Builder` o `*Fixture` dedicadas
- **Observaciones técnicas:**
  - Alta duplicación en construcción de objetos de prueba
  - Mantenimiento frágil ante cambios en modelos
- **Recomendación:**
  - Crear `TestDataBuilder` o `ObjectMother` pattern para `Example`, `ExampleStatus`, etc.
  - Ejemplo: `ExampleTestBuilder.aValidExample().withDni("12345678").build()`

#### 1.4. Golden files (JSON request/response)
- **Estado:** NO IMPLEMENTADA
- **Evidencia encontrada:**
  - No existen directorios `src/test/resources/golden/` ni similares
  - Tests no validan contra snapshots JSON
- **Observaciones técnicas:**
  - Cambios en serialización no detectados automáticamente
- **Recomendación:**
  - Implementar snapshot testing con JSONAssert o Approval Tests
  - Almacenar respuestas esperadas en `src/test/resources/snapshots/`

#### 1.5. ArchUnit (tests de arquitectura)
- **Estado:** NO IMPLEMENTADA
- **Evidencia encontrada:**
  - No existe dependencia `com.tngtech.archunit:archunit-junit5` en `pom.xml`
  - Sin tests que validen reglas arquitecturales
- **Observaciones técnicas:**
  - Arquitectura hexagonal no está protegida por tests
  - Riesgo de violaciones: domain dependiendo de infrastructure
- **Recomendación:**
  - Agregar ArchUnit para validar:
    - `domain` no depende de `adapters` ni `application`
    - Uso correcto de ports e interfaces
    - Nomenclatura de packages

#### 1.6. Snapshot tests de OpenAPI
- **Estado:** NO IMPLEMENTADA
- **Evidencia encontrada:**
  - OpenAPI configurado (`OpenApiConfig.java`, SpringDoc 2.7.0)
  - No existen tests que validen estabilidad de especificación
- **Observaciones técnicas:**
  - Cambios no intencionados en API no detectados
- **Recomendación:**
  - Test que genere `openapi.json` y lo compare con snapshot
  - Validar cambios breaking vs. evolutivos

---

### 2. Base de Datos

#### 2.1. Flyway con versionado correcto (V__, R__)
- **Estado:** IMPLEMENTADA
- **Evidencia encontrada:**
  - Flyway Core y PostgreSQL driver en `pom.xml`
  - Migraciones versionadas: `V1__Create_examples_table.sql`, `V2__Insert_initial_examples.sql`, `V3__Add_genero_cuit_to_examples.sql`
  - Nomenclatura estándar respetada
- **Observaciones técnicas:**
  - Sin migraciones repetibles (R__) detectadas
  - Convención V[version]__[description].sql correcta

#### 2.2. Convenciones de migraciones (inmutabilidad)
- **Estado:** PARCIAL
- **Evidencia encontrada:**
  - Migraciones versionadas presentes
  - V3 modifica datos existentes (UPDATE) antes de agregar NOT NULL
- **Observaciones técnicas:**
  - Patrón correcto: ALTER + UPDATE + ALTER NOT NULL (V3)
  - Sin evidencia de modificación de migraciones ya aplicadas
- **Recomendación:**
  - Documentar política: migraciones aplicadas son inmutables
  - Validar en CI que archivos V__ no cambian entre commits

#### 2.3. Auditoría de entidades (createdAt, updatedAt, etc.)
- **Estado:** IMPLEMENTADA
- **Evidencia encontrada:**
  - `ExampleEntity.java`: campos `createdAt`, `updatedAt`
  - Callbacks JPA: `@PrePersist` (onCreate), `@PreUpdate` (onUpdate)
  - Migraciones con columnas audit: `created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP`
- **Observaciones técnicas:**
  - Implementación manual por entidad
  - Sin abstracción reutilizable (ej: `@EntityListeners` con `AuditingEntityListener`)
- **Recomendación:**
  - Considerar clase base `AuditableEntity` o Spring Data JPA auditing
  - Agregar campos `createdBy`, `updatedBy` si aplica multi-usuario

#### 2.4. Validaciones en DB + Bean Validation
- **Estado:** PARCIAL
- **Evidencia encontrada:**
  - **Bean Validation:** `CreateExampleRequest.java` con `@NotBlank`, `@Pattern`, `@Size`
  - **DB Constraints:** `UNIQUE(dni)`, `NOT NULL`, `CHECK (genero IN ('H', 'M'))`
  - Spring Boot Starter Validation incluido
- **Observaciones técnicas:**
  - Validaciones duplicadas (aplicación + BD) ✓
  - No todas las reglas están en ambas capas
- **Recomendación:**
  - Sincronizar validaciones: `@Pattern(regexp="^[HM]$")` ↔ `CHECK (genero IN ('H','M'))`
  - Agregar índices estratégicos para queries frecuentes

---

### 3. Observabilidad

#### 3.1. Logging estructurado (JSON)
- **Estado:** PARCIAL
- **Evidencia encontrada:**
  - `logback-spring.xml` configurado con appenders CONSOLE, FILE, ASYNC
  - Patrón de log text-based: `%d{yyyy-MM-dd'T'HH:mm:ss.SSS} %-5level ...`
  - Sin encoder JSON (ej: `logstash-logback-encoder`)
- **Observaciones técnicas:**
  - Logs en texto plano, no parseables por herramientas modernas
  - Dificulta agregación en ELK/Grafana Loki
- **Recomendación:**
  - Agregar `net.logstash.logback:logstash-logback-encoder`
  - Cambiar appender a `LogstashEncoder` con campos custom

#### 3.2. Correlation ID / Request ID (MDC)
- **Estado:** NO IMPLEMENTADA
- **Evidencia encontrada:**
  - Búsqueda de `MDC`, `correlation`, `request-id`, `traceId`: 0 resultados
  - Sin filtros para propagación de IDs entre requests
- **Observaciones técnicas:**
  - Imposible trazar requests a través de logs distribuidos
- **Recomendación:**
  - Implementar filtro Servlet que genere/extrae `X-Request-ID` header
  - Poblarlo en MDC: `MDC.put("requestId", requestId)`
  - Incluir en patrón de log: `%X{requestId}`
  - Considerar Micrometer Tracing + Brave

#### 3.3. Actuator con health groups
- **Estado:** PARCIAL
- **Evidencia encontrada:**
  - Actuator incluido en `pom.xml`
  - Configuración en `application.yml`:
    - `management.endpoints.web.exposure.include: health,info,metrics,loggers`
    - `probes.enabled: true` (liveness/readiness K8s)
  - Sin grupos custom definidos
- **Observaciones técnicas:**
  - Health probes básicos habilitados
  - No hay agrupación por criticidad (ej: `liveness` vs `readiness`)
- **Recomendación:**
  - Configurar health groups:
    ```yaml
    management.endpoint.health.group:
      liveness.include: diskSpace,ping
      readiness.include: db,redis,external-services
    ```

#### 3.4. Micrometer (metrics)
- **Estado:** IMPLEMENTADA
- **Evidencia encontrada:**
  - `spring-boot-starter-actuator` incluye `micrometer-core` 1.16.2
  - Dependencias transitivas: `micrometer-observation`, `micrometer-commons`, `micrometer-jakarta9`
  - Endpoint `/actuator/metrics` expuesto
  - Mención en documentación (`docs/monitoreo.md`, `docs/operaciones.md`)
- **Observaciones técnicas:**
  - Métricas básicas de JVM, HTTP, DB pool disponibles
  - Sin métricas custom de negocio detectadas

#### 3.5. Exporter configurado (Prometheus / OTel)
- **Estado:** NO IMPLEMENTADA
- **Evidencia encontrada:**
  - Búsqueda de `prometheus|MeterRegistry`: encontrado en docs pero no en código
  - **No existe** dependencia `io.micrometer:micrometer-registry-prometheus` en `pom.xml`
  - Docs mencionan configuración futura
- **Observaciones técnicas:**
  - Métricas no exportables a sistemas externos
- **Recomendación:**
  - Agregar dependencia `micrometer-registry-prometheus`
  - Exponer endpoint `/actuator/prometheus`
  - O implementar OpenTelemetry con `micrometer-tracing-bridge-otel`

---

### 4. Resiliencia y Seguridad

#### 4.1. Resilience4j (retry, circuit breaker, etc.)
- **Estado:** NO IMPLEMENTADA
- **Evidencia encontrada:**
  - Búsqueda de `resilience4j|@CircuitBreaker|@Retry`: 0 resultados
  - No existe dependencia `io.github.resilience4j:*` en `pom.xml`
- **Observaciones técnicas:**
  - Llamadas a servicios externos (`CuitServiceAdapter`) sin protección
  - Fallas transitorias no son manejadas
- **Recomendación:**
  - Agregar `resilience4j-spring-boot3` (compatible con Boot 4)
  - Implementar `@CircuitBreaker` y `@Retry` en `CuitServiceAdapter`
  - Configurar timeouts y fallbacks

#### 4.2. Manejo de errores estandarizado (RFC 7807 / Problem Details)
- **Estado:** NO IMPLEMENTADA
- **Evidencia encontrada:**
  - Búsqueda de `@ControllerAdvice|@ExceptionHandler`: 0 resultados
  - No existe `GlobalExceptionHandler` o similar
  - Sin implementación de `ProblemDetail` (RFC 7807)
- **Observaciones técnicas:**
  - Errores devuelven respuestas inconsistentes de Spring Boot default
  - No hay control sobre formato de error
- **Recomendación:**
  - Crear `@RestControllerAdvice` con handlers para:
    - `MethodArgumentNotValidException` (400)
    - `EntityNotFoundException` (404)
    - `Exception` genérico (500)
  - Usar `ProblemDetail` (Spring 6+) o biblioteca `zalando/problem-spring-web`

#### 4.3. Hardening de Spring Security
- **Estado:** NO IMPLEMENTADA
- **Evidencia encontrada:**
  - Búsqueda de `@EnableWebSecurity|SecurityFilterChain`: 0 resultados
  - No existe dependencia `spring-boot-starter-security` en `pom.xml`
- **Observaciones técnicas:**
  - Aplicación sin autenticación/autorización
  - Endpoints públicos sin restricción
- **Recomendación:**
  - Si es API interna: agregar Security con OAuth2/JWT
  - Si es pública: rate limiting, CORS, CSRF según necesidad

#### 4.4. Gestión de secretos por entorno
- **Estado:** PARCIAL
- **Evidencia encontrada:**
  - Perfiles por entorno: `application-local.yml`, `application-dev.yml`, `application-prod.yml`
  - Credenciales en `docker-compose.yml`: `POSTGRES_PASSWORD: 1q2w3e` (hardcoded)
  - No hay integración con vaults externos
- **Observaciones técnicas:**
  - Secretos en archivos de configuración (anti-pattern)
- **Recomendación:**
  - Variables de entorno para secretos en producción
  - Integrar con HashiCorp Vault, AWS Secrets Manager, o K8s Secrets
  - Usar `spring-cloud-config-server` para configuración centralizada

---

### 5. Build y Gobernanza

#### 5.1. Checkstyle / Spotless / EditorConfig
- **Estado:** NO IMPLEMENTADA
- **Evidencia encontrada:**
  - Búsqueda de `checkstyle|spotless`: 0 resultados (excepto docs)
  - No existe `.editorconfig`
  - Sin plugins de formato de código en `pom.xml`
- **Observaciones técnicas:**
  - Estilo de código no forzado automáticamente
- **Recomendación:**
  - Agregar `maven-checkstyle-plugin` con Google Java Style o similar
  - O `spotless-maven-plugin` para formato automático
  - Crear `.editorconfig` para consistencia IDE

#### 5.2. Análisis estático (SpotBugs / PMD)
- **Estado:** NO IMPLEMENTADA
- **Evidencia encontrada:**
  - Búsqueda de `spotbugs|pmd`: 0 resultados
  - Sin plugins de análisis estático en `pom.xml`
- **Observaciones técnicas:**
  - Bugs potenciales no detectados automáticamente
- **Recomendación:**
  - Agregar `spotbugs-maven-plugin` y `maven-pmd-plugin`
  - Configurar en fase `verify` del build

#### 5.3. Cobertura con JaCoCo
- **Estado:** NO IMPLEMENTADA
- **Evidencia encontrada:**
  - Mención en `README_HEXAGONAL.md`: `mvn test jacoco:report`
  - **No existe** plugin `jacoco-maven-plugin` en `pom.xml`
- **Observaciones técnicas:**
  - Cobertura no medida, comando documentado no funciona
- **Recomendación:**
  - Agregar `jacoco-maven-plugin` con goals `prepare-agent` y `report`
  - Configurar umbrales mínimos (ej: 80%)

#### 5.4. Quality Gates (ej: Sonar)
- **Estado:** NO IMPLEMENTADA
- **Evidencia encontrada:**
  - Búsqueda de `sonar`: 0 resultados
  - No existe `sonar-project.properties`
- **Observaciones técnicas:**
  - Sin control de calidad centralizado
- **Recomendación:**
  - Integrar SonarQube/SonarCloud
  - Configurar quality gate: cobertura >80%, 0 bugs críticos, 0 code smells bloqueantes

#### 5.5. Scanning de dependencias (SCA)
- **Estado:** NO IMPLEMENTADA
- **Evidencia encontrada:**
  - Sin plugins de análisis de vulnerabilidades en `pom.xml`
  - No existe `dependency-check-maven` o `snyk`
- **Observaciones técnicas:**
  - Dependencias con CVEs no detectadas
- **Recomendación:**
  - Agregar `dependency-check-maven` o integrar Snyk/Dependabot
  - Ejecutar en CI/CD con umbral de severidad

#### 5.6. SBOM (CycloneDX)
- **Estado:** NO IMPLEMENTADA
- **Evidencia encontrada:**
  - Búsqueda de `cyclonedx|sbom`: 0 resultados
  - Sin generación de Bill of Materials
- **Observaciones técnicas:**
  - No cumple con requisitos de transparencia de supply chain
- **Recomendación:**
  - Agregar `cyclonedx-maven-plugin`
  - Generar SBOM en formato CycloneDX JSON/XML en cada build

---

### 6. Documentación y Contratos

#### 6.1. Swagger / OpenAPI versionado
- **Estado:** IMPLEMENTADA
- **Evidencia encontrada:**
  - SpringDoc OpenAPI 2.7.0 en `pom.xml`
  - Configuración custom en `OpenApiConfig.java`
  - Endpoints: `/api-docs`, `/swagger-ui.html`
  - Versionado leído de `BuildProperties` (`@project.version@`)
- **Observaciones técnicas:**
  - Documentación automática desde anotaciones
  - Sin versionado explícito de API (ej: `/v1/examples`)
- **Recomendación:**
  - Implementar versionado de API: path-based (`/v1/`) o header-based
  - Mantener múltiples versiones de spec para backward compatibility

#### 6.2. Contract testing (Pact / Spring Cloud Contract)
- **Estado:** NO IMPLEMENTADA
- **Evidencia encontrada:**
  - Búsqueda de `pact|spring-cloud-contract`: solo menciones genéricas de "contract" en Javadocs
  - Sin dependencias de contract testing en `pom.xml`
- **Observaciones técnicas:**
  - Contratos entre consumer/provider no validados automáticamente
- **Recomendación:**
  - Evaluar necesidad según arquitectura (microservicios vs monolito)
  - Si aplica: implementar Pact para servicios HTTP externos

#### 6.3. Changelog técnico automatizado
- **Estado:** NO IMPLEMENTADA
- **Evidencia encontrada:**
  - No existe archivo `CHANGELOG.md`
  - Sin plugins de generación automática (ej: `conventional-changelog`)
- **Observaciones técnicas:**
  - Cambios entre versiones no documentados
- **Recomendación:**
  - Adoptar Conventional Commits (feat:, fix:, BREAKING CHANGE:)
  - Usar herramienta: `git-changelog-maven-plugin` o `semantic-release`

---

## Resumen Ejecutivo

| Categoría | Implementadas | Parciales | No Implementadas |
|-----------|---------------|-----------|------------------|
| **Testing y Calidad** | 1/6 | 0/6 | 5/6 |
| **Base de Datos** | 2/4 | 2/4 | 0/4 |
| **Observabilidad** | 2/5 | 2/5 | 1/5 |
| **Resiliencia y Seguridad** | 0/4 | 1/4 | 3/4 |
| **Build y Gobernanza** | 0/6 | 0/6 | 6/6 |
| **Documentación y Contratos** | 1/3 | 0/3 | 2/3 |
| **TOTAL** | **6/28** | **5/28** | **17/28** |

### Prioridades Recomendadas (Top 5)
1. **Manejo de errores estandarizado (RFC 7807)** - Alto impacto en producción
2. **JaCoCo + Quality Gates** - Visibilidad de calidad de código
3. **Resilience4j** - Protección de integraciones externas críticas
4. **Testcontainers** - Confiabilidad de tests de integración
5. **Logging estructurado (JSON) + Correlation ID** - Observabilidad en producción

---

**Nota Técnica:** Este análisis se basa exclusivamente en evidencia concreta encontrada en el código fuente, configuraciones y dependencias del proyecto al momento de la auditoría. Features mencionadas únicamente en documentación pero sin implementación real fueron marcadas como NO IMPLEMENTADAS.
