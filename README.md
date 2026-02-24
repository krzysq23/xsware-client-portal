# XsWare Client Portal

Backend application for the XsWare Client Portal – a secure, production-ready Spring Boot 4 application designed to demonstrate enterprise-grade architecture, security, and clean code principles.

This project serves as a portfolio showcase for mid/senior Java backend development with modern best practices.

---

## Tech Stack

- Java 21
- Spring Boot 4
- Spring Security (JWT + Refresh Token rotation)
- Spring Data JPA
- PostgreSQL (AWS RDS)
- Flyway (database migrations)
- OpenTelemetry (tracing)
- Logback (structured logging)
- Docker (deployment-ready)
- AWS EC2 + S3 (production environment)

---

## Authentication & Security

### JWT Authentication
- Stateless authentication using short-lived Access Tokens
- Refresh Token stored in **HttpOnly Secure cookie**
- Refresh Token rotation
- Logout invalidates refresh token

### Security Features
- Role-based authorization
- CORS configuration (frontend ↔ API)
- Optimistic locking (`@Version`)
- Auditing (`createdBy`, `updatedBy`, `createdAt`, `updatedAt`)
- Structured logging with `traceId`
- Environment-based configuration (dev / prod)

---

## Architecture

The project follows layered architecture with separation of concerns:

```
pl.xsware
├── domain
├── application
└── infrastructure
```

### Key Principles
- Clear separation between domain and infrastructure
- No business logic in controllers
- Storage abstraction (strategy pattern for avatar storage)
- Transaction boundaries in service layer
- Clean DTO mapping
- Optimistic locking to prevent lost updates

---

## Configuration

### Development (Local)

```
application-dev.yml
```

- Local storage provider
- Local PostgreSQL or AWS RDS
- Debug logging

### Production

```
application-prod.yml
```

- S3 storage provider
- Structured JSON logging
- Secure cookies enabled
- AWS region configuration

---

## Running with Docker

Build:

```bash
./gradlew clean build

docker build -t xsware-client-portal .
docker run -p 8081:8081 xsware-client-portal
```

---

## Observability

- Actuator endpoints
- OpenTelemetry tracing
- Log correlation via traceId
- Production-ready JSON logs (CloudWatch compatible)
