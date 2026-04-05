# Shopping List Service

## Overview

Spring Boot backend for shopping lists: categories, quantity units (amount types), items, and **full-data sync** over **WebSocket**. HTTP API is minimal (exceptions, utilities). **User accounts and JWT validation** are delegated to a separate **user service** (`SecClient` → `user.service.base-url`).

## Requirements

- **Java 21**
- **Maven 3.8+**
- **MariaDB** with database `shopping_list_db` (or adjust URLs in configuration)
- Running **user/auth service** compatible with this app’s `SecClient` (default base URL in properties: `http://localhost:4443/user`)

## Stack

| Area | Technology |
|------|------------|
| Runtime | Java 21, Spring Boot **3.4.x** |
| Web | Spring Web, Spring Security (stateless JWT), Spring WebSocket (custom protocol, not STOMP) |
| Data | Spring Data JPA, Hibernate, **MariaDB**, **Flyway** (`classpath:db/migration`) |
| Mapping | **MapStruct** (+ Lombok) |
| Other | Lombok, virtual threads enabled (`spring.threads.virtual.enabled`), Spring Boot Actuator (endpoints mostly disabled by default) |

Logging uses Spring Boot’s default setup; log file path is configured via `logging.file.name` (e.g. under `logs/`).

## Features

- JWT authentication: token passed as **`token` query parameter** on HTTP requests (`UriTokenFilter`).
- CRUD-style operations for shopping data primarily via **WebSocket** destinations (see below).
- **Bulk sync** endpoint `/synchronizeData` with conflict detection (`dirty` flag) to refresh client state from the server.
- **POST `/exception`**: clients can submit structured error payloads for server-side logging.
- **Scheduled job** (`ScheduledJob`): daily at 12:00 (server time), **bought** items with `savedTime` older than **one month** are soft-deleted (`deleted = true`). *Note: `@EnableScheduling` must be enabled on the application for this cron to run.*

## Configuration

Main defaults live in `src/main/resources/application.properties`:

- **Server port:** `5443` (HTTPS/TLS termination may be handled externally).
- **Datasource:** `jdbc:mariadb://localhost:3306/shopping_list_db` — set `spring.datasource.username` / `spring.datasource.password` (often via `application-secret-dev.properties` / `application-secret-prod.properties` or env).
- **Schema:** managed by **Flyway**; do not rely on `ddl-auto` for production. Migrations are under `src/main/resources/db/migration/`.
- **User service:** `user.service.base-url` (e.g. `http://localhost:4443/user`) — used for token validation and user/saved-time updates.

Optional profiles: `application-dev.properties`, `application-prod.properties`.

## Build and run

```bash
mvn clean package
java -jar target/ShoppingListService-3.0.jar
```

Adjust the JAR name if `<version>` in `pom.xml` changes.

## HTTP surface

| Path | Notes |
|------|--------|
| **POST** `/exception` | Body: `ExceptionDto` JSON; authenticated user label is logged when available. |
| **`/util/message`** | Permitted without auth (see `WebSecurityConfiguration`). |
| **Other HTTP routes** | Typically require a valid JWT in the **`token` query parameter**. |

There is **no** `/user` REST API in *this* repository; user operations go through the configured **external user service**.

## WebSocket

- **URL:** `ws://<host>:5443/ws` (or `wss://` behind TLS). Clients usually pass the JWT as a **query parameter** `token` (stored on the session server-side).
- **Allowed origins:** configured in `WebSocketConfig` via `setAllowedOriginPatterns` (tighten for production if needed).
- **Registered topics** (examples): `/synchronizeData`, `/{userName}/pip`, `/{userName}/putAmountType`, `/{userName}/postAmountType`, `/{userName}/deleteAmountType`, and analogous paths for **category** and **shopping item** operations.

Messaging uses a **custom** framed protocol (see `WebSocketHandler`, `ConnectionBroker`, `BeanInspector`), not Spring STOMP.

## Cloning

```bash
git clone https://github.com/KamJer/shopping-list.git
cd shopping-list
```

*(If your local folder name differs, e.g. `ShoppingListService`, use that directory after clone.)*

## Tests

```bash
mvn test
```
