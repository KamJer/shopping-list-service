# Plan napraw - ShoppingListService

**Data utworzenia:** 2026-09-23
**Stack:** Spring Boot 3.4.5, Java 21, MariaDB, Spring WebSocket, custom JSON protocol (null-byte framed)

---

## PRIORYTET 1: Bezpieczenstwo (KRYTYCZNE)

### 1.1. Token w CONNECT message / Authorization header zamiast query param ✅ 2026-10-10

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

**Testy do napisania:** ✅ 2026-10-10
- ~~`UriTokenFilterTest.java` — query param (legacy), Authorization header (nowy), brak tokenu~~ ✅
- ~~`WebSocketHandlerTest.java` — pobieranie tokenu z SecurityContextHolder~~ ✅

**Zrealizowane ✅ 2026-10-10 — session opencode:**
- `UriTokenFilter` akceptuje oba kanały: najpierw `Authorization: Bearer`, fallback `?token=`; gdy oba obecne i różne → 401 "Conflicting authentication tokens".
- `WebSocketHandler.afterConnectionEstablished` pobiera token z `SecurityContextHolder` (details/credentials) z fallbackiem na URI query; zapisuje w atrybucie sesji `TOKEN`.
- Głowa `Authorization` NIE jest przenoszona do wiadomości CONNECT (zgodnie z decyzją w AGENTS.md).
- Utworzono `UriTokenFilterTest.java` (6 testów) + dodano test w `WebSocketHandlerTest` (token z SecurityContext).
- Weryfikacja `mvn test` przeszła ✅ 2026-11-19 — sesja ses_e0d52adaeffehzxo4UILCfoIJc.

---

### 1.2. Maskowanie token z logow — ZAOBSERWOWANE, nie w scope (konfiguracja infrastruktury Nginx/HAProxy)

**UWAGA:** Maskowanie token z access logs to zmiana po stronie infrastruktury (Nginx, HAProxy, Cloudflare). Nie jest to zmiana w kodzie Java.

---

### 1.3. Token z handshake znika przed `afterConnectionEstablished` ✅ 2026-11-20 — sesja ses_e0d52adaeffehzxo4UILCfoIJc

**Problem:** Po przejściu Androida na `Authorization: Bearer` handshake był uwierzytelniany (`Authenticated request via Authorization`, principal = nazwa użytkownika), ale `WebSocketHandler.afterConnectionEstablished` czytał token z `SecurityContextHolder`, który w tym momencie był już wyczyszczony. Fallback z `?token=` nie działał (Android zgodnie z decyzją nie wysyła tokenu w URL). Atrybut sesji `TOKEN` był pusty, więc `WebsocketCustomService.requireAuthenticatedUser()` rzucało `NoResourcesFoundException("User not authenticated")` przy pierwszych wiadomościach CRUD/synchronizacji.

**Plan:**

Krok 1: Dodać `WebSocketTokenHandshakeInterceptor`, który w `beforeHandshake` odczytuje token z nagłówka `Authorization` (preferowany) lub query param (fallback dla przeglądarki) i zapisuje go w atrybucie sesji `HANDSHAKE_TOKEN`; przy rozbieżnych tokenach zwraca 401 ✅ 2026-11-20 — sesja ses_e0d52adaeffehzxo4UILCfoIJc
Krok 2: Zarejestrować interceptor w `WebSocketConfig.registerWebSocketHandlers()` dla endpointu `/ws` ✅ 2026-11-20 — sesja ses_e0d52adaeffehzxo4UILCfoIJc
Krok 3: `WebSocketHandler.resolveToken()` czyta najpierw `HANDSHAKE_TOKEN`, potem `SecurityContextHolder`, potem URI; zapisuje wynik jako `TOKEN` ✅ 2026-11-20 — sesja ses_e0d52adaeffehzxo4UILCfoIJc
Krok 4: `WebsocketCustomService.getUserFromAuth()` korzysta ze stałej `TOKEN_ATTRIBUTE` i loguje ostrzeżenie o braku tokenu bez jego wartości ✅ 2026-11-20 — sesja ses_e0d52adaeffehzxo4UILCfoIJc
Krok 5: Dodać testy `WebSocketTokenHandshakeInterceptorTest` i test priorytetu atrybutu w `WebSocketHandlerTest` ✅ 2026-11-20 — sesja ses_e0d52adaeffehzxo4UILCfoIJc

**Zrealizowane ✅ 2026-11-20 — sesja ses_e0d52adaeffehzxo4UILCfoIJc:**
- `mvn test` przechodzi (bez błędów kompilacji i testów).
- Weryfikacja na emulatorze `emulator-5554` (bez czyszczenia danych): logowanie zapisanym użytkownikiem, logowanie hasłem oraz restart aplikacji (`force-stop` → `launch`) kończą się ekranem `ShoppingListActivity`, zielonym wskaźnikiem WS i brakiem toastu „User not authenticated”.
- Logi backendu po poprawce: `Authenticated request via Authorization`, `New session connected`, `/synchronizeData connected: User qa_reg_20261119_2232`, zero wpisów `User not authenticated` i zero `ERROR`.

---

### 1.4. Token w logach kontrolerów WebSocket ✅ 2026-11-20 — sesja ses_e0d52adaeffehzxo4UILCfoIJc

**Problem:** Kontrolery WS logowały `webSocketDataHolder.getCurrentSession().getPrincipal()`. Ponieważ `Authentication.details` zawiera surowy token, `toString()` obiektu Principal zapisywał token do `logs/application_logback.log`.

**Plan:**

Krok 1: Dodać `WebsocketCustomController.currentUserName()` zwracający tylko `Principal::getName` ✅ 2026-11-20 — sesja ses_e0d52adaeffehzxo4UILCfoIJc
Krok 2: Zastąpić wszystkie logi `getPrincipal()` wywołaniem `currentUserName()` w kontrolerach amount type, category, shopping item i util ✅ 2026-11-20 — sesja ses_e0d52adaeffehzxo4UILCfoIJc

**Zrealizowane ✅ 2026-11-20 — sesja ses_e0d52adaeffehzxo4UILCfoIJc:**
- Po restarcie backendu i ponownym logowaniu: `details_lines=0`, `auth_fail_lines=0`, `error_lines=0` w ostatnich 120 liniach logu; wpis `/synchronizeData connected: User qa_reg_20261119_2232` zawiera tylko nazwę użytkownika.

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

---

## Uwaga środowiskowa — konfiguracja IntelliJ ✅ 2026-11-19

Zgłoszony błąd `cannot find symbol: MessageValidator / WebSocketDataHolder / ConnectionBroker / WebsocketMessageDecryptor` nie wynikał z kodu źródłowego. `mvn clean compile` i `mvn clean test` przechodzą, a klasy istnieją w `config/websocket/` z poprawnym package.

**Przyczyna (potwierdzona w logach IDE `idea.log` / `build-log/build.log`):**
- IntelliJ wykonywał przyrostowy `Make` tylko 3 zmienionych plików po wyczyszczeniu katalogów wyjściowych — javac nie miał w classpath pozostałych klas modułu i zgłaszał 5 błędów `cannot find symbol`. Kolejny pełny compile dawał 0 błędów i aplikacja startowała.
- Dodatkowo `.idea/misc.xml` miał `languageLevel="JDK_26"` oraz `project-jdk-name="openjdk-21"`, ale globalna definicja SDK `openjdk-21` w `jdk.table.xml` wskazywała na `C:/Program Files/Java/jdk-25` (poprawny SDK `21` → `~/.jdks/ms-21.0.12` istnieje). IDE kompilowało więc javac 25, podczas gdy Maven celuje w release 21.

**Wykonane ✅ 2026-11-19 — sesja bieżąca:**
- `.idea/misc.xml`: `languageLevel="JDK_21"`, `project-jdk-name="21"` (zgodnie z `pom.xml` i faktycznym JDK 21).
- Source roots, exclude'y, annotation processing (Lombok 1.18.46 + MapStruct 1.6.3) i run configuration (`--spring.profiles.active=dev`, JRE `21`) — zweryfikowane, poprawne.
- Weryfikacja `mvn clean test` → PASS ✅ 2026-11-19.

Jeśli błąd pojawi się ponownie w IDE: `Build → Rebuild Project` (pełny compile), ewentualnie `File → Invalidate Caches…`. Najpewniejsze uruchomienie: `mvn clean spring-boot:run -Dspring-boot.run.profiles=dev`.
