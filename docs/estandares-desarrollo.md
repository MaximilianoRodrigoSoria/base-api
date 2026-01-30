# Estándares de Desarrollo

## Objetivo

Establecer convenciones, principios y prácticas obligatorias para el desarrollo de código en el proyecto Base API, garantizando consistencia, calidad y mantenibilidad.

## Alcance

Este documento aplica a todo código Java, configuración Spring Boot, tests, documentación y revisiones de código.

## Principios Fundamentales

### SOLID

Todos los componentes deben adherir estrictamente a:

- **Single Responsibility**: Una clase, un propósito, una razón para cambiar
- **Open/Closed**: Extensible mediante herencia/composición, no modificación
- **Liskov Substitution**: Subtipos reemplazables por sus tipos base sin alterar corrección
- **Interface Segregation**: Contratos específicos sobre contratos generales
- **Dependency Inversion**: Dependencia de abstracciones, no de implementaciones concretas

### Domain-Driven Design

- **Ubiquitous Language**: Terminología del dominio en código, documentación y conversaciones
- **Bounded Contexts**: Límites explícitos entre subdominios
- **Aggregates**: Consistencia transaccional dentro de agregados, eventual entre agregados
- **Entidades vs Value Objects**: Identidad vs igualdad por valor

### Clean Code

- Nombres expresivos que revelen intención
- Funciones pequeñas con un solo nivel de abstracción
- Ausencia de comentarios obvios (código autoexplicativo)
- Manejo explícito de excepciones (nunca capturas genéricas)

## Convenciones de Código

### Estructura de Clases

```java
public class ExampleService {
    
    // 1. Constantes estáticas
    private static final String DEFAULT_VALUE = "default";
    
    // 2. Atributos finales (dependencias)
    private final DependencyRepository repository;
    private final DependencyMapper mapper;
    
    // 3. Constructor (inyección de dependencias)
    public ExampleService(DependencyRepository repository, DependencyMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }
    
    // 4. Métodos públicos
    public ExampleResponse execute(ExampleRequest request) {
        // Implementación
    }
    
    // 5. Métodos privados
    private void helperMethod() {
        // Implementación
    }
}
```

### Nomenclatura

| Elemento | Convención | Ejemplo |
|----------|------------|---------|
| Paquetes | lowercase sin separadores | `com.ar.laboratory.baseapi.service` |
| Clases | PascalCase, sustantivos | `PaymentService`, `OrderRepository` |
| Interfaces | PascalCase sin prefijo 'I' | `PaymentProcessor`, `OrderValidator` |
| Métodos | camelCase, verbos | `processPayment()`, `validateOrder()` |
| Variables | camelCase, sustantivos | `orderId`, `paymentAmount` |
| Constantes | UPPER_SNAKE_CASE | `MAX_RETRY_ATTEMPTS`, `DEFAULT_TIMEOUT` |
| Records | PascalCase terminado en Request/Response | `CreateOrderRequest`, `PaymentResponse` |
| Excepciones | PascalCase terminado en Exception | `OrderNotFoundException`, `PaymentValidationException` |

### Inyección de Dependencias

**OBLIGATORIO: Constructor Injection**

```java
@Service
@RequiredArgsConstructor  // Lombok genera constructor
public class OrderService {
    
    private final OrderRepository repository;
    private final OrderValidator validator;
    private final PaymentClient paymentClient;
    
    // Métodos de servicio
}
```

**PROHIBIDO: Field Injection**

```java
// NUNCA hacer esto
@Service
public class OrderService {
    
    @Autowired  // PROHIBIDO
    private OrderRepository repository;
}
```

**Razones**:
- Facilita testing (mocking de dependencias)
- Hace explícitas las dependencias de la clase
- Permite inmutabilidad (`final`)
- Previene NullPointerException en construcción

### Manejo de Excepciones

**Excepciones de Negocio**

```java
/**
 * Se lanza cuando una orden no se encuentra en el sistema.
 */
public class OrderNotFoundException extends RuntimeException {
    
    private final String orderId;
    
    public OrderNotFoundException(String orderId) {
        super(String.format("Order not found: %s", orderId));
        this.orderId = orderId;
    }
    
    public String getOrderId() {
        return orderId;
    }
}
```

**Reglas**:
- Extender `RuntimeException` (no checked exceptions)
- Nombre descriptivo terminado en `Exception`
- Mensaje claro con contexto
- Incluir datos relevantes como atributos

**Manejo Centralizado**

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(OrderNotFoundException ex) {
        log.warn("Order not found: {}", ex.getOrderId());
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse.of("ORDER_NOT_FOUND", ex.getMessage()));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse.of("INTERNAL_ERROR", "An unexpected error occurred"));
    }
}
```

**PROHIBIDO**:
```java
// NUNCA capturar Exception genérico sin análisis
try {
    operation();
} catch (Exception e) {  // PROHIBIDO
    // ...
}

// NUNCA lanzar Exception genérico
public void method() throws Exception {  // PROHIBIDO
    // ...
}
```

### DTOs y Records

**Request/Response Separados**

```java
// Request
public record CreateOrderRequest(
    @NotBlank String customerId,
    @NotEmpty List<@Valid OrderItemRequest> items,
    @NotNull BigDecimal totalAmount
) {}

// Response
public record OrderResponse(
    String orderId,
    String customerId,
    OrderStatus status,
    LocalDateTime createdAt
) {}
```

**Reglas**:
- Records para DTOs inmutables
- Validación Jakarta en records de entrada
- No reutilizar DTOs entre request y response
- No exponer entidades directamente en APIs

### Validación

**Declarativa en DTOs**

```java
public record CreatePaymentRequest(
    @NotBlank(message = "Order ID is required")
    String orderId,
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    BigDecimal amount,
    
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    String currency
) {}
```

**En Controllers**

```java
@PostMapping
public ResponseEntity<PaymentResponse> create(@Valid @RequestBody CreatePaymentRequest request) {
    // @Valid activa validación automática
}
```

**Validaciones Complejas**

Para lógica de negocio compleja:

```java
@Component
@RequiredArgsConstructor
public class OrderValidator {
    
    private final OrderRepository repository;
    
    /**
     * Valida que una orden sea elegible para pago.
     *
     * @param orderId identificador de la orden
     * @throws OrderNotEligibleException si la orden no cumple criterios
     */
    public void validatePaymentEligibility(String orderId) {
        Order order = repository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
            
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new OrderNotEligibleException(orderId, "Order is not in PENDING status");
        }
        
        if (order.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new OrderNotEligibleException(orderId, "Order amount must be positive");
        }
    }
}
```

### Logging

**Niveles**

| Nivel | Uso |
|-------|-----|
| ERROR | Errores que requieren intervención inmediata |
| WARN | Situaciones anómalas que no impiden funcionamiento |
| INFO | Eventos importantes del flujo de negocio |
| DEBUG | Información detallada para debugging |
| TRACE | Información extremadamente detallada |

**Prácticas**

```java
@Slf4j
@Service
public class PaymentService {
    
    public PaymentResponse processPayment(String orderId, PaymentRequest request) {
        // Entrada a operación crítica
        log.info("Processing payment for order: {}", orderId);
        
        try {
            Payment payment = executePayment(request);
            
            // Éxito
            log.info("Payment processed successfully. PaymentId: {}, OrderId: {}", 
                     payment.getId(), orderId);
            
            return mapper.toResponse(payment);
            
        } catch (PaymentGatewayException ex) {
            // Error de negocio
            log.error("Payment failed for order: {}. Reason: {}", orderId, ex.getMessage());
            throw new PaymentProcessingException(orderId, ex);
        }
    }
    
    private Payment executePayment(PaymentRequest request) {
        // Detalle técnico
        log.debug("Executing payment with gateway: {}", request.getGateway());
        
        // Implementación
    }
}
```

**PROHIBIDO**:
```java
// NO loggear datos sensibles
log.info("Payment with card: {}", request.getCardNumber());  // PROHIBIDO

// NO loggear stacktraces en INFO
log.info("Error occurred", exception);  // PROHIBIDO - usar ERROR

// NO concatenar strings en logs
log.info("Processing order: " + orderId);  // PROHIBIDO - usar placeholders {}
```

### Null Safety

**Preferir `Optional` en retornos**

```java
public interface OrderRepository extends JpaRepository<Order, String> {
    
    Optional<Order> findByCustomerId(String customerId);
}

public class OrderService {
    
    public OrderResponse findByCustomerId(String customerId) {
        return repository.findByCustomerId(customerId)
            .map(mapper::toResponse)
            .orElseThrow(() -> new OrderNotFoundException(customerId));
    }
}
```

**Validación explícita con `Objects`**

```java
import java.util.Objects;

public void processOrder(Order order) {
    Objects.requireNonNull(order, "Order cannot be null");
    Objects.requireNonNull(order.getId(), "Order ID cannot be null");
    
    if (Objects.isNull(order.getCustomer())) {
        throw new IllegalStateException("Order must have a customer");
    }
    
    // Procesamiento
}
```

**PROHIBIDO**:
```java
// NO retornar null cuando Optional es apropiado
public Order findByCustomerId(String customerId) {
    return repository.findByCustomerId(customerId);  // PROHIBIDO - puede ser null
}

// NO usar == null / != null
if (order == null) {  // Preferir Objects.isNull(order)
}
```

## Documentación

### JavaDoc Obligatorio

**Métodos públicos de servicios**

```java
/**
 * Procesa el pago de una orden garantizando idempotencia y consistencia transaccional.
 *
 * <p>Valida elegibilidad de la orden, reserva fondos en el gateway de pagos
 * y actualiza el estado de la orden de forma atómica.
 *
 * @param orderId identificador único de la orden
 * @param request {@code PaymentRequest} con método de pago y metadatos
 * @return {@code PaymentResponse} con resultado del procesamiento
 * @throws OrderNotFoundException si la orden no existe
 * @throws OrderNotEligibleException si la orden no cumple criterios para pago
 * @throws PaymentProcessingException si el gateway rechaza el pago
 * @see OrderValidator#validatePaymentEligibility(String)
 */
@Transactional
public PaymentResponse processPayment(String orderId, PaymentRequest request) {
    // Implementación
}
```

**Clases y componentes principales**

```java
/**
 * Servicio de dominio para gestión del ciclo de vida de pagos.
 *
 * <p>Responsabilidades:
 * <ul>
 *   <li>Procesamiento de pagos contra gateways externos</li>
 *   <li>Validación de elegibilidad de órdenes</li>
 *   <li>Garantía de idempotencia en operaciones de pago</li>
 *   <li>Coordinación con servicio de órdenes para actualización de estados</li>
 * </ul>
 *
 * <p>Este servicio es transaccional y garantiza consistencia entre
 * la entidad Payment y el estado de la Order asociada.
 *
 * @see PaymentRepository
 * @see OrderService
 * @see PaymentGatewayClient
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    // Implementación
}
```

### Comentarios en Código

**Permitidos**:
- Justificación de decisiones técnicas no obvias
- Workarounds temporales con referencia a issue
- Explicación de algoritmos complejos

**PROHIBIDOS**:
- Comentarios que repiten el código
- Código comentado (usar control de versiones)
- TODOs sin contexto ni responsable

```java
// PERMITIDO
// Workaround para bug en librería externa v2.3.1
// Ver: https://github.com/library/issues/1234
// TODO(johndoe): Eliminar cuando se actualice a v2.4.0
payment.setStatus(PaymentStatus.PENDING);

// PROHIBIDO
// Setea el status a pending
payment.setStatus(PaymentStatus.PENDING);  // Obvio
```

## Testing

### Cobertura Mínima

- **Servicios de negocio**: 80% coverage
- **Validadores**: 90% coverage
- **Mappers**: 70% coverage
- **Controllers**: Endpoints críticos con integración tests

### Estructura de Tests

```java
@DisplayName("PaymentService: processPayment()")
class PaymentServiceTest {
    
    @Mock
    private PaymentRepository paymentRepository;
    
    @Mock
    private OrderService orderService;
    
    @Mock
    private PaymentGatewayClient gatewayClient;
    
    @InjectMocks
    private PaymentService paymentService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    @DisplayName("should_ProcessPaymentSuccessfully_When_OrderIsEligible")
    void should_ProcessPaymentSuccessfully_When_OrderIsEligible() {
        // Given: orden válida y gateway responde exitosamente
        String orderId = "ORD-123";
        PaymentRequest request = createValidRequest();
        
        when(orderService.validateEligibility(orderId)).thenReturn(true);
        when(gatewayClient.charge(any())).thenReturn(createSuccessResponse());
        when(paymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        
        // When: se procesa el pago
        PaymentResponse response = paymentService.processPayment(orderId, request);
        
        // Then: el pago se registra y la respuesta contiene datos correctos
        assertNotNull(response);
        assertEquals(PaymentStatus.COMPLETED, response.getStatus());
        verify(paymentRepository).save(any(Payment.class));
        verify(orderService).updateStatus(orderId, OrderStatus.PAID);
    }
    
    @Test
    @DisplayName("should_ThrowOrderNotEligibleException_When_OrderStatusIsNotPending")
    void should_ThrowOrderNotEligibleException_When_OrderStatusIsNotPending() {
        // Given: orden en estado no elegible
        String orderId = "ORD-456";
        
        when(orderService.validateEligibility(orderId))
            .thenThrow(new OrderNotEligibleException(orderId, "Status is COMPLETED"));
        
        // When/Then: se lanza excepción
        assertThrows(OrderNotEligibleException.class, 
            () -> paymentService.processPayment(orderId, createValidRequest()));
        
        verify(gatewayClient, never()).charge(any());
        verify(paymentRepository, never()).save(any());
    }
}
```

### Nombres de Tests

**Patrón**: `should_ExpectedBehavior_When_StateUnderTest`

Ejemplos:
- `should_ReturnOrder_When_OrderExists`
- `should_ThrowNotFoundException_When_OrderDoesNotExist`
- `should_UpdateOrderStatus_When_PaymentIsSuccessful`

## Transaccionalidad

### En Servicios

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // Default para reads
public class OrderService {
    
    private final OrderRepository repository;
    
    // Hereda readOnly=true
    public OrderResponse findById(String orderId) {
        // Solo lectura
    }
    
    @Transactional  // Sobrescribe para escritura
    public OrderResponse createOrder(CreateOrderRequest request) {
        // Operación de escritura transaccional
    }
    
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void processPayment(String orderId) {
        // Transacción con aislamiento estricto
    }
}
```

### NUNCA en Repositorios

```java
// CORRECTO - sin @Transactional
public interface OrderRepository extends JpaRepository<Order, String> {
    Optional<Order> findByCustomerId(String customerId);
}
```

## Refactoring

### Disparadores Obligatorios

| Situación | Acción Requerida |
|-----------|------------------|
| Clase > 300 líneas | Evaluar responsabilidades y extraer |
| Método > 50 líneas | Dividir en métodos privados |
| Duplicación de lógica (>3 veces) | Extraer a método/clase compartida |
| Complejidad ciclomática > 10 | Simplificar condicionales |
| Dependencias > 5 en constructor | Evaluar cohesión y separar responsabilidades |

### Clasificación de Extracciones

```java
// Helpers puros → util/*Utils.java
public final class DateUtils {
    private DateUtils() {}  // Constructor privado
    
    public static LocalDate parseIsoDate(String date) {
        // Lógica pura sin dependencias
    }
}

// Lógica de negocio → service/*Service.java
@Service
@RequiredArgsConstructor
public class OrderValidationService {
    private final OrderRepository repository;
    
    public void validateOrder(Order order) {
        // Lógica con dependencias
    }
}

// Transformaciones → mapper/*Mapper.java
@Component
public class OrderMapper {
    public OrderResponse toResponse(Order order) {
        // Conversión DTO ↔ Entity
    }
}
```

## Patrones Recomendados

### Builder para Entidades Complejas

```java
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    
    @Id
    private String id;
    
    private String customerId;
    
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items;
    
    private BigDecimal totalAmount;
    
    private LocalDateTime createdAt;
}

// Uso
Order order = Order.builder()
    .id(UUID.randomUUID().toString())
    .customerId(request.getCustomerId())
    .status(OrderStatus.PENDING)
    .items(convertItems(request.getItems()))
    .totalAmount(calculateTotal(request.getItems()))
    .createdAt(LocalDateTime.now())
    .build();
```

### Strategy para Variabilidad de Comportamiento

```java
public interface PaymentStrategy {
    PaymentResult execute(PaymentRequest request);
}

@Component
public class CreditCardPaymentStrategy implements PaymentStrategy {
    @Override
    public PaymentResult execute(PaymentRequest request) {
        // Implementación para tarjeta de crédito
    }
}

@Component
public class DebitCardPaymentStrategy implements PaymentStrategy {
    @Override
    public PaymentResult execute(PaymentRequest request) {
        // Implementación para tarjeta de débito
    }
}

@Service
@RequiredArgsConstructor
public class PaymentService {
    
    private final Map<PaymentMethod, PaymentStrategy> strategies;
    
    public PaymentResponse process(PaymentRequest request) {
        PaymentStrategy strategy = strategies.get(request.getMethod());
        
        if (strategy == null) {
            throw new UnsupportedPaymentMethodException(request.getMethod());
        }
        
        return strategy.execute(request);
    }
}
```

## Revisión de Código

### Checklist Obligatorio

- [ ] Inyección por constructor
- [ ] Excepciones específicas (no genéricas)
- [ ] JavaDoc en métodos públicos
- [ ] Tests para lógica de negocio
- [ ] Logging apropiado (niveles y contexto)
- [ ] Validación en límites de API
- [ ] No exposición de entidades en APIs
- [ ] Transacciones en servicios, no en repositorios
- [ ] Nombres expresivos de clases, métodos y variables
- [ ] Sin código comentado ni TODOs sin contexto
- [ ] Uso de `Objects.isNull()` / `Objects.nonNull()` en lugar de `== null`
- [ ] `Optional` en retornos que pueden no tener valor

### Criterios de Rechazo

Rechazo automático si:
- Field injection (`@Autowired` en atributos)
- Captura o lanzamiento de `Exception` genérica
- Código sin tests cuando añade lógica de negocio
- Métodos públicos sin JavaDoc
- Exposición de entidades directamente en controllers
- Logging de datos sensibles

## Referencias

- [Arquitectura General](./arquitectura.md)
- [Containerización y Despliegue](./containerizacion.md)
- [Monitoreo y Observabilidad](./monitoreo.md)
- [Clean Code - Robert C. Martin](https://www.amazon.com/Clean-Code-Handbook-Software-Craftsmanship/dp/0132350882)
- [Effective Java - Joshua Bloch](https://www.amazon.com/Effective-Java-Joshua-Bloch/dp/0134685997)

