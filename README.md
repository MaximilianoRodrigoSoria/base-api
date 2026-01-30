# Base API

Aplicación backend basada en Java 25 y Spring Boot 4.0.2, diseñada siguiendo principios de Domain-Driven Design (DDD), Arquitectura Limpia y SOLID.

## Stack Tecnológico

- **Java**: 25
- **Spring Boot**: 4.0.2
- **Build Tool**: Maven
- **Base de Datos**: H2 (desarrollo), preparado para PostgreSQL/MySQL
- **Containerización**: Docker con multi-stage build

## Inicio Rápido

### Prerrequisitos

- Java 25 JDK
- Maven 3.8+
- Docker y Docker Compose (para ejecución containerizada)

### Ejecución Local

#### Con Maven

```bash
./mvnw spring-boot:run
```

#### Con Docker Compose

```bash
cd local
.\start.ps1  # Windows PowerShell
# o
./start.sh   # Linux/Mac
```

### Acceso a la Aplicación

Una vez iniciada, la aplicación estará disponible en:

- **API Base**: http://localhost:8080/base-api
- **Health Check**: http://localhost:8080/base-api/actuator/health
- **Métricas**: http://localhost:8080/base-api/actuator/metrics
- **H2 Console**: http://localhost:8080/base-api/h2-console

## Documentación Técnica

La documentación completa del proyecto se encuentra en la carpeta `/docs`:

| Documento | Descripción |
|-----------|-------------|
| [Arquitectura General](./docs/arquitectura.md) | Visión general, stack tecnológico, principios y decisiones arquitectónicas |
| [Containerización y Despliegue](./docs/containerizacion.md) | Estrategia Docker, orquestación y deployment |
| [Estándares de Desarrollo](./docs/estandares-desarrollo.md) | Convenciones de código, patrones y buenas prácticas |
| [Monitoreo y Observabilidad](./docs/monitoreo.md) | Health checks, métricas, logging y alertas |

Ver [índice completo de documentación](./docs/README.md).

## Estructura del Proyecto

```
base-api/
├── docs/                    # Documentación técnica
├── local/                   # Configuración Docker local
│   ├── docker-compose.yml
│   ├── start.ps1
│   └── docker.ps1
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/ar/laboratory/baseapi/
│   │   └── resources/
│   │       ├── application.properties
│   │       └── application-local.properties
│   └── test/
├── Dockerfile
├── pom.xml
└── README.md
```

## Comandos Útiles

### Maven

```bash
# Compilar
./mvnw clean package

# Tests
./mvnw test

# Skip tests
./mvnw clean package -DskipTests
```

### Docker

```bash
# Build imagen
docker build -t base-api:latest .

# Run contenedor
docker run -p 8080:8080 base-api:latest

# Con Docker Compose
cd local
docker-compose up -d

# Ver logs
docker-compose logs -f

# Detener
docker-compose down
```

### Scripts de Gestión (PowerShell)

```bash
cd local

# Iniciar servicios
.\docker.ps1 start

# Ver logs
.\docker.ps1 logs

# Verificar health
.\docker.ps1 health

# Detener servicios
.\docker.ps1 stop

# Ver todos los comandos
.\docker.ps1 help
```

## Contribución

Antes de contribuir, revisar:

1. [Estándares de Desarrollo](./docs/estandares-desarrollo.md) - Convenciones obligatorias
2. [Arquitectura General](./docs/arquitectura.md) - Principios arquitectónicos
3. Checklist de revisión de código en documentación de estándares

## Licencia

Este proyecto es parte de Laboratory AR.

---

Para más información, consultar la [documentación técnica completa](./docs/README.md).
