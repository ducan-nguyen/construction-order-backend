# 🏗️ Building Material Order System — Backend

> REST API for a construction materials order management platform. Built with Spring Boot 3, secured with JWT + RBAC, documented with Swagger, and deployed to the cloud.

[![Java](https://img.shields.io/badge/Java-17-blue.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-orange.svg)](https://www.mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-24.0-blue.svg)](https://www.docker.com/)
[![Live Demo](https://img.shields.io/badge/Live%20Demo-Railway-blueviolet)](https://construction-order-frontend.vercel.app)

🔗 **Frontend repo:** [construction-order-frontend](https://github.com/ducan-nguyen/construction-order-frontend)  
🌐 **Live demo:** [construction-order-frontend.vercel.app](https://construction-order-frontend.vercel.app)

---

## 📌 Overview

A full-stack order management system for construction materials suppliers, featuring:
- End-to-end **order lifecycle management** with 5 status stages
- **Role-based access control** (ADMIN / CUSTOMER)
- **Refund request & approval** workflow
- Production deployment on **Railway** (backend) + **Vercel** (frontend)

---

## ✨ Features

### Order Management
- Product catalog API (create, update, list, filter)
- Order lifecycle: `PENDING → PAID → PROCESSING → SHIPPING → COMPLETED`
- Refund request & admin approval workflow
- Order history per customer

### Security
- JWT authentication with stateless sessions
- RBAC with 2 roles: **ADMIN** (full access) · **CUSTOMER** (order & refund)
- Custom **Rate-Limiting filter** to prevent API abuse
- BCrypt password hashing

### Quality & Documentation
- **Swagger / OpenAPI 3.0** — full API documentation
- **14 unit tests** (JUnit 5 + Mockito) covering OrderService & UserService
- Dockerized for consistent local and production environments

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3, Spring Security |
| Auth | JWT (JJWT), RBAC |
| ORM | Spring Data JPA / Hibernate |
| Database | MySQL 8.0 |
| Docs | Swagger / OpenAPI 3.0 |
| Testing | JUnit 5, Mockito |
| DevOps | Docker, Railway |
| Frontend | ReactJS, Vite, Vercel |

---

## 🗂️ Project Structure

```
src/
├── controller/       # REST API endpoints
├── service/          # Business logic
├── repository/       # JPA repositories
├── entity/           # JPA entities
├── dto/              # Request / Response DTOs
├── security/         # JWT filter, RBAC config
├── exception/        # Global exception handler
└── config/           # Swagger, CORS, app config
```

---

## 🚀 Getting Started

### Prerequisites
- Java 17+
- MySQL 8.0+
- Docker (optional)
- Maven 3.9+

### Run with Docker (Recommended)

```bash
git clone https://github.com/ducan-nguyen/construction-order-backend.git
cd construction-order-backend
docker build -t construction-backend .
docker run -p 8080:8080 construction-backend
```

### Run Manually

```bash
# 1. Clone repo
git clone https://github.com/ducan-nguyen/construction-order-backend.git
cd construction-order-backend

# 2. Configure database in src/main/resources/application.properties
# spring.datasource.url=jdbc:mysql://localhost:3306/construction_db
# spring.datasource.username=your_username
# spring.datasource.password=your_password

# 3. Run
mvn spring-boot:run
```

### API Documentation
Once running, access Swagger UI at:
```
http://localhost:8080/swagger-ui/index.html
```

---

## 🔐 API Overview

| Method | Endpoint | Role | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Register new account |
| POST | `/api/auth/login` | Public | Login & get JWT |
| GET | `/api/products` | Public | List products |
| POST | `/api/orders` | CUSTOMER | Place order |
| PUT | `/api/orders/{id}/status` | ADMIN | Update order status |
| POST | `/api/orders/{id}/refund` | CUSTOMER | Request refund |
| PUT | `/api/orders/{id}/refund/approve` | ADMIN | Approve refund |

---

## 🧪 Running Tests

```bash
mvn test
```
14 unit tests covering `OrderService` and `UserService`.

---

## ☁️ Deployment

- **Backend** → deployed on [Railway](https://railway.app)
- **Frontend** → deployed on [Vercel](https://vercel.com)
- Containerized with **Docker** for portable deployment

---

## 👤 Author

**Nguyen Duc An**  
[![LinkedIn](https://img.shields.io/badge/LinkedIn-ducan--nguyen9801-0A66C2?style=flat&logo=linkedin)](https://linkedin.com/in/ducan-nguyen9801)
[![GitHub](https://img.shields.io/badge/GitHub-ducan--nguyen-181717?style=flat&logo=github)](https://github.com/ducan-nguyen)
