## Ingeniería DevOps: Evaluación dos**

*Integrantes: Dangelo Rojas, Daniel Nuñez*

# saludo-service — Pipeline CI/CD

Microservicio básico desarrollado en Spring Boot. Será usado como base para implementar un pipeline CI/CD completo con GitHub Actions, Docker, SonarCloud y Snyk.

---

## Descripción del proyecto

`saludo-service` expone tres endpoints REST:

| Endpoint | Método | Descripción |
|---|---|---|
| `/` | GET | Retorna "Hola Mundo" y descripcion |
| `/saludo?nombre=X` | GET | Retorna un saludo personalizado |
| `/actuator/health` | GET | Health check para monitoreo |

Tecnologías: Spring Boot 3.2, Java 17, Maven, JaCoCo, Docker, Nginx.

---

## Arquitectura del Pipeline

```
[Push a main]
     │
     ▼
[snyk-scan]──── FALLA ──► Pipeline bloqueado
     │
     ▼
[test + JaCoCo]──── FALLA ──► sonar, build, deploy no corren
     │
     ▼
[sonar-analysis]──── FALLA ──► build, deploy no corren
     │
     ▼
[build]
     │
     ▼
[Deploy] (solo en push a main)
```

---

## Etapas del Pipeline

**Job 1 — snyk-scan:** Escanea las dependencias Maven en busca de vulnerabilidades. Si detecta alguna de severidad HIGH o CRITICAL, el pipeline se detiene completamente. Genera un reporte JSON descargable.

**Job 2 — test:** Compila el proyecto, ejecuta todos los tests unitarios y de integración, y genera el reporte de cobertura con JaCoCo. Requiere al menos 80% de cobertura de líneas para pasar.

**Job 3 — sonar-analysis:** Envía el código y el reporte JaCoCo a SonarCloud para análisis estático de calidad: bugs, code smells, duplicación y vulnerabilidades en el propio código.

**Job 4 — build:** Construye la imagen Docker usando el Dockerfile multi-stage. Etiqueta la imagen con el SHA del commit para trazabilidad.

**Job 5 — deploy:** Solo se ejecuta en push a `main`. Levanta la app con Docker Compose, verifica que responde en `/actuator/health` y ejecuta smoke tests básicos.

---

## Trazabilidad

- Cada commit en `main` dispara el pipeline automáticamente.
- La imagen Docker se etiqueta con `${{ github.sha }}` (hash único del commit).
- Los artefactos generados (JaCoCo, Snyk, logs) quedan vinculados al run ID en GitHub Actions.
- El historial de ejecuciones en la pestaña **Actions** permite rastrear qué commit causó cada falla o éxito.

---

## Calidad

- **Pruebas unitarias**: JUnit 5 + MockMvc para controladores y lógica de negocio.
- **Cobertura mínima**: 80% de líneas cubiertas (JaCoCo falla el build si no se alcanza).
- **Análisis estático**: SonarCloud evalúa bugs, vulnerabilidades y code smells en cada push.
- **Dependencias**: Dependabot revisa actualizaciones de dependencias Maven y GitHub Actions diariamente.

---

## Seguridad

- **Snyk**: Escanea dependencias contra la base de datos CVE. Bloquea el pipeline si detecta vulnerabilidades HIGH.
- **Flujo de bloqueo**: `snyk-scan` falla → `test` no se ejecuta → `sonar-analysis` no se ejecuta → `deploy` no se ejecuta.
- **Docker**: El contenedor no corre como root (`USER appuser`), reduciendo la superficie de ataque.
- **Branch protection**: Configurar en Settings → Branches → Require status checks para bloquear merges si el pipeline falla.

---

## Orquestación con Docker Compose

Docker Compose orquesta 2 servicios:

**`app` — saludo-service (Spring Boot)**
- Puerto: 8080
- Límite: 512MB RAM, 1 CPU
- Reserva: 256MB RAM, 0.5 CPU
- Health check: `GET /actuator/health`

**`nginx` — Proxy inverso**
- Puerto: 80 → redirige a app:8080
- Límite: 128MB RAM, 0.5 CPU
- Solo arranca cuando `app` está healthy (`depends_on: condition: service_healthy`)

Ambos servicios comparten la red interna `backend` de tipo bridge.

---

## Cómo ejecutar localmente

**Con Docker Compose (recomendado):**
```bash
docker compose up --build
# La app responde en http://localhost (nginx) y http://localhost:8080 (directo)
```

**Con Maven:**
```bash
./mvnw spring-boot:run
# La app responde en http://localhost:8080
```

**Ejecutar solo los tests:**
```bash
./mvnw clean verify
# El reporte JaCoCo queda en target/site/jacoco/index.html
```

---

## Secrets requeridos en GitHub

Configurar en *Settings → Secrets and variables → Actions*:

| Secret | Descripción |
|---|---|
| `SNYK_TOKEN` | Token de autenticación de snyk.io |
| `SONAR_TOKEN` | Token de autenticación de sonarcloud.io |

Variables de repositorio (**Settings → Variables**):

| Variable | Ejemplo |
|---|---|
| `SONAR_PROJECT_KEY` | `mi-org_saludo-service` |
| `SONAR_ORGANIZATION` | `mi-org` |

---

## Uso de IA

- **Claude (Anthropic)**: Apoyo en la estructuración del scaffolding inicial del microservicio (estructura y packaging del proyecto, Dependencias del POM y estructura del README).
- Todo el código fue revisado, entendido y adaptado manualmente por los integrantes del equipo.

---
