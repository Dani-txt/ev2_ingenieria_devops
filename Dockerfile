
# Dockerfile - saludo-service
# Multi-stage build

# 1ero se Descargan dependencias
FROM eclipse-temurin:17-jdk AS dependencies
WORKDIR /app
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN ./mvnw dependency:go-offline -B

# Ejecución de los tests
FROM eclipse-temurin:17-jdk AS test
WORKDIR /app
COPY . .
COPY --from=dependencies /root/.m2 /root/.m2
RUN ./mvnw test -B

# Compilación y empaquetación
FROM eclipse-temurin:17-jdk AS compile
WORKDIR /app
COPY --from=test /app /app
COPY --from=dependencies /root/.m2 /root/.m2
RUN ./mvnw clean package -DskipTests -B

# Imagen que va a producción (solo JRE, imagen mínima)
FROM eclipse-temurin:17-jre AS prod
WORKDIR /app

# No correr como root (el profe comentó que era buena práctica de seguridad)
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
USER appuser

COPY --from=compile /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
