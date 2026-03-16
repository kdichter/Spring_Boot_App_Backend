# Contact Management Application (Backend)

A containerized full-stack contact management system built with **Spring Boot, React, and PostgreSQL**.

The backend exposes a REST API for managing contacts and uses **JWT authentication** to secure endpoints.  
The entire system is containerized using **Docker** and can be started locally with **Docker Compose**.

---

## Tech Stack

**Backend**
- Java
- Spring Boot
- Spring Security
- JWT Authentication

**Frontend**
- React

**Database**
- PostgreSQL

**Infrastructure**
- Docker
- Docker Compose

**CI/CD**
- GitHub Actions

---

## Features

- Create, update, and delete contacts
- Secure user authentication with JWT
- RESTful API design
- Persistent PostgreSQL database
- Containerized deployment
- Automated Docker builds using GitHub Actions

---

## Prerequisites
- Install [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- Ensure Docker Desktop is running

---

## Running the Application

1. Open a terminal and navigate to the directory containing `docker-compose.yaml`

2. Start the application:
   ```
   docker compose up -d
   ```
3. Open your browser and navigate to:
   ```
   http://localhost:3000
   ```
4. To stop and remove the containers:
   ```
   docker compose down
   ```
5. To stop the containers and remove associated volumes (this deletes persisted data):
   ```
   docker compose down -v
   ```
## Acknowledgements

- The contact management functionality was inspired by the project from [GetArrays](https://github.com/getarrays/contactapi).
- The JWT authentication implementation was developed by following a tutorial by [Ali-Bouali](https://github.com/ali-bouali/spring-boot-3-jwt-security).
