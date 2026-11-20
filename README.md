# 🏋️ Olympic Nutrition Tracker

Backend REST API developed with **Spring Boot** for the **Olympic Nutrition Tracker** project as part of the **Bachelor Développeur d'Application – Studi**.

The application manages athletes, coaches, consultations, and nutrition entries through a secure REST API.

---

# 🚀 Features

- CRUD management for Athletes
- CRUD management for Coaches
- CRUD management for Consultations
- CRUD management for Nutrition Entries
- Assign a Coach to an Athlete
- JWT Authentication
- Role-Based Authorization (ADMIN, COACH, ATHLETE)
- Spring Security
- BCrypt Password Encryption
- Global Exception Handling
- Validation using Jakarta Validation
- Pagination using Spring Data
- MySQL Database Integration
- Swagger / OpenAPI Documentation
- JUnit 5 Service Tests
- API Testing with Postman

---

# 🔐 Authentication

The application uses **Spring Security with JWT Authentication**.

Authentication workflow:

1. Register a new user.
2. Login using username and password.
3. Receive a JWT Token.
4. Use the token to access protected endpoints.

Example:

```http
Authorization: Bearer <JWT_TOKEN>
```

---

# 👥 User Roles

## ADMIN

- Full CRUD on Athletes
- Full CRUD on Coaches
- Full CRUD on Consultations
- Full CRUD on Nutrition Entries
- Privileged account provisioning is handled by a trusted database operator.

---

## COACH

- View assigned athletes and the coach directory
- View and manage nutrition entries for assigned athletes
- View, create, and update their own consultations for assigned athletes

---

## ATHLETE

- View their linked athlete profile, consultations, and nutrition entries
- View the coach directory
- Public registration creates an ATHLETE account. A trusted operator must link it to a verified profile before it can read athlete records.

---

# 🛠️ Technologies Used

- Java 21
- Spring Boot 3.3.4
- Spring Security
- JWT
- BCrypt Password Encoder
- Spring Data JPA
- Hibernate
- MySQL
- Jakarta Validation
- Swagger / OpenAPI
- JUnit 5
- Postman
- Maven
- IntelliJ IDEA

---

# 📂 Project Structure

```text
src/main/java/com/eman/tracker/olympicnutritiontracker

├── config
├── controller
├── dto
├── exception
├── mapper
├── model
├── repository
├── security
├── service
└── OlympicNutritionTrackerApplication
```

---

# 🔑 Authentication Endpoints

| Method | Endpoint |
|---------|----------|
| POST | /api/auth/register |
| POST | /api/auth/login |

---

# 🌐 API Endpoints

## Athletes

| Method | Endpoint |
|---------|----------|
| GET | /api/athletes |
| GET | /api/athletes/{id} |
| POST | /api/athletes |
| PUT | /api/athletes/{id} |
| DELETE | /api/athletes/{id} |
| PUT | /api/athletes/{athleteId}/assign-coach/{coachId} |

---

## Coaches

| Method | Endpoint |
|---------|----------|
| GET | /api/coaches |
| GET | /api/coaches/{id} |
| POST | /api/coaches |
| PUT | /api/coaches/{id} |
| DELETE | /api/coaches/{id} |
| GET | /api/coaches/{coachId}/athletes |

---

## Consultations

| Method | Endpoint |
|---------|----------|
| GET | /api/consultations |
| GET | /api/consultations/{id} |
| POST | /api/consultations |
| PUT | /api/consultations/{id} |
| DELETE | /api/consultations/{id} |

---

## Nutrition Entries

| Method | Endpoint |
|---------|----------|
| GET | /api/nutrition-entries |
| GET | /api/nutrition-entries/{id} |
| GET | /api/nutrition-entries?athleteId={athleteId} |
| POST | /api/nutrition-entries |
| PUT | /api/nutrition-entries/{id} |
| DELETE | /api/nutrition-entries/{id} |

---

# 🗄️ Database

The application uses **MySQL** as the relational database.

Main entities:

- Athlete
- Coach
- Consultation
- NutritionEntry
- User

Relationships:

- One Coach → Many Athletes
- One Athlete → Many Nutrition Entries
- One Athlete → Many Consultations
- One Coach → Many Consultations

---

# 🧪 Testing

The application has been tested using **Postman**.

Validated features:

- JWT Authentication
- Login
- Register
- Role-Based Authorization
- CRUD Operations
- Validation
- Exception Handling
- 401 Unauthorized
- 403 Forbidden
- 404 Not Found

JUnit 5 was used for service layer testing.

---

# ▶️ Running the Project

Clone the repository

```bash
git clone https://github.com/eman-java-dev/OlympicNutritionTracker-Backend.git
```

Open the project using IntelliJ IDEA.

Set `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, and `JWT_SECRET` in the process environment or in your IDE's run configuration. Store credentials outside the repository. The signing secret is standard Base64 for at least 32 random bytes. See [deployment and schema setup](deploy/README.md) for the existing-schema migration and the production configuration.

Run the application:

```bash
mvn spring-boot:run
```

Run the build and tests with `mvn verify` or `./mvnw verify` (`mvnw.cmd verify` on Windows). The integration tests use embedded H2 and keep the application database out of the test run.

---

# 📖 Swagger

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

---

# 🔮 Future Improvements

- Refresh Token
- Email Verification
- Password Reset
- Angular Frontend
- Docker Support
- GitHub Actions CI/CD
- Monitoring and Logging
- Integration Tests

---

# 👩‍💻 Author

**Eman Altohami**

Bachelor Développeur d'Application – Studi

GitHub:

https://github.com/eman-java-dev

---

# 📌 Project Status

This project provides a complete REST API secured with **JWT Authentication** and **Role-Based Authorization** using Spring Security.

Implemented successfully:

- JWT Authentication
- Role-Based Authorization
- CRUD Operations
- Validation
- Global Exception Handling
- Pagination
- Swagger Documentation
- MySQL Integration
- JUnit Tests
- Postman API Testing

The project is ready for demonstration and evaluation.