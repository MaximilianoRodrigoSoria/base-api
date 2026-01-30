# Arquitectura Hexagonal - Base API

## Descripción

Este proyecto implementa una arquitectura hexagonal (también conocida como arquitectura de puertos y adaptadores) siguiendo los principios de clean architecture.

## Requisitos

- **Java 25**: El proyecto está configurado para Java 25
- **Maven 3.8+**: Para la gestión de dependencias y construcción
- **Spring Boot 4.0.2**: Framework base de la aplicación

## Estructura del Proyecto

```
com.ar.laboratory.baseapi
│
├── domain/                          # Capa de Dominio (núcleo)
│   ├── model/                       # Modelos de dominio (entidades)
│   │   ├── ExampleStatus.java
│   │   └── HealthStatus.java
│   │
│   └── ports/                       # Interfaces (contratos)
│       ├── in/                      # Puertos de entrada (casos de uso)
│       │   ├── ExampleStatusUseCase.java
│       │   └── HealthCheckUseCase.java
│       │
│       └── out/                     # Puertos de salida (repositorios)
│           └── ExampleRepositoryPort.java
│
├── application/                     # Capa de Aplicación
│   └── service/                     # Implementación de casos de uso
│       ├── ExampleStatusService.java
│       └── HealthCheckService.java
│
├── adapters/                        # Adaptadores
│   ├── in/                          # Adaptadores de entrada
│   │   └── web/                     # Adaptador REST API
│   │       ├── controller/          # Controladores REST
│   │       │   ├── ExampleStatusController.java
│   │       │   └── HealthCheckController.java
│   │       │
│   │       ├── dto/                 # Data Transfer Objects
│   │       │   ├── ExampleStatusResponse.java
│   │       │   └── HealthCheckResponse.java
│   │       │
│   │       └── mapper/              # Mappers (dominio ↔ DTO)
│   │           └── ExampleStatusMapper.java
│   │
│   └── out/                         # Adaptadores de salida
│       └── persistence/             # Adaptador de persistencia
│           └── InMemoryExampleRepository.java
│
└── config/                          # Configuración
    └── OpenApiConfig.java           # Configuración de Swagger/OpenAPI
```

## Capas de la Arquitectura

### 1. Dominio (Domain)

El núcleo de la aplicación, contiene:
- **Modelos**: Entidades de negocio puras, sin dependencias de frameworks
- **Ports**: Interfaces que definen contratos para la comunicación entre capas

**Características:**
- No depende de ninguna capa externa
- Contiene la lógica de negocio pura
- Framework-independent

### 2. Aplicación (Application)

Implementa los casos de uso del negocio:
- **Services**: Implementan los puertos de entrada (use cases)
- Orquestan el flujo de datos entre puertos de entrada y salida
- Contienen la lógica de aplicación

### 3. Adaptadores (Adapters)

#### Adaptadores de Entrada (In)
- **Controllers REST**: Exponen la API HTTP
- **DTOs**: Objetos de transferencia para las respuestas
- **Mappers**: Convierten entre modelos de dominio y DTOs

#### Adaptadores de Salida (Out)
- **Repositories**: Implementan los puertos de salida
- **InMemoryExampleRepository**: Implementación mock con datos en memoria

### 4. Configuración (Config)

Configuración de infraestructura:
- **OpenAPI/Swagger**: Documentación de la API

## Flujo de Datos

```
[Cliente HTTP]
      ↓
[Controller] (Adapter In)
      ↓
[Service] (Application - Use Case)
      ↓
[Repository Port] (Domain Port Out)
      ↓
[Repository Implementation] (Adapter Out)
```

## Endpoints Disponibles

### Health Check
- **GET** `/base-api/health`
  - Retorna el estado de salud de la aplicación
  - Respuesta 200 con información de versión y timestamp

### Example Status
- **GET** `/base-api/example-status`
  - Retorna todos los estados de ejemplo
  
- **GET** `/base-api/example-status/active`
  - Retorna solo los estados activos
  
- **GET** `/base-api/example-status/{id}`
  - Retorna un estado específico por ID
  - Respuesta 404 si no existe

### Documentación API
- **Swagger UI**: `/base-api/swagger-ui.html`
- **OpenAPI JSON**: `/base-api/api-docs`

### Actuator (Monitoreo)
- **GET** `/base-api/actuator/health`
- **GET** `/base-api/actuator/info`
- **GET** `/base-api/actuator/metrics`

## Beneficios de la Arquitectura Hexagonal

1. **Independencia de Frameworks**: El dominio no depende de Spring, JPA, etc.
2. **Testabilidad**: Fácil de testear con mocks
3. **Mantenibilidad**: Separación clara de responsabilidades
4. **Flexibilidad**: Fácil cambiar adaptadores (ej: de in-memory a base de datos real)
5. **Escalabilidad**: Cada capa puede evolucionar independientemente

## Testing

El proyecto incluye tests unitarios para:
- **Services**: Verifican la lógica de negocio
- **Controllers**: Verifican los endpoints HTTP con MockMvc
- **Cobertura**: Tests para casos positivos y negativos

### Ejecutar Tests

```bash
mvn test
```

## Compilar y Ejecutar

### Compilar
```bash
mvn clean install
```

### Ejecutar
```bash
mvn spring-boot:run
```

### Ejecutar con perfil
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

## OpenAPI/Swagger

La configuración de OpenAPI lee dinámicamente:
- **Título**: Desde `app.name` en application.yml
- **Descripción**: Desde `app.description` en application.yml  
- **Versión**: Desde `project.version` del pom.xml (inyectada en runtime via BuildProperties)

Acceder a Swagger UI:
```
http://localhost:8080/base-api/swagger-ui.html
```

## Notas Importantes

- El repositorio actual es **in-memory** (datos mock)
- Para producción, implementar adaptador con base de datos real
- La versión se inyecta automáticamente desde Maven usando `@project.version@`
- El proyecto usa **solo archivos YML** para configuración (no .properties)
