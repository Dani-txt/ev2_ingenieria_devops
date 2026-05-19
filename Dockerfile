
# Dockerfile - saludo-service
# Multi-stage build

# 1ero se Descargan dependencias
FROM eclipse-temurin:17-jdk AS dependencies
WORKDIR /app
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Ejecución de los tests
FROM eclipse-temurin:17-jdk AS test
WORKDIR /app
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*
COPY . .
COPY --from=dependencies /root/.m2 /root/.m2
RUN mvn test -B

# Compilación y empaquetación
FROM eclipse-temurin:17-jdk AS compile
WORKDIR /app
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*
COPY --from=test /app /app
COPY --from=dependencies /root/.m2 /root/.m2
RUN mvn clean package -DskipTests -B

# Imagen que va a producción (solo JRE, imagen mínima)
FROM eclipse-temurin:17-jre AS prod
WORKDIR /app

# No correr como root (crear usuarios con privilegios especificos)
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
USER appuser
COPY --from=compile /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]