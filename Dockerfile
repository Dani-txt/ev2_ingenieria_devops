# Dockerfile - saludo-service
# Multi-stage build

# Etapa 1: Descargar dependencias
FROM maven:3.9-eclipse-temurin-17 AS dependencies
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Etapa 2: Ejecutar tests
FROM maven:3.9-eclipse-temurin-17 AS test
WORKDIR /app
COPY . .
COPY --from=dependencies /root/.m2 /root/.m2
RUN mvn test -B

# Etapa 3: Compilar y empaquetar
FROM maven:3.9-eclipse-temurin-17 AS compile
WORKDIR /app
COPY --from=test /app /app
COPY --from=dependencies /root/.m2 /root/.m2
RUN mvn clean package -DskipTests -B

# Etapa 4: Producción (solo JRE, imagen mínima)
FROM eclipse-temurin:17-jre AS prod
WORKDIR /app
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
USER appuser
COPY --from=compile /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]