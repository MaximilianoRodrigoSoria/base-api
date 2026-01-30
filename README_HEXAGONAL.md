# Base API - Arquitectura Hexagonal

## Descripción del Proyecto

API REST base implementada con Spring Boot 4.0.2 y arquitectura hexagonal (puertos y adaptadores), configurada para Java 25.

## Características Principales

- **Arquitectura Hexagonal**: Separación clara entre dominio, aplicación y adaptadores
- **OpenAPI/Swagger**: Documentación automática de la API
- **Spring Boot 4.0.2**: Framework moderno y estable
- **Java 25**: Configuración para la última versión de Java
- **Monitoreo**: Spring Boot Actuator integrado
- **Logging**: Logback configurado
- **Tests**: Cobertura de tests unitarios con JUnit 5 y MockMvc
- **Solo YML**: Toda la configuración en archivos YAML

## Requisitos del Sistema

### Para Desarrollo y Ejecución
- **Java 25 (JDK 25)**: Requerido para compilar y ejecutar
- **Maven 3.8+**: Para gestión de dependencias
- **Puerto 8080**: Disponible para la aplicación

### Para Instalar Java 25

Si actualmente tienes otra versión de Java, necesitarás instalar Java 25:

1. Descargar JDK 25 desde: https://jdk.java.net/25/
2. Configurar JAVA_HOME apuntando al JDK 25
3. Verificar instalación:
   ```bash
   java -version
   # Debe mostrar: java version "25"
   ```

## Estructura del Proyecto

```
base-api/
├── src/main/java/com/ar/laboratory/baseapi/
│   ├── domain/                 # Capa de dominio (modelos y ports)
│   ├── application/            # Capa de aplicación (services)
│   ├── adapters/              # Adaptadores (in: web, out: persistence)
│   └── config/                # Configuración (OpenAPI)
│
├── src/main/resources/
│   ├── application.yml        # Configuración principal
│   ├── application-local.yml  # Perfil local
│   ├── application-dev.yml    # Perfil desarrollo
│   ├── application-prod.yml   # Perfil producción
│   └── logback-spring.xml     # Configuración de logs
│
└── src/test/java/             # Tests unitarios
```

## Instalación y Configuración

### 1. Clonar el repositorio
```bash
git clone <repository-url>
cd base-api
```

### 2. Compilar el proyecto
```bash
mvn clean install
```

### 3. Ejecutar la aplicación
```bash
# Con perfil por defecto
mvn spring-boot:run

# Con perfil local
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Con perfil dev
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 4. Verificar que está funcionando
```bash
curl http://localhost:8080/base-api/health
```

## Endpoints Disponibles

### API Principal

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/base-api/health` | Health check personalizado |
| GET | `/base-api/example-status` | Obtener todos los estados |
| GET | `/base-api/example-status/active` | Obtener estados activos |
| GET | `/base-api/example-status/{id}` | Obtener estado por ID |

### Documentación API
| URL | Descripción |
|-----|-------------|
| http://localhost:8080/base-api/swagger-ui.html | Interfaz Swagger UI |
| http://localhost:8080/base-api/api-docs | OpenAPI JSON |

### Actuator (Monitoreo)
| URL | Descripción |
|-----|-------------|
| http://localhost:8080/base-api/actuator/health | Estado de salud |
| http://localhost:8080/base-api/actuator/info | Información de la app |
| http://localhost:8080/base-api/actuator/metrics | Métricas |

### Consola H2 (Solo en local/dev)
| URL | Descripción |
|-----|-------------|
| http://localhost:8080/base-api/h2-console | Consola de base de datos H2 |

## Arquitectura Hexagonal

### Capas

1. **Domain (Dominio)**
   - Modelos de negocio puros
   - Ports (interfaces) que definen contratos
   - Sin dependencias externas

2. **Application (Aplicación)**
   - Services que implementan casos de uso
   - Lógica de negocio
   - Orquestación entre ports

3. **Adapters (Adaptadores)**
   - **In**: Controllers REST, DTOs, Mappers
   - **Out**: Repositories (actualmente in-memory)

### Ejemplo de Flujo

```
Cliente → Controller → Mapper → Service → Repository → Base de Datos Mock
   ↓                                            ↑
   └──────────── Response DTO ←────────────────┘
```

## Testing

### Ejecutar todos los tests
```bash
mvn test
```

### Ejecutar tests con cobertura
```bash
mvn test jacoco:report
```

### Tests incluidos
- ✅ Tests unitarios de servicios
- ✅ Tests de controladores con MockMvc
- ✅ Tests de casos positivos y negativos
- ✅ Validación de respuestas HTTP

## Configuración de OpenAPI

La documentación OpenAPI se genera automáticamente y toma:
- **Título**: Del property `app.name` en application.yml
- **Descripción**: Del property `app.description` en application.yml
- **Versión**: Del `project.version` del pom.xml

Para personalizar, editar `application.yml`:
```yaml
app:
  name: Tu API Name
  description: Tu descripción personalizada
```

## Perfiles de Spring

### Local
```yaml
# application-local.yml
- Base de datos H2 en memoria
- Actuator completo expuesto
- Logs detallados (DEBUG)
```

### Dev
```yaml
# application-dev.yml
- Base de datos H2 en memoria
- Actuator con más endpoints
- Logs DEBUG para desarrollo
```

### Prod
```yaml
# application-prod.yml
- Actuator limitado (solo health, info)
- Logs WARN/INFO
- H2 console deshabilitada
```

## Logs

Los logs se configuran en `logback-spring.xml` y están organizados por:
- **Console**: Salida en consola con formato
- **Niveles**: INFO, DEBUG, WARN, ERROR
- **Paquetes**: Logs específicos por paquete

## Próximos Pasos

### Para un Entorno Real

1. **Reemplazar InMemoryRepository** por una implementación real:
   - Agregar dependencia de base de datos (PostgreSQL, MySQL, etc.)
   - Crear entidades JPA
   - Implementar ExampleRepositoryPort con JPA

2. **Seguridad**:
   - Agregar Spring Security
   - Implementar autenticación JWT
   - Configurar CORS

3. **Validaciones**:
   - Agregar validaciones de entrada con Bean Validation
   - Manejo global de excepciones

4. **Métricas avanzadas**:
   - Integrar con Prometheus
   - Dashboard con Grafana

## Documentación Adicional

Ver documentación detallada en:
- [Arquitectura Hexagonal](./docs/arquitectura-hexagonal.md)
- [Estándares de Desarrollo](./docs/estandares-desarrollo.md)
- [Operaciones](./docs/operaciones.md)
- [Monitoreo](./docs/monitoreo.md)

## Contribuir

1. Fork el proyecto
2. Crear una rama feature (`git checkout -b feature/AmazingFeature`)
3. Commit los cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abrir un Pull Request

## Licencia

Este proyecto es un template base para APIs REST con Spring Boot.

## Contacto

Laboratory Team - contact@laboratory.ar

## Notas Importantes

- **Java 25 es requerido**: Asegúrate de tener JDK 25 instalado
- **Solo configuración YML**: No uses archivos .properties
- **Mock Repository**: Los datos son estáticos en memoria, no persisten
- **Build Info**: Maven genera automáticamente build-info.properties con la versión del proyecto
