# Shopping List Application Service

## Overview

Shopping List Service is a Spring Boot server application designed for managing shopping lists. It supports user authentication, product category storage, and data synchronization between clients via WebSocket. Additionally, the application includes mechanisms for automatically cleaning up old data and error logging.

## Technologies

- Java 17+

- Spring Boot (Security, Data JPA, Scheduling, WebSockets)

- MariaDB as the database

- Hibernate as ORM

- Lombok for reducing boilerplate code

- Log4J2 for logging

## Features

- User authentication and registration

- Spring Security for endpoint protection

- Shopping list management: Adding, deleting, and editing

- Handling categories and quantity units

- Automatic deletion of purchased products older than 1 month

- WebSocket communication for real-time updates

- Error logging, diagnostics, and global exception handling

## Installation and Configuration

### Cloning the repository:

    git clone https://github.com/KamJer/shopping-list.git  
    cd shopping-list  

### Database Configuration (MariaDB):

After adding the appropriate repository to the pom file, installing the server on your machine, and creating the database, configure the server settings in `application.properties`:

    spring.datasource.url=jdbc:MariaDB://localhost:3306/shopping_list_db  
    spring.datasource.username=your_username  
    spring.datasource.password=your_password  
    spring.jpa.hibernate.ddl-auto=update  

### Creating a Certificate:

Create a certificate and specify it in `application.properties`:

    server.ssl.key-store=your_file  
    server.ssl.key-store-type=PKCS12  
    server.ssl.key-alias=your_alias  

### Building and Running the Application:

    mvn clean package  
    java -jar target/shopping-list-service.jar  

## Usage

The application exposes three endpoints:
- **/user** - a REST API endpoint handling the following methods:
  - `PUT` for adding new users. Currently, the application does not allow deleting or modifying existing users.
- **/exception** - a REST API URL allowing clients to send their errors for diagnostic purposes.
- **/ws** - a WebSocket URL for establishing bidirectional communication between the client and the service. The application uses a custom communication protocol based on STOMP but in a simplified form.  