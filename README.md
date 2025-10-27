# Feedback Service

This microservice student feedback for the E-Learning Platform. It handles feedback creation, retrieval, and rating calculation using PostgreSQL functions and triggers.

## Related Services

| Service                                                               | Description                       |
|-----------------------------------------------------------------------|-----------------------------------|
| [Course Service](https://github.com/annasergeevaGIT/course-service)   | Handles courses                   |
| [Feedback Service](https://github.com/annasergeevaGIT/feedback-service) | Manages user feedback             |
| [Course Aggregate Service](../course-aggregate-service)               | Aggregates course and review data |
| [Gateway Service](../gateway-service)                                 | Routes requests to microservices  |
| [Config Server](../config-server)                                     | Centralized configuration storage |

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
