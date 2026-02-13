# Feedback Service

This microservice student feedback for the E-Learning Platform. It handles feedback creation, retrieval, and rating calculation using PostgreSQL functions and triggers.

## Related Services

| Service                                                               | Description                       |
|-----------------------------------------------------------------------|-----------------------------------|
| [Enrollment Service](https://github.com/annasergeevaGIT/enrollment-service-e-learning-platform)   | Manages course enrollments |
| [Course Service](https://github.com/annasergeevaGIT/course-service-e-learning-platform)   | Handles courses and content|
| [Feedback Service](https://github.com/annasergeevaGIT/eedback-service-e-learning-platform) | Manages ratings and feedback |
| [Course Aggregate Service](https://github.com/annasergeevaGIT/aggregate-service-e-learning-platform)| Aggregates course and review data |
| [Gateway Service](https://github.com/annasergeevaGIT/gateway-service-e-learning-platform)| Routing, security, rate limiting |
| [Discovery Service](https://github.com/annasergeevaGIT/discovery-service-e-learning-platform)| Eureka Service registry |
| [Dispatcher Service](https://github.com/annasergeevaGIT/dispatcher-service-e-learning-platform)| Kafka producer/consumer (event streaming) |
| [Docker Deployment](https://github.com/annasergeevaGIT/dispatcher-service-e-learning-platform)| Centralized configuration management |

## Overview

The Feedback Service allows students to leave feedback and ratings for completed courses.
It supports rating aggregation logic using PostgreSQL functions and triggers for accurate score calculation based on binomial distribution (Wilson score confidence interval).

The service can operate independently or as part of the complete E-Learning microservices ecosystem.

## Functionality

- Create, update, and delete user feedback
- Retrieve feedback by user or course
- Calculate average course ratings using a statistical Wilson score algorithm
- Expose course rating and feedback data for aggregation in other services
- Database migration with Flyway
- Fully containerized with Docker

## Endpoints

| Method  | Endpoint                        | Description                            |
|---------|---------------------------------|----------------------------------------|
| `POST`  | `/v1/feedbacks`                 | Create a new feedback                  |
| `GET`   | `/v1/feedbacks/{id}`            | Get feedback by ID                     |
| `GET`   | `/v1/feedbacks/my`              | Get feedbacks of the current user      |
| `GET`   | `/v1/feedbacks/course/{menuId}` | Get feedbacks and ratings for a course |
| `POST`  | `/v1/feedbacks/ratings`         | Get ratings of multiple courses        |

## Tech Stack

- **Java 21**
- **Spring Boot 3**
- **Spring Data JPA**
- **PostgreSQL**
- **Flyway**
- **MapStruct (DTO mapping)**
- **Lombok**
- **WebTestClient / JUnit 5 (testing)**
- **Kafka**
- **Micrometer / Prometheus**
- **Eureka Discovery**
- **Docker**
- **GitHub Actions (CI/CD)**

## Build & Run

```bash
./gradlew clean bootBuildImage
docker-compose up -d
./gradlew test
