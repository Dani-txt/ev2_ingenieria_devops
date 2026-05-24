# saludo-service — Pipeline CI/CD

**Ingeniería DevOps — Evaluación Parcial N°2**

*Integrantes: Dangelo Rojas y Daniel Nuñez | Profesor: Mauricio Velasquez*

Microservicio desarrollado en Spring Boot como base para implementar un pipeline CI/CD completo con GitHub Actions, Docker, SonarCloud y Snyk.

---

## Descripción del proyecto

`saludo-service` expone los siguientes endpoints REST:

| Endpoint | Método | Descripción |
|---|---|---|
| `/` | GET | Retorna mensaje de bienvenida con timestamp y versión |
| `/saludo?nombre=X` | GET | Retorna un saludo personalizado (default: Mundo) |
| `/actuator/health` | GET | Health check para monitoreo |
| `/swagger-ui/index.html` | GET | Documentación interactiva de la API |

**Tecnologías:** Spring Boot 3.4.5, Java 17, Maven, JaCoCo, Docker, Nginx, Springdoc OpenAPI 2.8.6.

---

## Arquitectura del Pipeline

```
[Push a main / Pull Request]
          │
          ▼
    [snyk-scan] ──── FALLA ──► Pipeline bloqueado
          │
          ▼
  [test + JaCoCo] ──── FALLA ──► sonar, build, deploy no corren
          │
          ▼
  [sonar-analysis] ──── FALLA ──► build, deploy no corren
          │
          ▼
       [build]
          │
          ▼
      [deploy] ← solo en push a main
```

---

## Etapas del Pipeline

**Job 1 — snyk-scan:** Escanea las dependencias Maven contra la base de datos CVE de Snyk. Si detecta vulnerabilidades de severidad HIGH o CRITICAL sin excepción documentada, el pipeline se detiene completamente. Genera un reporte JSON descargable como artefacto. Los PRs de Dependabot omiten este job ya que no tienen acceso a los secrets del repositorio.

**Job 2 — test:** Compila el proyecto, ejecuta todos los tests unitarios y de integración con JUnit 5 y MockMvc, y genera el reporte de cobertura con JaCoCo. Requiere un mínimo del 80% de cobertura de líneas para pasar. Los reportes quedan disponibles como artefactos en GitHub Actions.

**Job 3 — sonar-analysis:** Descarga el reporte JaCoCo del job anterior y lo envía junto al análisis de código a SonarCloud para evaluación estática de calidad: bugs, code smells, duplicación y vulnerabilidades en el propio código fuente.

**Job 4 — build:** Construye la imagen Docker usando el Dockerfile multi-stage. Etiqueta la imagen con el SHA del commit (`github.sha`) para trazabilidad completa.

**Job 5 — deploy:** Solo se ejecuta en push directo a `main` (no en PRs). Construye el JAR, levanta los servicios con Docker Compose, verifica que la app responde en `/actuator/health` con reintentos durante 2 minutos y ejecuta smoke tests básicos sobre los endpoints. El cleanup (`docker compose down -v`) se ejecuta siempre, incluso si algo falla.

---

## Trazabilidad

- Cada commit en `main` dispara el pipeline automáticamente.
- La imagen Docker se etiqueta con `${{ github.sha }}` (hash único del commit).
- Los artefactos generados (reporte JaCoCo, reporte Snyk JSON, logs de deploy) quedan vinculados al run ID en GitHub Actions con retención de 90 días.
- El historial de ejecuciones en la pestaña **Actions** permite rastrear qué commit causó cada falla o éxito.

---

## Calidad

- **Pruebas unitarias:** JUnit 5 + MockMvc para controladores y lógica de negocio.
- **Cobertura mínima:** 80% de líneas cubiertas (JaCoCo falla el build si no se alcanza).
- **Análisis estático:** SonarCloud evalúa bugs, vulnerabilidades y code smells en cada push.
- **Dependencias:** Dependabot revisa actualizaciones de dependencias Maven (diario) y GitHub Actions (semanal) y propone PRs automáticos.

---

## Seguridad

- **Snyk:** Escanea dependencias contra la base de datos CVE. Bloquea el pipeline si detecta vulnerabilidades HIGH o CRITICAL.
- **Política .snyk:** Los CVEs sin fix disponible en la línea Spring Boot 3.x se documentan en el archivo `.snyk` con razón y fecha de expiración, siguiendo la práctica estándar de gestión de riesgo aceptado.
- **Flujo de bloqueo:** `snyk-scan` falla → `test` no corre → `sonar-analysis` no corre → `deploy` no corre.
- **Docker:** El contenedor no corre como root (`USER appuser`), reduciendo la superficie de ataque.
- **Secrets:** Todas las credenciales se almacenan como secrets de GitHub Actions y nunca se hardcodean en el código.

---

## Orquestación con Docker Compose

Docker Compose orquesta 2 servicios conectados en la red interna `backend` (bridge):

**`app` — saludo-service (Spring Boot)**
- Puerto: `8080:8080`
- Límite: 512MB RAM, 1 CPU | Reserva: 256MB RAM, 0.5 CPU
- Health check: `GET /actuator/health` cada 15s, 5 reintentos, start_period 30s
- Volumen `app-logs` montado en `/app/logs` para persistencia de logs

**`nginx` — Proxy inverso**
- Puerto: `80:80`
- Límite: 128MB RAM, 0.5 CPU | Reserva: 64MB RAM, 0.25 CPU
- Solo arranca cuando `app` está healthy (`depends_on: condition: service_healthy`)
- Redirige `/` → `/docs` → `/swagger-ui/index.html` para exponer la documentación directamente

---

## Dockerfile — Multi-stage Build

El Dockerfile implementa 4 etapas independientes:

| Etapa | Imagen base | Propósito |
|---|---|---|
| `dependencies` | `maven:3.9-eclipse-temurin-17` | Descarga dependencias Maven en modo offline |
| `test` | `maven:3.9-eclipse-temurin-17` | Ejecuta los tests unitarios |
| `compile` | `maven:3.9-eclipse-temurin-17` | Compila y empaqueta el JAR |
| `prod` | `eclipse-temurin:17-jre` | Imagen final mínima solo con JRE y el JAR |

La imagen de producción no incluye el JDK ni Maven, reduciendo significativamente su tamaño y superficie de ataque.

---

## Cómo ejecutar localmente

**Con Docker Compose (recomendado):**
```bash
docker compose up --build
# App disponible en http://localhost (nginx) y http://localhost:8080 (directo)
# Swagger UI en http://localhost/docs
```

**Con Maven:**
```bash
mvn spring-boot:run
# App disponible en http://localhost:8080
```

**Solo los tests:**
```bash
mvn clean verify
# Reporte JaCoCo en target/site/jacoco/index.html
```

---

## Secrets y Variables requeridos en GitHub

Configurar en **Settings → Secrets and variables → Actions**:

| Nombre | Tipo | Descripción |
|---|---|---|
| `SNYK_TOKEN` | Secret | Token de autenticación de snyk.io |
| `SONAR_TOKEN` | Secret | Token de autenticación de sonarcloud.io |
| `SONAR_PROJECT_KEY` | Variable | Clave del proyecto en SonarCloud |
| `SONAR_ORGANIZATION` | Variable | Organización en SonarCloud |

El `GITHUB_TOKEN` es inyectado automáticamente por GitHub Actions, no requiere configuración manual.

---

## Uso de IA

- **Claude (Anthropic):** Apoyo en configuración del pipeline CI/CD, corrección del Dockerfile multi-stage, resolución de errores de autenticación en Snyk, configuración del archivo `.snyk` y estructuración del README.
- Todo el código fue revisado, entendido y adaptado manualmente por los integrantes del equipo.

---
