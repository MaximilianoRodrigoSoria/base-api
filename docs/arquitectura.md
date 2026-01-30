# Arquitectura General

## Objetivo

Definir la estructura arquitectónica del sistema, las decisiones técnicas adoptadas y los principios que guían el desarrollo de la aplicación Base API.

## Alcance

Este documento aplica a toda la capa backend de la aplicación, incluyendo servicios REST, persistencia, seguridad, observabilidad y despliegue.

## Stack Tecnológico

### Runtime

| Componente | Versión | Justificación |
|------------|---------|---------------|
| Java | 25 | LTS con últimas optimizaciones de rendimiento y características del lenguaje |
| Spring Boot | 4.0.2 | Framework empresarial con soporte extendido y ecosistema maduro |
| Maven | Wrapper incluido | Gestión de dependencias y ciclo de vida del proyecto |

### Dependencias Core

| Librería | Propósito |
|----------|-----------|
| spring-boot-starter-webmvc | Exposición de APIs REST con arquitectura MVC |
| spring-boot-starter-validation | Validación declarativa con Jakarta Bean Validation |
| spring-boot-starter-actuator | Observabilidad, métricas y health checks |
| lombok | Reducción de boilerplate mediante anotaciones |
| h2 | Base de datos embebida para desarrollo y testing |

## Principios Arquitectónicos

### Domain-Driven Design (DDD)

El sistema se estructura siguiendo principios de DDD para mantener el foco en el dominio del negocio:

- **Entidades de Dominio**: Modelan conceptos clave del negocio con identidad única
- **Value Objects**: Objetos inmutables que describen características sin identidad
- **Servicios de Dominio**: Lógica de negocio que no pertenece naturalmente a una entidad
- **Repositorios**: Abstracciones para persistencia sin exponer detalles de implementación
- **Agregados**: Agrupaciones de entidades tratadas como unidad transaccional

### Arquitectura Limpia

La separación por capas garantiza independencia de frameworks y facilita testing:

```
┌─────────────────────────────────────┐
│   Capa de Presentación              │
│   (Controllers, DTOs, Mappers)      │
├─────────────────────────────────────┤
│   Capa de Aplicación                │
│   (Services, Use Cases)             │
├─────────────────────────────────────┤
│   Capa de Dominio                   │
│   (Entities, Value Objects, Rules)  │
├─────────────────────────────────────┤
│   Capa de Infraestructura           │
│   (Repositories, External Services) │
└─────────────────────────────────────┘
```

**Reglas de dependencia**:
- Las capas externas dependen de las internas, nunca al revés
- El dominio es independiente de frameworks y bibliotecas
- Los detalles de infraestructura son plugins intercambiables

### SOLID

Todos los componentes adhieren a los principios SOLID:

- **Single Responsibility**: Cada clase tiene una única razón para cambiar
- **Open/Closed**: Abierto a extensión, cerrado a modificación
- **Liskov Substitution**: Las subclases son sustituibles por sus clases base
- **Interface Segregation**: Interfaces específicas mejor que interfaces generales
- **Dependency Inversion**: Dependencia de abstracciones, no de concreciones

## Estructura de Paquetes

```
com.ar.laboratory.baseapi/
├── controller/          # Capa de presentación REST
│   ├── dto/            # Request/Response objects
│   └── mapper/         # DTO ↔ Entity conversions
├── service/            # Capa de aplicación
│   ├── impl/          # Implementaciones de servicios
│   └── validator/     # Validadores de negocio
├── domain/             # Capa de dominio
│   ├── entity/        # Entidades de negocio
│   ├── repository/    # Contratos de persistencia
│   ├── exception/     # Excepciones de dominio
│   └── rules/         # Reglas de negocio complejas
├── infrastructure/     # Capa de infraestructura
│   ├── persistence/   # Implementación de repositorios
│   ├── config/        # Configuración de beans
│   └── external/      # Integraciones externas
└── util/              # Utilidades transversales
```

## Patrones de Diseño Aplicados

### Inyección de Dependencias

- **Por Constructor**: Único mecanismo permitido (no field injection)
- **Inmutabilidad**: Dependencias marcadas como `final`
- **Interfaces**: Contratos explícitos para servicios y repositorios

### DTO Pattern

- **Request/Response separados**: No reutilización de DTOs entre entrada y salida
- **Records**: Para DTOs inmutables cuando sea apropiado
- **Mappers dedicados**: MapStruct o clases mapper manuales

### Repository Pattern

- **Abstracciones Spring Data**: Uso de `JpaRepository` cuando aplique
- **Queries custom**: Documentadas con intención y consideraciones de performance
- **Transaccionalidad**: Gestionada en capa de servicio, no en repositorios

### Exception Handling Centralizado

- **@RestControllerAdvice**: Punto único de manejo de excepciones
- **Excepciones de negocio**: Extienden `RuntimeException` con contexto específico
- **Mapping HTTP**: Códigos de estado apropiados según tipo de excepción

## Decisiones Arquitectónicas

### ADR-001: Uso de Java 25

**Contexto**: Necesidad de aprovechar características modernas del lenguaje y optimizaciones de runtime.

**Decisión**: Adoptar Java 25 como versión base del proyecto.

**Consecuencias**:
- Acceso a pattern matching, records, sealed classes y otras mejoras del lenguaje
- Mejoras de rendimiento en GC y arranque de aplicación
- Requiere actualización de conocimientos del equipo

### ADR-002: Spring Boot 4.x

**Contexto**: Requerimiento de framework empresarial con soporte LTS y ecosistema maduro.

**Decisión**: Utilizar Spring Boot 4.0.2 con autoconfiguración y starters.

**Consecuencias**:
- Reducción significativa de configuración manual
- Integración simplificada con Actuator, Security, Data, etc.
- Dependencia del ciclo de vida de Spring Framework

### ADR-003: Arquitectura en Capas con DDD

**Contexto**: Necesidad de mantener separación de responsabilidades y facilitar evolución del sistema.

**Decisión**: Implementar arquitectura limpia con enfoque DDD.

**Consecuencias**:
- Mayor inversión inicial en diseño y estructura
- Código más testeable y mantenible a largo plazo
- Curva de aprendizaje para desarrolladores nuevos

### ADR-004: H2 como Base de Datos de Desarrollo

**Contexto**: Facilitar desarrollo local sin dependencias externas.

**Decisión**: Utilizar H2 embebida para entornos locales y testing.

**Consecuencias**:
- Arranque rápido sin configuración de infraestructura
- Consola web para inspección de datos
- Debe sustituirse por BD productiva en ambientes superiores

### ADR-005: Validación en Límites de API

**Contexto**: Garantizar que los datos inválidos no penetren en el dominio.

**Decisión**: Validación declarativa en controllers mediante Jakarta Bean Validation.

**Consecuencias**:
- Contratos explícitos en DTOs de entrada
- Respuestas de error estandarizadas
- Dominio protegido de datos inconsistentes

## Consideraciones de Escalabilidad

### Horizontal

- Aplicación stateless: Puede escalarse añadiendo instancias
- No dependencia de sesiones HTTP en memoria
- Configuración externa mediante properties o variables de entorno

### Vertical

- Optimizaciones JVM para contenedores (`-XX:+UseContainerSupport`)
- Gestión dinámica de memoria (`MaxRAMPercentage`)
- Garbage Collector G1 optimizado para baja latencia

### Resiliencia

- Health checks para orquestadores (Kubernetes, Docker)
- Graceful shutdown para drenaje de conexiones
- Timeouts configurables en integraciones externas

## Seguridad

### Principios

- **Mínimo Privilegio**: Aplicación ejecuta como usuario no-root en contenedores
- **Validación Estricta**: Toda entrada externa se valida antes de procesar
- **Logging Seguro**: No se registran datos sensibles (credenciales, tokens, PII)

### Mecanismos

- Jakarta Bean Validation en límites de API
- Exception handling que no expone stacktraces en producción
- Actuator endpoints protegidos mediante autenticación (cuando aplique)

## Observabilidad

Ver documento específico: [Monitoreo y Observabilidad](./monitoreo.md)

## Despliegue

Ver documento específico: [Containerización y Despliegue](./containerizacion.md)

## Evolución del Sistema

### Extensiones Planificadas

El diseño actual soporta las siguientes extensiones sin cambios arquitectónicos mayores:

- Integración con bases de datos relacionales (PostgreSQL, MySQL)
- Autenticación y autorización (Spring Security + JWT/OAuth2)
- Mensajería asíncrona (RabbitMQ, Kafka)
- Cache distribuido (Redis)
- Trazabilidad distribuida (Spring Cloud Sleuth + Zipkin)
- API Gateway y Service Discovery

### Principios de Evolución

- **Backward Compatibility**: Versionado de APIs para mantener compatibilidad
- **Feature Flags**: Activación gradual de funcionalidades nuevas
- **Refactoring Continuo**: Pago constante de deuda técnica
- **Testing Automatizado**: Cobertura mínima requerida para cambios

## Referencias

- [Estándares de Desarrollo](./estandares-desarrollo.md)
- [Containerización y Despliegue](./containerizacion.md)
- [Monitoreo y Observabilidad](./monitoreo.md)
- [Spring Boot Reference Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Domain-Driven Design Reference](https://www.domainlanguage.com/ddd/)

