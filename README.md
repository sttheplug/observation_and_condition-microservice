# Observation & Condition Service

## Overview
The **Observation & Condition Service** stores patient observations and diagnosed conditions.

## Features
- Record and retrieve medical observations
- Store diagnosed conditions
- Secure API endpoints for medical staff

## Technologies Used
- **Spring Boot**
- **JPA & Hibernate**
- **PostgreSQL/MySQL**
- **Docker & Kubernetes**

## Installation & Setup
```sh
git clone https://github.com/yourusername/observation-condition-service.git
cd observation-condition-service
mvn clean install
docker build -t observation-service .
docker run -p 8084:8084 observation-service
```

## Other Services
- Fetches Patient Data from Patient Service.
- Fetches all observations and conditions by practitioner from Practitioner Service.
- Used by Search Service to query patient records.
