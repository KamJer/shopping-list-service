# Shopping List Application Service

## Overview

Shopping List Service to aplikacja serwerowa napisana w Spring Boot, służąca do zarządzania listami zakupów. Obsługuje uwierzytelnianie użytkowników, przechowywanie kategorii produktów oraz synchronizację danych między klientami poprzez WebSocket. Dodatkowo aplikacja posiada mechanizmy automatycznego czyszczenia starych danych oraz obsługę logowania błędów.


## Technologie

Java 17+

Spring Boot (Security, Data JPA, Scheduling, WebSockets)

MariaDB jako baza danych

Hibernate jako ORM

Lombok do redukcji boilerplate'u w kodzie

Log4J2 do logowania

## Funkcjonalności

- Autoryzacja i rejestracja użytkowników

- Spring Security do ochrony endpointów

- Zarządzanie listami zakupów

- Dodawanie, usuwanie i edycja zakupów

- Obsługa kategorii i jednostek ilościowych

- Automatyczne usuwanie zakupionych produktów starszych niż 1 miesiąc

- Komunikacja WebSocket w celu osiągnięcia komunikacji w czasie rzeczywistym

- Logowanie błędów i diagnostyka

- Obsługa wyjątków globalnych

## Instalacja i konfiguracja

### Klonowanie repozytorium:

git clone https://github.com/user/shopping-list-service.git
cd shopping-list-service

### Konfiguracja bazy danych (MariaDB):

spring.datasource.url=jdbc:MariaDB://localhost:3306/shopping_list_db
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update

### Tworzenie certyfikatu


### Budowanie i uruchamianie aplikacji:

mvn clean package
java -jar target/shopping-list-service.jar

