# Integración de Redis - Base API

## Descripción

Este proyecto integra Redis como sistema de caché siguiendo los principios de arquitectura hexagonal. Redis se implementa como un adaptador de salida (output adapter) a través del puerto `CachePort`.

## Arquitectura

### Capa de Dominio
- **CachePort**: Interface que define las operaciones de caché
  - `get(key)`: Obtener valor del caché
  - `put(key, value)`: Almacenar valor en caché
  - `evict(key)`: Eliminar valor del caché
  - `clear()`: Limpiar todo el caché
  - `exists(key)`: Verificar si existe una clave

### Capa de Adaptadores
- **RedisCacheAdapter**: Implementación de CachePort usando Redis
  - Serialización JSON con Jackson
  - TTL de 10 minutos por defecto
  - Manejo de errores sin afectar la funcionalidad principal
  - Logging detallado de operaciones

### Integración en Services
El `ExampleStatusService` utiliza el caché con el patrón **cache-aside**:
1. Intenta obtener el valor del caché
2. Si no existe (cache miss), consulta el repositorio
3. Almacena el resultado en caché para futuras consultas

## Configuración

### Dependencias Maven

```xml
<!-- Redis -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
</dependency>
```

### Configuración por Perfil

#### Local (application-local.yml)
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
  cache:
    redis:
      time-to-live: 300000  # 5 minutos
```

#### Development (application-dev.yml)
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
  cache:
    redis:
      time-to-live: 600000  # 10 minutos
```

#### Production (application-prod.yml)
```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
  cache:
    redis:
      time-to-live: 1800000  # 30 minutos
```

## Ejecutar Redis Localmente

### Opción 1: Docker Compose (Recomendado)

```bash
cd local
docker-compose up -d
```

Esto levantará:
- Redis en puerto 6379
- Con persistencia de datos
- Health check configurado

### Opción 2: Docker Manual

```bash
docker run -d \
  --name base-api-redis \
  -p 6379:6379 \
  redis:7-alpine
```

### Opción 3: Instalación Local

Descargar e instalar Redis desde: https://redis.io/download

```bash
redis-server
```

## Verificar Redis

### Redis CLI

```bash
# Conectarse a Redis
docker exec -it base-api-redis redis-cli

# O si está instalado localmente
redis-cli

# Comandos útiles
PING                           # Verificar conexión
KEYS example-status:*          # Ver todas las claves del caché
GET example-status:1           # Ver valor específico
TTL example-status:1           # Ver tiempo de vida restante
FLUSHALL                       # Limpiar todo el caché (usar con precaución)
```

## Operaciones de Caché

### Flujo de Caché en getExampleStatusById

1. **Request**: Cliente solicita `/base-api/example-status/1`
2. **Cache Check**: Service verifica si existe en Redis
   - **Cache Hit**: Retorna valor inmediatamente
   - **Cache Miss**: Continúa al paso 3
3. **Repository Query**: Consulta el InMemoryRepository
4. **Cache Store**: Almacena el resultado en Redis con TTL de 10 minutos
5. **Response**: Retorna el valor al cliente

### Estructura de Claves

```
example-status:{id}
```

Ejemplos:
- `example-status:1`
- `example-status:2`
- `example-status:3`

### TTL (Time To Live)

- **Local**: 5 minutos (300,000 ms)
- **Development**: 10 minutos (600,000 ms)
- **Production**: 30 minutos (1,800,000 ms)

## Ventajas de la Arquitectura Hexagonal con Redis

### 1. Independencia del Framework
El dominio no depende de Redis. El puerto `CachePort` define el contrato.

### 2. Facilidad de Testing
Puedes mockear `CachePort` en tests sin necesitar Redis.

### 3. Intercambiabilidad
Fácil cambiar de Redis a otro sistema de caché (Memcached, Hazelcast, etc.) solo implementando `CachePort`.

### 4. Manejo de Errores Graceful
Si Redis falla, la aplicación sigue funcionando consultando el repositorio directamente.

## Monitoreo de Redis

### Métricas de Caché

El servicio loggea:
- Cache hits: Cuando encuentra el valor en Redis
- Cache misses: Cuando debe consultar el repositorio
- Errores de Redis: Cuando falla alguna operación

### Logs Ejemplo

```
DEBUG c.a.l.b.a.s.ExampleStatusService - Found example status in cache: Service A
DEBUG c.a.l.b.a.o.c.RedisCacheAdapter - Cache hit for key: example-status:1
```

## Buenas Prácticas

### 1. Cache Invalidation
Cuando los datos cambian, invalida el caché:

```java
cachePort.evict(id);  // Eliminar entrada específica
cachePort.clear();    // Limpiar todo el caché
```

### 2. Serialización
Los modelos de dominio deben implementar `Serializable`:

```java
public class ExampleStatus implements Serializable {
    private static final long serialVersionUID = 1L;
    // ...
}
```

### 3. Timeout Configuración
Configura timeouts adecuados para evitar bloqueos:

```yaml
spring:
  data:
    redis:
      timeout: 2000ms
```

### 4. Pool de Conexiones
Configura el pool para manejar concurrencia:

```yaml
spring:
  data:
    redis:
      jedis:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
```

## Troubleshooting

### Error: Could not connect to Redis

**Problema**: La aplicación no puede conectarse a Redis.

**Solución**:
```bash
# Verificar que Redis esté corriendo
docker ps | grep redis

# Iniciar Redis si no está corriendo
docker-compose up -d redis

# Verificar logs de Redis
docker logs base-api-redis
```

### Error: Serialization Error

**Problema**: Error al serializar/deserializar objetos.

**Solución**:
- Verificar que las clases implementen `Serializable`
- Verificar que todos los campos tengan `serialVersionUID`
- Verificar configuración de Jackson en `RedisConfig`

### Performance Lento

**Problema**: Las operaciones de caché son lentas.

**Solución**:
- Reducir el tamaño de los objetos cacheados
- Aumentar el pool de conexiones
- Revisar configuración de timeout
- Verificar red/latencia a Redis

## Testing

### Test Sin Redis
Los tests unitarios no requieren Redis ya que mockean `CachePort`.

### Test de Integración con Redis
Para tests de integración, usa Testcontainers:

```java
@Testcontainers
@SpringBootTest
class RedisCacheIntegrationTest {
    
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);
    
    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }
    
    // tests...
}
```

## Próximos Pasos

### 1. Cache Warming
Implementar precarga de caché al inicio de la aplicación:

```java
@PostConstruct
public void warmCache() {
    List<ExampleStatus> all = repository.findAll();
    all.forEach(status -> cachePort.put(status.getId(), status));
}
```

### 2. Cache Aside Pattern Completo
Implementar invalidación automática al actualizar datos.

### 3. Distributed Caching
Para múltiples instancias, Redis ya proporciona caché distribuido.

### 4. Redis Pub/Sub
Implementar invalidación de caché entre instancias usando pub/sub.

### 5. Métricas de Caché
Integrar con Micrometer/Prometheus para métricas detalladas:
- Hit ratio
- Miss ratio
- Latency
- Size

## Referencias

- [Spring Data Redis](https://spring.io/projects/spring-data-redis)
- [Redis Documentation](https://redis.io/documentation)
- [Cache-Aside Pattern](https://docs.microsoft.com/en-us/azure/architecture/patterns/cache-aside)
