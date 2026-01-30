# ============================
# Etapa 1: Build (Compilación)
# ============================
FROM eclipse-temurin:25-jdk-alpine AS builder

# Metadata
LABEL stage=builder
LABEL maintainer="laboratory"

# Directorio de trabajo para la compilación
WORKDIR /build

# Copiar archivos de configuración de Maven
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Dar permisos de ejecución al wrapper de Maven
RUN chmod +x mvnw

# Descargar dependencias (capa cacheada si pom.xml no cambia)
RUN ./mvnw dependency:go-offline -B

# Copiar código fuente
COPY src src

# Compilar la aplicación (skip tests para build más rápido)
RUN ./mvnw clean package -DskipTests -B

# Extraer el nombre del JAR generado
RUN mkdir -p target/dependency && \
    cd target/dependency && \
    jar -xf ../*.jar

# ============================
# Etapa 2: Runtime (Ejecución)
# ============================
FROM eclipse-temurin:25-jre-alpine

# Metadata
LABEL maintainer="laboratory"
LABEL description="Base API - Spring Boot Application"
LABEL version="0.0.1-SNAPSHOT"

# Crear usuario no-root para seguridad
RUN addgroup -S spring && adduser -S spring -G spring

# Directorio de trabajo
WORKDIR /app

# Copiar artefactos desde la etapa de build
ARG DEPENDENCY=/build/target/dependency
COPY --from=builder ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=builder ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=builder ${DEPENDENCY}/BOOT-INF/classes /app

# Cambiar propietario de archivos al usuario spring
RUN chown -R spring:spring /app

# Cambiar a usuario no-root
USER spring:spring

# Puerto por defecto de Spring Boot
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Punto de entrada optimizado con flags de JVM
ENTRYPOINT ["java", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=75.0", \
            "-XX:+UseG1GC", \
            "-XX:+ExitOnOutOfMemoryError", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-cp", \
            "/app:/app/lib/*", \
            "com.ar.laboratory.baseapi.BaseApiApplication"]

