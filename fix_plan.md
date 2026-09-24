# Plan napraw - ShoppingListService

**Data utworzenia:** 2026-09-23
**Stack:** Spring Boot 3.4.5, Java 21, MariaDB, Spring WebSocket, custom JSON protocol (null-byte framed)

---

## PRIORYTET 1: Bezpieczenstwo (KRYTYCZNE)

### 1.1. Token w CONNECT message / Authorization header zamiast query param

**Problem:** Token przesyany jako query param (`/ws?token=...`) pojawia sie w server logs, proxy logs.
**Lokalizacja:** `src/main/java/pl/kamjer/shoppinglistservice/config/security/UriTokenFilter.java`, `config/websocket/WebSocketHandler.java`

**Kontekst:** Frontend (ShoppingListWeb + ShoppingList) przenosi access token z localStorage do in-memory storage. Poniewaz token bedzie tylko w pamieci, nie moze juz byc latwo odczytany z URL (localStorage nie zapisuje URL). Backend musi zaakceptowac token z innej metode — lub z `Authorization` header (Android), lub z `CONNECT` message (Angular, bo browser WebSocket API nie obsluguje custom headers w handshake).

**Plan:**

Krok 1: `UriTokenFilter.java` — obsluga tokenu z Authorization header (dla Androida)
- Obecnie: `request.getParameter("token")` — tylko query param
- Nowe: sprawdzac kolejno:
  1. `Authorization` header (dla przyszlej migracji Androida)
  2. `token` query param (legacy fallback, do usuniecia pozniej)
- Dodac logger logujacy metode uwierzytelnienia

Krok 2: `WebSocketHandler.java:33-36` — pobieranie tokenu z SecurityContextHolder zamiast URI
- Obecnie: `session.getUri().getQueryParams().getFirst("token")`
- Nowe: token z `SecurityContextHolder.getContext().getAuthentication().getCredentials()`
- Zapisac token w atrybutach sesji z auth zamiast z URI

Krok 3: `WebsocketCustomService.java` — pobieranie tokenu z auth
- Obecnie: czyta token z `session.getAttributes().get("TOKEN")`
- Nowe: token z `SecurityContextHolder.getContext().getAuthentication()`
- Fallback do session attributes na czas migracji

**Testy do napisania:**
- `UriTokenFilterTest.java` — query param (legacy), Authorization header (nowy), brak tokenu
- `WebSocketHandlerTest.java` — pobieranie tokenu z SecurityContextHolder

---

### 1.2. Maskowanie token z logow — ZAOBSERWOWANE, nie w scope (konfiguracja infrastruktury Nginx/HAProxy)

**UWAGA:** Maskowanie token z access logs to zmiana po stronie infrastruktury (Nginx, HAProxy, Cloudflare). Nie jest to zmiana w kodzie Java.

---

## PRIORYTET 2: Jakosc kodu

### 2.1. Username enumeration via BadCredentialsException

**Problem:** `BadCredentialsException` uzyty dla "user not found" — leakuje czy user istnieje.
**Lokalizacja:** `service/websocketservice/WebsocketCustomService.java` (linia ~68,74,81)

**Plan:**

Krok 1: Stworzyc custom exception `AuthenticationFailedException`
- Message: "Authentication failed" (uniwersalny, nie leakuje user existence)

Krok 2: Zmienic `BadCredentialsException` na nowy exception w WebsocketCustomService

---

### 2.2. Eager loading UserDetails na kazdy request

**Problem:** `JwtAuthFilter` w SecurityService laduje UserDetails za pomoca `UserDetailsService.loadUserByUsername()` na KA-ZDY request HTTPS (nie tylko WS).
**Lokalizacja:** `config/security/JwtAuthFilter.java:44`

**Plan:**

Krok 1: Dodac cache do `loadUserByUsername()` w `CustomUserDetailsService`
- Uzyc Spring `@Cacheable` lub manualny cache (Caffeine/Guava)
- TTL: 5-10 minut

Krok 2: Dodac invalidation cache przy zmianie username/roli

---

### 2.3. `ResponseStatusException` mixed z custom exceptions

**Problem:** `UserService.java:141-142` rzucaca `ResponseStatusException` zamiast custom exception — bypassuje `@ControllerAdvice`.
**Lokalizacja:** `service/UserService.java:141-142`

**Plan:**

Krok 1: Stworzyc custom exception `UserAlreadyExistsException` / `UserNotFound`
- Zmienic `ResponseStatusException` na custom exception
- Upewnic sie ze `@ControllerAdvice` obsluguje nowy exception

---

### 2.4. `Topic.equals()` bug (tylko hashCode compare)

**Problem:** `Topic.equals()` porownuje przez `hashCode()` a nie faktyczne pola — rozne obiekty moga byc "rowne".
**Lokalizacja:** `config/websocket/Topic.java`

**Plan:**

Krok 1: Zmienic `equals()` na porownanie wszystkich pol (dest, para)
- Usunac override hashCode() lub zrobic spójny z equals()
- Dodac testy

---

## PRIORYTET 3: Testy

### 3.1. Testy WebSocket

**Plan:**
- `UriTokenFilterTest.java` — token z query param, token z header, brak tokenu, invalid token
- `WebSocketHandlerTest.java` — afterConnectionEstablished, session attributes, token storage
- `ConnectionBrokerTest.java` — CONNECT/SUBSCRIBE/MESSAGE/UNSUBSCRIBE/ERROR commands
- `WebsocketCustomServiceTest.java` — getUserFromAuth, CRUD operations

### 3.2. Testy Security + Token

**Plan:**
- `JwtAuthenticationProviderTest.java` — valid/invalid/expired token
- `JwtAuthFilterTest.java` — token z header, token z cookie, 401 handling
- `WebSecurityConfigurationTest.java` — endpoint access control

### 3.3. Testy UserService

**Plan:**
- `UserServiceTest.java` — create, update, delete, changeRole, changePassword
- `UserServiceTest.java` — self-modification prevention, SUPER_ADMIN protection

### 3.4. Testy REST controllers

**Plan:**
- `CategoryControllerTest.java` — CRUD endpoints
- `ShoppingItemControllerTest.java` — CRUD endpoints
- `AmountTypeControllerTest.java` — CRUD endpoints

---

## PRIORYTET 4: Wydajnosc

### 4.1. Dodać index na refresh_token.user_name

**Problem:** `revokeAllForUser` query to full table scan bez index.
**Lokalizacja:** `db/migration/V2__add_refresh_tokens.sql`

**Plan:**
- Dodac migration: `V3__add_refresh_token_user_index.sql`
- `CREATE INDEX idx_refresh_token_user ON refresh_token(user_name)`

### 4.2. Pagination w getAllUsers()

**Problem:** `findAll()` bez pagination — degradacja z liczba userow.
**Lokalizacja:** `service/UserService.java:86`

**Plan:**
- Dodac Pageable do `getAllUsers()`
- Zmienic na `Page<UserInfo>`

### 4.3. N+1 queries z @Data na Topic

**Problem:** `@Data` na Topic generuje equals/hashCode ktore iteruja przez wszystkie pola + potencjalnie lazy load.
**Lokalizacja:** `config/websocket/Topic.java`

**Plan:**
- Sprawdzic czy Topic ma jakiekolwiek lazy-loaded associations
- Jezeli nie — problem w equals/hashCode (patrz 2.4)
- Jezeli tak — zmienic fetch type na EAGER lub dodac index

---

## PRIORYTET 5: Cleanup

### 5.1. Usunac unnecessary configuration

**Plan:**
- `application.properties` — usunac `spring.jpa.database-platform` (Hibernate auto-detects)
- `pom.xml` — usunac explicit `hibernate-core` dependency (Spring Boot manages)
