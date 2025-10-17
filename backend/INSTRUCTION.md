# SolonTax G1 - Project Documentation

## Overview
SolonTax G1 is a Spring Boot application implementing a tax management system using **Hexagonal Architecture** (Ports and Adapters pattern). This is a pet project designed to practice Spring Framework, event-driven architecture with Kafka, and microservices deployment.

**Tech Stack:**
- Java 17
- Spring Boot 3.5.6
- PostgreSQL 13
- Apache Kafka
- Docker & Kubernetes
- Maven (Multi-module project)

---

## Architecture Overview

The project follows **Hexagonal Architecture** principles with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────┐
│                    Adapter Layer (HTTP)                      │
│              solontax-g1-management-adapter                  │
│              (REST Controllers, Configuration)               │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│                    Core/Domain Layer                         │
│              solontax-g1-management-core                     │
│         (Business Logic, Domain Models, Ports)               │
└────────────┬──────────────────────────┬─────────────────────┘
             │                          │
┌────────────▼─────────────┐  ┌────────▼─────────────────────┐
│   DAO Layer (Database)   │  │   Kafka Layer (Events)       │
│ solontax-g1-management-  │  │ solontax-g1-management-      │
│          dao             │  │         kafka                │
└──────────────────────────┘  └──────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│              Migration/Scheduler Service                     │
│         solontax-g1-management-migration                     │
│    (Background jobs, Event producers/consumers)              │
└─────────────────────────────────────────────────────────────┘
```

---

## Module Descriptions

### 1. **solontax-g1-management-core**
**Purpose:** Domain/Business Logic Layer

**Responsibilities:**
- Contains domain models (`Person`, `AuditLog`)
- Defines port interfaces (Repository contracts)
- Implements business services (`PersonService`)
- DTOs for data transfer
- Exception handling
- RSQL query filtering support

**Key Components:**
- `Person` domain model - represents a taxpayer with fields: id, firstName, lastName, dateOfBirth, taxNumber, taxDebt
- `PersonService` - handles person creation, search with RSQL filtering, and tax number lookups
- `PersonRepositoryPort` - interface for data persistence (implemented by DAO module)
- `KafkaTopics` constants - defines all Kafka topic names

**Dependencies:** Spring Boot Starter, Spring Web, Spring Data JPA, Spring Kafka, RSQL Parser

---

### 2. **solontax-g1-management-adapter**
**Purpose:** HTTP Adapter Layer (Main Application Entry Point)

**Responsibilities:**
- Exposes REST API endpoints
- HTTP request/response handling
- Application bootstrap and configuration
- Integration of all modules

**Key Components:**
- `SolontaxG1ManagementApplication` - Main Spring Boot application
- `PersonController` - REST endpoints for person management
- `InitialController` - Welcome endpoint

**Ports:**
- Default: 1945 (local)
- Docker: 1975
- Kubernetes NodePort: 30075

**Dependencies:** All other modules (core, dao, kafka)

---

### 3. **solontax-g1-management-dao**
**Purpose:** Data Access Layer

**Responsibilities:**
- JPA entity definitions
- Database repository implementations
- Implements ports defined in core module
- RSQL specification support for dynamic queries

**Key Components:**
- `PersonEntity` - JPA entity mapped to `persons` table
- `PersonJpaRepository` - Spring Data JPA repository
- `PersonJpaAdapter` - implements `PersonRepositoryPort` interface
- Supports pagination, sorting, and RSQL filtering

**Database Schema:**
- **Table:** `persons`
  - `id` (UUID, Primary Key)
  - `first_name` (String)
  - `last_name` (String)
  - `date_of_birth` (Date)
  - `tax_number` (Long, Unique, Not Updatable)
  - `tax_debt` (Long, Not Null, Default: 0)

---

### 4. **solontax-g1-management-kafka**
**Purpose:** Event-Driven Communication Layer

**Responsibilities:**
- Kafka producers for sending events
- Kafka consumers for processing events
- Both single-message and batch processing
- Dead Letter Topic (DLT) handling
- Retry mechanisms with parking lot pattern

**Key Components:**

#### Producers:
- `PersonProducer` - sends events to Kafka topics:
  - `upsert(Person)` - upsert person event
  - `delete(UUID)` - delete person event
  - `calculateTax(TaxCalculationDto)` - single tax calculation
  - `calculateTaxInBatch(List<TaxCalculationDto>)` - batch tax calculation
  - `upsertForManualConsume(Person)` - for manual batch consumption

#### Consumers:

**SinglePersonConsumer:**
- Listens to individual events with automatic retries
- Topics: `upsert-person`, `delete-person`, `tax-calculation`
- DLT handling for failed messages
- Group ID: `solontax-g1-group`

**BatchPersonConsumer:**
- Processes messages in batches
- Topic: `tax-calculation-batch`
- Manual acknowledgment
- Failed messages sent to DLT individually
- Group ID: `solontax-g1-batch-group`

**Kafka Topics:**
- `upsert-person` - person create/update events
- `delete-person` - person deletion events
- `upsert-person-batch` - batch upsert for manual polling
- `tax-calculation` - single tax debt updates
- `tax-calculation-batch` - batch tax debt updates
- `*.DLT` - Dead Letter Topics for retry
- `parking-lot` - final storage for unprocessable messages

**Retry Mechanism:**
- Single message: fail → retry → fail → DLT → retry → fail → parking-lot
- Batch message: one fails → DLT → retry → fail → parking-lot (others continue)

---

### 5. **solontax-g1-management-migration**
**Purpose:** Background Job Scheduler Service

**Responsibilities:**
- Scheduled tasks for testing Kafka flows
- Manual batch consumption via REST API polling
- Data generation for testing

**Key Components:**
- `PersonSchedulerController` - REST endpoints to control schedulers
- `PersonScheduler` - manages scheduled tasks
- `PersonJobService` - business logic for scheduled jobs
  - `sendUpsertPersonEvents()` - generates and sends person events
  - `consumeUpsertPersonEvents()` - polls and processes batch events

**Schedulers:**
1. **Upsert Person Event Scheduler** - sends random person data every 2 seconds
2. **Consume Upsert Person Event Scheduler** - polls and processes batch events every 7 seconds

**Ports:**
- Default: 1978 (local)
- Docker: 1930
- Kubernetes NodePort: 30030

---

## API Endpoints Documentation

### Main Application (solontax-g1-management-adapter)

#### Base URL
- Local: `http://localhost:1945`
- Docker: `http://localhost:1975`
- Kubernetes: `http://localhost:30075`

---

#### 1. Welcome Endpoint
```
GET /
```
**Description:** Returns welcome message

**Response:**
```
Welcome to Hexagonal Solon Tax G1
```

---

#### 2. Create Person
```
POST /v1/person
Content-Type: application/json
```

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "dateOfBirth": "1990-01-15",
  "taxNumber": 123456789,
  "taxDebt": 0
}
```

**Response:** `201 CREATED`
```json
{
  "id": "uuid",
  "firstName": "John",
  "lastName": "Doe",
  "age": 35,
  "taxNumber": 123456789,
  "taxDebt": 0
}
```

**Description:** Creates a new person in the database synchronously.

---

#### 3. Search Persons (with RSQL filtering)
```
GET /v1/person?query=firstName==John&sortBy=lastName&sortDirection=asc&offset=0&size=10
```

**Query Parameters:**
- `query` (optional) - RSQL filter expression
  - Examples:
    - `firstName==John`
    - `taxDebt>1000`
    - `lastName==Doe;age>30` (AND condition)
    - `firstName==John,firstName==Jane` (OR condition)
- `sortBy` (default: "id") - field to sort by
- `sortDirection` (default: "asc") - "asc" or "desc"
- `offset` (default: 0) - page number
- `size` (default: 10) - page size

**Response:** `200 OK`
```json
{
  "content": [
    {
      "id": "uuid",
      "firstName": "John",
      "lastName": "Doe",
      "age": 35,
      "taxNumber": 123456789,
      "taxDebt": 0
    }
  ],
  "pageable": {...},
  "totalPages": 1,
  "totalElements": 1
}
```

**Description:** Search and filter persons with pagination and RSQL support.

---

#### 4. Find Person by Tax Number
```
GET /v1/person/by-tax-number/{taxNumber}
```

**Path Parameters:**
- `taxNumber` - unique tax identification number

**Response:** `200 OK`
```json
{
  "id": "uuid",
  "firstName": "John",
  "lastName": "Doe",
  "age": 35,
  "taxNumber": 123456789,
  "taxDebt": 0
}
```

**Error Response:** `404 NOT FOUND`
```json
{
  "message": "Cannot found person with tax number: 123456789"
}
```

---

#### 5. Send Upsert Person Event (Kafka)
```
POST /v1/person/kafka
Content-Type: application/json
```

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "dateOfBirth": "1990-01-15",
  "taxNumber": 123456789,
  "taxDebt": 0
}
```

**Response:** `200 OK`
```
Upsert person event sent
```

**Description:** Sends person upsert event to Kafka topic `upsert-person`. The event will be consumed asynchronously by `SinglePersonConsumer`.

---

#### 6. Send Delete Person Event (Kafka)
```
DELETE /v1/person/kafka/{id}
```

**Path Parameters:**
- `id` - UUID of the person to delete

**Response:** `200 OK`
```
Delete person event send
```

**Description:** Sends delete event to Kafka topic `delete-person`. Includes intentional random failures for testing retry mechanism.

---

#### 7. Send Tax Calculation Event (Single)
```
POST /v1/person/kafka/tax
Content-Type: application/json
```

**Request Body:**
```json
{
  "taxNumber": 123456789,
  "calculatedTax": 5000
}
```

**Response:** `200 OK`
```
Tax calculation event sent
```

**Description:** Sends tax calculation event to `tax-calculation` topic. Updates person's tax debt by adding the calculated tax.

---

#### 8. Send Tax Calculation Events (Batch)
```
POST /v1/person/kafka/batch/tax
Content-Type: application/json
```

**Request Body:**
```json
[
  {
    "taxNumber": 123456789,
    "calculatedTax": 5000
  },
  {
    "taxNumber": 987654321,
    "calculatedTax": 3000
  }
]
```

**Response:** `200 OK`
```
Tax calculation in batch event sent
```

**Description:** Sends multiple tax calculation events to `tax-calculation-batch` topic. Processed by `BatchPersonConsumer` with manual acknowledgment.

---

#### 9. Send Upsert Event for Manual Consumption
```
POST /v1/person/kafka/batch/manual
Content-Type: application/json
```

**Request Body:**
```json
{
  "firstName": "Jane",
  "lastName": "Smith",
  "dateOfBirth": "1995-05-20",
  "taxNumber": 555666777,
  "taxDebt": 0
}
```

**Response:** `200 OK`
```
Upsert person event is sent to upsert-person-batch
```

**Description:** Sends event to `upsert-person-batch` topic for manual polling/consumption.

---

#### 10. Consume Upsert Person Batch Events (Manual Poll)
```
GET /v1/person/kafka/batch/manual
```

**Response:** `200 OK`
```json
{
  "events": [
    {
      "firstName": "Jane",
      "lastName": "Smith",
      "dateOfBirth": "1995-05-20",
      "taxNumber": 555666777,
      "taxDebt": 0
    }
  ],
  "count": 1
}
```

**Description:** Manually polls and retrieves up to 3 messages from `upsert-person-batch` topic. Uses `KafkaBatchService` for manual offset management.

---

### Migration Service (solontax-g1-management-migration)

#### Base URL
- Local: `http://localhost:1978`
- Docker: `http://localhost:1930`
- Kubernetes: `http://localhost:30030`

---

#### 1. Start Upsert Person Event Scheduler
```
POST /v1/person/scheduler/start/upsert-person-event
```

**Response:** `200 OK`
```
Upsert person event schedulers start
```

**Description:** Starts a scheduled task that sends random person data to `upsert-person-batch` topic every 2 seconds.

---

#### 2. Stop Upsert Person Event Scheduler
```
POST /v1/person/scheduler/stop/upsert-person-event
```

**Response:** `200 OK`
```
Upsert person event schedulers stop
```

**Description:** Stops the upsert person event scheduler.

---

#### 3. Start Consume Upsert Person Event Scheduler
```
POST /v1/person/scheduler/start/consume-upsert-person-event
```

**Response:** `200 OK`
```
Consume upsert person event schedulers start
```

**Description:** Starts a scheduled task that polls and consumes events from the main application's batch endpoint every 7 seconds, then saves them to the database.

---

#### 4. Stop Consume Upsert Person Event Scheduler
```
POST /v1/person/scheduler/stop/consume-upsert-person-event
```

**Response:** `200 OK`
```
Consume upsert person event schedulers start
```

**Description:** Stops the consume upsert person event scheduler.

---

## Deployment Instructions

### Prerequisites
- **Docker:** Installed and running
- **Kubernetes:** Minikube or Docker Desktop with Kubernetes enabled
- **Maven:** 3.6+ (for local builds)
- **Java:** JDK 17

---

## Local Docker Deployment

### Step 1: Build Docker Images

Navigate to the backend directory:
```cmd
cd C:\Users\nhkh\OneDrive - Netcompany\Desktop\solon-product\solontax-g1\backend
```

Build the Docker images using Docker Compose:
```cmd
docker compose build
```

This will create two images:
- `solontax-g1-management:0.0.1`
- `solontax-g1-management-migration:0.0.1`

---

### Step 2: Clean Up (Optional)

Stop and remove all existing containers and volumes:
```cmd
docker compose down -v
```

---

### Step 3: Start All Services

Start all services in detached mode with build:
```cmd
docker compose up -d --build
```

This command will start:
- **PostgreSQL** on port `5430`
- **Kafka Broker** on ports `9092` (Windows host) and `29092` (Docker network)
- **solontax-g1-management** on port `1975`
- **solontax-g1-management-migration** on port `1930`

---

### Step 4: Verify Services

Check running containers:
```cmd
docker compose ps
```

View logs:
```cmd
docker compose logs -f
```

View specific service logs:
```cmd
docker compose logs -f solontax-g1-management
```

---

### Step 5: Access Applications

- **Main Application:** http://localhost:1975
- **Migration Service:** http://localhost:1930
- **PostgreSQL:** localhost:5430
  - Username: `postgres`
  - Password: `postgres`
  - Database: `solontax_g1`
- **Kafka:** localhost:9092

---

### Step 6: Test the Application

Test welcome endpoint:
```cmd
curl http://localhost:1975
```

Create a person:
```cmd
curl -X POST http://localhost:1975/v1/person ^
  -H "Content-Type: application/json" ^
  -d "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"dateOfBirth\":\"1990-01-15\",\"taxNumber\":123456789,\"taxDebt\":0}"
```

Search persons:
```cmd
curl "http://localhost:1975/v1/person?offset=0&size=10"
```

---

### Step 7: Stop Services

Stop all services:
```cmd
docker compose down
```

Stop and remove volumes:
```cmd
docker compose down -v
```

---

## Local Kubernetes Deployment

### Prerequisites

Ensure Kubernetes is enabled:
- **Docker Desktop:** Enable Kubernetes in settings
- **Minikube:** Start with `minikube start`

---

### Step 1: Build Docker Images

First, build the images using Docker Compose (same as Docker deployment):
```cmd
cd C:\Users\nhkh\OneDrive - Netcompany\Desktop\solon-product\solontax-g1\backend
docker compose build
```

**Important:** Kubernetes needs access to these images. If using Minikube:
```cmd
minikube image load solontax-g1-management:0.0.1
minikube image load solontax-g1-management-migration:0.0.1
```

---

### Step 2: Navigate to k8s Directory

```cmd
cd k8s
```

---

### Step 3: Deploy All Services

Apply all Kubernetes manifests:
```cmd
kubectl apply -f .
```

This deploys:
- PostgreSQL with persistent volume
- Kafka broker
- Main management application
- Migration/scheduler service

---

### Step 4: Verify Deployments

Check all pods:
```cmd
kubectl get pods
```

Check services:
```cmd
kubectl get services
```

Check deployments:
```cmd
kubectl get deployments
```

View pod logs:
```cmd
kubectl logs -f <pod-name>
```

For example:
```cmd
kubectl logs -f deployment/solontax-g1-management
```

---

### Step 5: Access Applications via NodePort

The services are exposed using NodePort:

- **Main Application:** http://localhost:30075
- **Migration Service:** http://localhost:30030
- **PostgreSQL:** localhost:30430
- **Kafka:** localhost:30092

If using Minikube, you may need to use the Minikube IP:
```cmd
minikube service solontax-g1-management-service --url
minikube service solontax-g1-management-migration-service --url
```

---

### Step 6: Test the Application

Test welcome endpoint:
```cmd
curl http://localhost:30075
```

Create a person:
```cmd
curl -X POST http://localhost:30075/v1/person ^
  -H "Content-Type: application/json" ^
  -d "{\"firstName\":\"Alice\",\"lastName\":\"Johnson\",\"dateOfBirth\":\"1985-03-10\",\"taxNumber\":999888777,\"taxDebt\":0}"
```

---

### Step 7: Scale Deployments (Optional)

Scale the main application:
```cmd
kubectl scale deployment solontax-g1-management --replicas=3
```

---

### Step 8: Update Deployments

After rebuilding images, restart deployments:
```cmd
kubectl rollout restart deployment solontax-g1-management
kubectl rollout restart deployment solontax-g1-management-migration
```

---

### Step 9: Delete All Resources

Remove all Kubernetes resources:
```cmd
kubectl delete -f .
```

Or delete individually:
```cmd
kubectl delete deployment solontax-g1-management
kubectl delete service solontax-g1-management-service
kubectl delete pvc postgres-pvc
```

---

## Database Connection

### PostgreSQL Configuration

**Docker:**
```
Host: localhost
Port: 5430
Database: solontax_g1
Username: postgres
Password: postgres
```

**Kubernetes:**
```
Host: localhost
Port: 30430
Database: solontax_g1
Username: postgres
Password: postgres
```

**Connection String:**
```
jdbc:postgresql://localhost:5430/solontax_g1
```

---

## Kafka Configuration

### Topics

The application automatically creates the following topics:
- `upsert-person` - Person upsert events
- `delete-person` - Person delete events
- `upsert-person-batch` - Batch upsert for manual consumption
- `tax-calculation` - Single tax calculations
- `tax-calculation-batch` - Batch tax calculations
- `*.DLT` - Dead Letter Topics for each main topic
- `parking-lot` - Final destination for unprocessable messages

### Kafka Ports

**Docker:**
- Host (Windows): `localhost:9092`
- Docker network: `kafka-broker:29092`

**Kubernetes:**
- NodePort: `localhost:30092`
- Internal: `kafka-broker-service:9092`

---

## Troubleshooting

### Docker Issues

**Issue:** Port already in use
```cmd
# Find and stop processes using the ports
netstat -ano | findstr :1975
netstat -ano | findstr :5430
```

**Issue:** Cannot connect to database
```cmd
# Check PostgreSQL logs
docker compose logs postgres
```

**Issue:** Kafka connection errors
```cmd
# Check Kafka logs
docker compose logs kafka-broker
```

---

### Kubernetes Issues

**Issue:** Pods not starting
```cmd
# Describe the pod for events
kubectl describe pod <pod-name>

# Check pod logs
kubectl logs <pod-name>
```

**Issue:** ImagePullBackOff error
- Ensure images are built locally
- For Minikube: use `minikube image load <image-name>`
- Set `imagePullPolicy: IfNotPresent` in deployment manifests

**Issue:** CrashLoopBackOff
```cmd
# Check application logs
kubectl logs deployment/solontax-g1-management

# Check previous logs if pod restarted
kubectl logs <pod-name> --previous
```

---

## Known Issues

1. **PostgreSQL Version Issue on Windows:** PostgreSQL version 14+ may have timezone issues on Windows. See [StackOverflow discussion](https://stackoverflow.com/questions/79570415/fatal-invalid-value-for-parameter-timezone-asia-saigon). Using PostgreSQL 13 resolves this issue.

2. **Global Exception Handler:** Currently cannot handle missing path variables and some validation errors.

3. **Random Failures:** The delete person event consumer includes intentional random failures (90% failure rate) for testing retry mechanisms. The tax calculation consumer has a 50% failure rate.

---

## Testing the Event Flow

### Test Single Event Processing

1. Create a person via Kafka:
```cmd
curl -X POST http://localhost:1975/v1/person/kafka ^
  -H "Content-Type: application/json" ^
  -d "{\"firstName\":\"Test\",\"lastName\":\"User\",\"dateOfBirth\":\"2000-01-01\",\"taxNumber\":111222333,\"taxDebt\":0}"
```

2. Check logs to see event processing
3. Verify person was created:
```cmd
curl "http://localhost:1975/v1/person/by-tax-number/111222333"
```

---

### Test Batch Processing

1. Send tax calculations in batch:
```cmd
curl -X POST http://localhost:1975/v1/person/kafka/batch/tax ^
  -H "Content-Type: application/json" ^
  -d "[{\"taxNumber\":111222333,\"calculatedTax\":1000},{\"taxNumber\":111222333,\"calculatedTax\":500}]"
```

2. Verify tax debt was updated:
```cmd
curl "http://localhost:1975/v1/person/by-tax-number/111222333"
```

---

### Test Scheduler Flow

1. Start the upsert event scheduler:
```cmd
curl -X POST http://localhost:1930/v1/person/scheduler/start/upsert-person-event
```

2. Wait a few seconds, then start the consumer scheduler:
```cmd
curl -X POST http://localhost:1930/v1/person/scheduler/start/consume-upsert-person-event
```

3. Check the database to see persons being created:
```cmd
curl "http://localhost:1975/v1/person?offset=0&size=20"
```

4. Stop the schedulers:
```cmd
curl -X POST http://localhost:1930/v1/person/scheduler/stop/upsert-person-event
curl -X POST http://localhost:1930/v1/person/scheduler/stop/consume-upsert-person-event
```

---

## Architecture Patterns Used

### 1. Hexagonal Architecture (Ports and Adapters)
- **Core domain** is independent of external frameworks
- **Ports** define interfaces for external communication
- **Adapters** implement ports for specific technologies (HTTP, JPA, Kafka)

### 2. Event-Driven Architecture
- Asynchronous communication via Kafka
- Event sourcing for audit trails
- Retry mechanisms with DLT pattern

### 3. CQRS Pattern Elements
- Separate read (search with RSQL) and write operations
- Event-driven updates via Kafka

### 4. Retry Pattern
- Single message: automatic retries with DLT
- Batch message: skip failed and continue processing
- Parking lot for ultimate failures

---

## Future Improvements

1. **Global Exception Handler:** Improve to handle all validation and path variable errors
2. **Scalability:** Extract common repository and entity classes
3. **Security:** Add authentication and authorization
4. **API Documentation:** Integrate Swagger/OpenAPI
5. **Monitoring:** Add Prometheus and Grafana
6. **Testing:** Add comprehensive unit and integration tests
7. **CI/CD:** Implement automated build and deployment pipelines
8. **Circuit Breaker:** Add resilience patterns for external service calls
9. **Configuration Management:** Use Spring Cloud Config or ConfigMaps
10. **Health Checks:** Enhance actuator endpoints for better monitoring

---

## Additional Resources

- **Spring Boot Documentation:** https://spring.io/projects/spring-boot
- **Apache Kafka Documentation:** https://kafka.apache.org/documentation/
- **Hexagonal Architecture:** https://alistair.cockburn.us/hexagonal-architecture/
- **RSQL Query Language:** https://github.com/jirutka/rsql-parser

---

## Project Structure Summary

```
backend/
├── pom.xml                                      # Parent POM
├── docker-compose.yml                           # Docker orchestration
├── README.md                                    # Quick reference
├── INSTRUCTION.md                               # This document
│
├── solontax-g1-management-core/                # Domain layer
│   ├── src/main/java/solontax/g1/management/core/
│   │   ├── domain/model/                       # Domain models
│   │   ├── domain/port/                        # Port interfaces
│   │   ├── application/service/                # Business services
│   │   ├── common/dto/                         # DTOs
│   │   └── constant/                           # Constants
│   └── pom.xml
│
├── solontax-g1-management-adapter/             # HTTP adapter
│   ├── src/main/java/solontax/g1/management/
│   │   ├── adapter/inbound/http/               # REST controllers
│   │   └── SolontaxG1ManagementApplication.java
│   ├── Dockerfile
│   └── pom.xml
│
├── solontax-g1-management-dao/                 # Database adapter
│   ├── src/main/java/solontax/g1/management/dao/
│   │   ├── outbound/entity/                    # JPA entities
│   │   ├── outbound/jpa/                       # JPA adapters
│   │   └── repository/                         # JPA repositories
│   └── pom.xml
│
├── solontax-g1-management-kafka/               # Kafka adapter
│   ├── src/main/java/solontax/g1/management/kafka/
│   │   ├── producer/                           # Kafka producers
│   │   ├── consumer/                           # Kafka consumers
│   │   ├── config/                             # Kafka configuration
│   │   └── service/                            # Kafka services
│   └── pom.xml
│
├── solontax-g1-management-migration/           # Scheduler service
│   ├── src/main/java/solontax/g1/management/migration/
│   │   ├── controller/                         # Scheduler controllers
│   │   ├── service/                            # Scheduler services
│   │   └── SolontaxG1ManagementMigrationApplication.java
│   ├── Dockerfile
│   └── pom.xml
│
└── k8s/                                        # Kubernetes manifests
    ├── postgres-deployment.yaml
    ├── kafka-deployment.yaml
    ├── solontax-g1-management-deployment.yaml
    └── solontax-g1-management-migration-deployment.yaml
```

---

**Last Updated:** October 14, 2025
**Version:** 0.0.1-SNAPSHOT
**Author:** SolonTax G1 Team

