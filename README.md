# ShoppingListService

Spring Boot backend for shopping list management with WebSocket-first data synchronization.
Part of a microservice ecosystem including an auth service, recipe service, Android app, and web frontend.

## Features

- **JWT authentication** – token passed as `token` query parameter on HTTP and WebSocket connections; validation delegated to ShoppingSecService
- **Full-data WebSocket sync** – bidirectional synchronization with dirty-flag conflict detection
- **CRUD operations** – create, update, and soft-delete shopping items, categories, and amount types via WebSocket
- **Cascade deletes** – deleting an amount type or category soft-deletes all associated shopping items
- **HTTP exception logging** – clients submit structured error payloads to `POST /exception` for server-side logging
- **Scheduled cleanup** – daily soft-deletion of bought items older than one month (requires `@EnableScheduling`)
- **MapStruct mapping** – entity↔DTO conversion with automatic ID resolution and reference handling
- **Virtual threads** – Java 21 virtual threads enabled

## Architecture

Layered Spring Boot application.

| Layer | Technology |
|-------|-----------|
| Web / WebSocket | Spring Web, Spring WebSocket (custom framed protocol) |
| Security | Spring Security (stateless JWT via query parameter) |
| Persistence | Spring Data JPA / Hibernate, MariaDB |
| Migrations | Flyway (`classpath:db/migration`) |
| Mapping | MapStruct + Lombok |
| Auth delegation | RestClient → ShoppingSecService (`user.service.base-url`) |
| Scheduling | `@Scheduled` (requires manual `@EnableScheduling`) |
| Build | Maven 3.8+, `spring-boot-maven-plugin` |

## Ecosystem (microservices)

The service communicates with the auth microservice and pairs with the Android app and web frontend:

```
                    ┌─────────────────────┐
                    │  ShoppingSecService  │
                    │  (auth, port 4443)   │
                    └──────────┬──────────┘
                               │ REST
          ┌────────────────────┼────────────────────┐
          │                    │                    │
          ▼                    ▼                    ▼
 ┌────────────────┐ ┌──────────────────┐ ┌──────────────────┐
 │ShoppingListWeb │ │ShoppingList      │ │ShoppingListService│
 │(Angular 21 SPA)│ │(Android app)     │ │(this service)    │
 │                │ │                  │ │port 5443         │
 └────────────────┘ └──────────────────┘ └──────────────────┘
          │                    │
          │                    └────────REST────────┐
          │                                         │
          │                              ┌──────────┴──────────┐
          └────────REST──────────────────│ShoppingListRecipes  │
                                         │Service (port 6443)  │
                                         └─────────────────────┘
```

## Requirements

- **Java 21**
- **Maven 3.8+**
- **MariaDB** with database `shopping_list_db`
- Running [**ShoppingSecService**](https://github.com/KamJer/Shopping-security-service) (or compatible deployment; default: `http://localhost:4443/user`)

## Stack

| Area | Technology |
|------|------------|
| Runtime | Java 21, Spring Boot 3.4.5 |
| Web | Spring Web, Spring Security (stateless JWT), Spring WebSocket |
| Data | Spring Data JPA, Hibernate, MariaDB, Flyway |
| Mapping | MapStruct 1.6.3, Lombok |
| HTTP client | RestClient (to ShoppingSecService) |
| Serialization | Jackson (JSON) |
| Scheduling | `@Scheduled` (cron) |
| Threading | Virtual threads (`spring.threads.virtual.enabled=true`) |
| Logging | Logback (console + rolling file) |
| Build | Maven, `spring-boot-maven-plugin` |

## Configuration

Main defaults in `src/main/resources/application.properties`:

- **Server port:** `5443`
- **Datasource:** `jdbc:mariadb://localhost:3306/shopping_list_db` — credentials via `application-secret-*.properties` or environment variables
- **Schema:** managed by Flyway (`spring.flyway.enabled=true`); do not rely on `ddl-auto` in production
- **User service:** `user.service.base-url=http://localhost:4443/user` — token validation and saved-time updates
- **Flyway repair:** set `app.flyway.repair-before-migrate=true` for one startup to recover from checksum mismatch (then revert to `false`)
- **Profiles:** `dev` / `prod` with corresponding `application-secret-*.properties` for credentials

## Build and run

```bash
mvn clean package
java -jar target/ShoppingListService-3.0.jar
```

Use `--spring.profiles.active=dev` or `=prod` to select a profile.

## HTTP surface

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/exception` | JWT (`token` query param) | Accepts `ExceptionDto` JSON; logs server-side with user label |
| GET | `/util/message` | None (permitAll) | Placeholder – no controller implementation |
| * | `/user/log/*` | JWT (stripped) | Legacy path – `SkipAuthorizationFilter` strips `Authorization` header |

There is no `/user` REST API in this service; user operations go through [ShoppingSecService](https://github.com/KamJer/Shopping-security-service).

## WebSocket

### Connection

- **Endpoint:** `ws://<host>:5443/ws?token=<JWT>` (or `wss://` behind TLS)
- **Allowed origins:** configured via `setAllowedOriginPatterns` (wildcard by default)
- **Supported origin patterns:** `*` (tighten for production)

### Protocol

Custom framed JSON protocol (not STOMP). Messages are JSON objects delimited by a null byte (`\0`). Partial message reassembly is supported.

**Commands:** `CONNECT`, `CONNECTED`, `MESSAGE`, `SUBSCRIBE`, `SUBSCRIBED`, `UNSUBSCRIBE`, `UNSUBSCRIBED`, `ERROR`

**Headers:** `ID`, `DEST`, `BODY`, `PARA`, `AUTH`

**Example flow:**

```
client → {"command":"SUBSCRIBE","headers":{"DEST":"/synchronizeData"}}
server → {"command":"SUBSCRIBED","headers":{"DEST":"/synchronizeData"}}
client → {"command":"MESSAGE","headers":{"ID":"1","DEST":"/synchronizeData","BODY":"...json..."}}
server → {"command":"MESSAGE","headers":{"DEST":"/synchronizeData","BODY":"...json...","ID":"1"}}
```

### Topics

| Topic | Direction | Description |
|-------|-----------|-------------|
| `/synchronizeData` | bidirectional | Full data sync with dirty-flag conflict detection |
| `/{userName}/pip` | server → client | Push notification when data has changed |
| `/{userName}/putAmountType` | client → server | Create a new amount type |
| `/{userName}/postAmountType` | client → server | Update an existing amount type |
| `/{userName}/deleteAmountType` | client → server | Soft-delete an amount type (cascades to items) |
| `/{userName}/putCategory` | client → server | Create a new category |
| `/{userName}/postCategory` | client → server | Update an existing category |
| `/{userName}/deleteCategory` | client → server | Soft-delete a category (cascades to items) |
| `/{userName}/putShoppingItem` | client → server | Create a new shopping item |
| `/{userName}/postShoppingItem` | client → server | Update an existing shopping item |
| `/{userName}/deleteShoppingItem` | client → server | Soft-delete a shopping item |

### Synchronization algorithm

1. Client sends `AllDto` with its current `savedTime` and entity lists (each with `ModifyState`: `INSERT`, `UPDATE`, `DELETE`)
2. Server compares the client's `savedTime` with the server's latest data
3. If the server has newer data → responds with `dirty=true` and the full server dataset for the client to reapply
4. If not dirty → applies all changes, persists to database, returns only entities with `savedTime > client's savedTime` (incremental delta)
5. Updates the user's `savedTime` via ShoppingSecService

## Database

Managed by Flyway migration `V1__create_tables.sql`.

| Table | Key columns |
|-------|-------------|
| `amount_type` | `amount_type_id` PK, `user_name`, `type_name`, `saved_time`, `deleted` |
| `category` | `category_id` PK, `user_name`, `category_name`, `saved_time`, `deleted` |
| `shopping_item` | `shopping_item_id` PK, `user_name`, `item_name`, `amount`, `bought`, `amount_type_id` (FK), `category_id` (FK), `saved_time`, `deleted` |

All foreign key relationships are JPA-only (no database-level constraints).

## Scheduled job

- **`ScheduledJob.deleteOldData()`** runs daily at 12:00 server time
- Soft-deletes (`deleted = true`) shopping items where `bought = true` and `savedTime` is older than one month
- **Note:** `@EnableScheduling` is not present on the application class — the cron does not run unless added

## Tests

```bash
mvn test
```

Uses H2 in-memory database (not MariaDB). 6 test classes with JUnit 5 + Mockito:

| Test class | Scope |
|------------|-------|
| `WebSocketAmountTypeServiceTest` | CRUD operations for amount types |
| `WebSocketCategoryServiceTest` | CRUD operations for categories |
| `WebSocketShoppingItemServiceTest` | CRUD operations for shopping items |
| `WebSocketUtilServiceSyncEntitiesTest` | Synchronization entity logic |
| `ShoppingEntityMapperTest` | MapStruct entity↔DTO mappings |
| `WebSocketDataHolderTest` | WebSocket topic registration and subscription |

## Project directories

| Directory | Description |
|-----------|-------------|
| `sql/` | Helper scripts (database reset, select queries) |
| `keystore_old/` | Legacy TLS keystores (unused) |
| `logs/` | Runtime log output |

## Related repositories

### Client applications

| Repository | Description |
|------------|-------------|
| [**Shopping-list-web**](https://github.com/KamJer/Shopping-list-web) | Web client (Angular SPA) |
| [**Shopping-List-Client**](https://github.com/KamJer/Shopping-List-Client) | Android application |

### Other backend services

| Repository | Description |
|------------|-------------|
| [**Shopping-security-service**](https://github.com/KamJer/Shopping-security-service) | Auth microservice: JWT validation, user management |
| [**Shopping-list-recipes-service**](https://github.com/KamJer/Shopping-list-recipes-service) | Recipe microservice |

---

## Privacy Policy

Detailed information about data processing can be found here:
[Privacy Policy](PRIVACY_POLICY.md)

## Account Deletion

If you want to delete your account and associated data, follow the instructions here:
[Account Deletion](ACCOUNT_DELETION.md)

## Contact

For questions or concerns: [kamjersoft@gmail.com](mailto:kamjersoft@gmail.com)
