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

All configuration is in `src/main/resources/application.properties`. Sensitive values are set via environment variables:

| Variable | Description |
|----------|-------------|
| `DB_USERNAME` | MariaDB username |
| `DB_PASSWORD` | MariaDB password |

### Defaults

| Setting | Default | Notes |
|---------|---------|-------|
| **Server port** | `5443` | |
| **Datasource** | `jdbc:mariadb://localhost:3306/shopping_list_db` | |
| **User service** | `http://localhost:4443/user` | Token validation and saved-time updates |
| **Flyway repair** | set `app.flyway.repair-before-migrate=true` for one startup to recover from checksum mismatch (then revert to `false`) |

- **Profiles:** `dev` / `prod` with corresponding `application-dev.properties` / `application-prod.properties`
- **Schema:** managed by Flyway (`spring.flyway.enabled=true`); do not rely on `ddl-auto` in production

## Build and run

You need **Java 21**, **Maven 3.8+**, and a running **MariaDB** instance with database `shopping_list_db`.

```bash
# Set required environment variables
export DB_USERNAME=your_db_user
export DB_PASSWORD=your_db_password

# Build
mvn clean package

# Run with dev profile
java -jar target/ShoppingListService-3.0.jar --spring.profiles.active=dev

# Run with prod profile
java -jar target/ShoppingListService-3.0.jar --spring.profiles.active=prod
```

## REST API

This service exposes minimal REST endpoints. Most operations go through WebSocket.

| Method | Path | Auth | Request body | Response | Description |
|--------|------|------|-------------|----------|-------------|
| POST | `/exception` | JWT (`token` query param) | `ExceptionDto` JSON | `200 OK` | Logs client-side exceptions server‑side with user label |

**`ExceptionDto` schema:**
```json
{
  "message": "string — exception message",
  "stackTrace": ["string — stack trace elements"]
}
```

> All user management and token operations go through [ShoppingSecService](https://github.com/KamJer/Shopping-security-service).  
> Legacy path `/user/log/*` is handled by `SkipAuthorizationFilter` which strips the `Authorization` header.

---

## WebSocket API

This is the primary interface of the service. All CRUD and synchronisation is done over a custom framed JSON WebSocket protocol.

### 1. Connection

```
ws[s]://<host>:5443/ws?token=<JWT>
```

The JWT access token is passed as a query parameter. The token is validated by delegating to [ShoppingSecService](https://github.com/KamJer/Shopping-security-service).

> **Origin check:** configured via `setAllowedOriginPatterns` — wildcard (`*`) by default. Tighten for production.

### 2. Protocol

Messages are **JSON objects delimited by a null byte** (`\0`). Each message has the same envelope:

```json
{"command":"<COMMAND>","headers":{"<KEY>":"<VALUE>",...}}
```

#### Commands

| Command | Direction | Description |
|---------|-----------|-------------|
| `CONNECT` | client → server | Open a logical session |
| `CONNECTED` | server → client | Session acknowledged |
| `SUBSCRIBE` | client → server | Subscribe to a topic |
| `SUBSCRIBED` | server → client | Subscription confirmed |
| `MESSAGE` | bidirectional | Payload on a topic |
| `UNSUBSCRIBE` | client → server | Unsubscribe from a topic |
| `UNSUBSCRIBED` | server → client | Unsubscription confirmed |
| `ERROR` | server → client | Error notification |

#### Headers

| Header | Description |
|--------|-------------|
| `ID` | Unique message identifier (echoed back by the server) |
| `DEST` | Topic name (e.g. `/synchronizeData`) |
| `BODY` | JSON payload of the message |
| `PARA` | Optional parameter |
| `AUTH` | Authentication data |

### 3. Full connection & sync flow

```
── Step 1: Connect ─────────────────────────────────────────────
client → {"command":"CONNECT"}
server → {"command":"CONNECTED","headers":{}}

── Step 2: Subscribe to synchronisation topic ─────────────────
client → {"command":"SUBSCRIBE","headers":{"DEST":"/synchronizeData"}}
server → {"command":"SUBSCRIBED","headers":{"DEST":"/synchronizeData"}}

── Step 3: Subscribe to push notifications ────────────────────
client → {"command":"SUBSCRIBE","headers":{"DEST":"/{userName}/pip"}}
server → {"command":"SUBSCRIBED","headers":{"DEST":"/{userName}/pip"}}

── Step 4: Send / receive data via /synchronizeData ────────────
client → {
  "command":"MESSAGE",
  "headers":{
    "ID":"1",
    "DEST":"/synchronizeData",
    "BODY":"{\"amountTypeDtoList\":[...],\"categoryDtoList\":[...],\"shoppingItemDtoList\":[...],\"savedTime\":\"2025-06-07T12:00:00\",\"dirty\":false}"
  }
}
server → {
  "command":"MESSAGE",
  "headers":{
    "ID":"1",
    "DEST":"/synchronizeData",
    "BODY":"{\"amountTypeDtoList\":[...],\"categoryDtoList\":[...],\"shoppingItemDtoList\":[...],\"savedTime\":\"2025-06-07T12:00:05\",\"dirty\":false}"
  }
}

── Step 5: Server pushes a change notification ────────────────
server → {
  "command":"MESSAGE",
  "headers":{"DEST":"/{userName}/pip","BODY":""}
}
```

### 4. Topics

| Topic | Direction | Description |
|-------|-----------|-------------|
| `/synchronizeData` | bidirectional | Full bidirectional sync with dirty‑flag conflict detection |
| `/{userName}/pip` | server → client | Push notification — triggers the client to pull fresh data |
| `/{userName}/putAmountType` | client → server | Create a new amount type |
| `/{userName}/postAmountType` | client → server | Update an existing amount type |
| `/{userName}/deleteAmountType` | client → server | Soft‑delete an amount type (cascades to items) |
| `/{userName}/putCategory` | client → server | Create a new category |
| `/{userName}/postCategory` | client → server | Update an existing category |
| `/{userName}/deleteCategory` | client → server | Soft‑delete a category (cascades to items) |
| `/{userName}/putShoppingItem` | client → server | Create a new shopping item |
| `/{userName}/postShoppingItem` | client → server | Update an existing shopping item |
| `/{userName}/deleteShoppingItem` | client → server | Soft‑delete a shopping item |

All CRUD topics expect the BODY to contain the respective DTO (see schemas below).

### 5. Data types – JSON schemas

#### `AllDto` — used on `/synchronizeData`

```json
{
  "amountTypeDtoList": [ ... ],
  "categoryDtoList": [ ... ],
  "shoppingItemDtoList": [ ... ],
  "savedTime": "2025-06-07T12:00:00",
  "dirty": false
}
```

- `savedTime` — client’s last known timestamp (ISO‑8601). Server compares it with its own latest timestamp.
- `dirty` — `true` when the server has newer data; the client must re‑send its entire dataset.
- Lists contain their respective DTOs (may be empty).

#### `AmountTypeDto`

```json
{
  "amountTypeId": 1,
  "typeName": "kg",
  "deleted": false,
  "modifyState": "INSERT",
  "localId": 0,
  "savedTime": "2025-06-07T12:00:00"
}
```

| Field | Type | Description |
|-------|------|-------------|
| `amountTypeId` | number | Server‑assigned ID (0 for new entities) |
| `typeName` | string | Unit name (e.g. "szt.", "kg", "l") |
| `deleted` | boolean | Soft‑delete flag |
| `modifyState` | enum | `INSERT`, `UPDATE`, `DELETE`, `NONE` |
| `localId` | number | Client‑side temporary ID for matching responses |
| `savedTime` | string (ISO‑8601) | Timestamp set by the server |

#### `CategoryDto`

```json
{
  "categoryId": 1,
  "categoryName": "Owoce",
  "deleted": false,
  "modifyState": "INSERT",
  "localId": 0,
  "savedTime": "2025-06-07T12:00:00"
}
```

| Field | Type | Description |
|-------|------|-------------|
| `categoryId` | number | Server‑assigned ID (0 for new entities) |
| `categoryName` | string | Category name |
| `deleted` | boolean | Soft‑delete flag |
| `modifyState` | enum | `INSERT`, `UPDATE`, `DELETE`, `NONE` |
| `localId` | number | Client‑side temporary ID |
| `savedTime` | string (ISO‑8601) | Timestamp set by the server |

#### `ShoppingItemDto`

```json
{
  "shoppingItemId": 1,
  "itemAmountTypeId": 1,
  "itemCategoryId": 1,
  "itemName": "Jabłka",
  "amount": 2.5,
  "bought": false,
  "deleted": false,
  "modifyState": "INSERT",
  "localId": 0,
  "localAmountTypeId": 0,
  "localCategoryId": 0,
  "savedTime": "2025-06-07T12:00:00"
}
```

| Field | Type | Description |
|-------|------|-------------|
| `shoppingItemId` | number | Server‑assigned ID (0 for new entities) |
| `itemAmountTypeId` | number | FK to `amount_type.amountTypeId` |
| `itemCategoryId` | number | FK to `category.categoryId` |
| `itemName` | string | Item name |
| `amount` | number (nullable) | Quantity |
| `bought` | boolean | Purchased flag |
| `deleted` | boolean | Soft‑delete flag |
| `modifyState` | enum | `INSERT`, `UPDATE`, `DELETE`, `NONE` |
| `localId` | number | Client‑side temporary ID for the item |
| `localAmountTypeId` | number | Client‑side temp FK for amount type |
| `localCategoryId` | number | Client‑side temp FK for category |
| `savedTime` | string (ISO‑8601) | Timestamp set by the server |

### 6. ModifyState semantics

| Value | Meaning |
|-------|---------|
| `INSERT` | New entity to be created on the server |
| `UPDATE` | Existing entity to be updated |
| `DELETE` | Existing entity to be soft‑deleted (`deleted = true`) |
| `NONE` | No change (used in responses) |

### 7. Synchronisation algorithm (detailed)

1. **Client connects** and subscribes to `/synchronizeData` and `/{userName}/pip`.
2. **Client sends** an `AllDto` with its current `savedTime` and all entities (each with `modifyState` set).
3. **Server compares** the client’s `savedTime` with the latest modification timestamp in the database for that user.
4. **If server has newer data** (`dirty = true`): server responds with the **full dataset** and the client must re‑apply its local changes, then re‑send with updated `savedTime`.
5. **If not dirty**: server applies changes, persists to DB, and returns **only entities whose `savedTime > client's savedTime`** (incremental delta).
6. **Server updates** the user’s `savedTime` via `PUT /user/savedTime` on the ShoppingSecService.
7. When **another client** modifies data, the server pushes an **empty message on `/{userName}/pip`**, signalling the subscribed client to perform a full sync cycle.

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
