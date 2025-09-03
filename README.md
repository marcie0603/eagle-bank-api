# Eagle Bank API

A REST API built with **Spring Boot 3**, implementing the Barclays take-home exercise.  
This stage includes full **User management** with **JWT authentication**.

---

## Features Implemented (so far)

- User signup (`POST /v1/users`)
- User login with JWT (`POST /auth/login`)
- Fetch user details (`GET /v1/users/{id}`)
- Update user details (`PATCH /v1/users/{id}`)
- Delete user (`DELETE /v1/users/{id}`)
- Access control:
    - Users can only access/update/delete their own data
    - 403 Forbidden returned if accessing another user
    - 404 Not Found returned if resource does not exist

---

## Tech Stack
- Java 17+ (running on OpenJDK 24 locally)
- Spring Boot 3.5.5
- Spring Data JPA + H2 in-memory database
- Spring Security with JWT (JJWT 0.11.5)
- Lombok

---

## How to Run
Clone the repo:

```bash
git clone https://github.com/marcie0603/eagle-bank-api.git
cd eagle-bank-api
```

---

## Build & run:

```bash
./mvnw clean install
./mvnw spring-boot:run
```

Server starts on: http://localhost:8080

---

## Postman Testing Guide

1. Create User(Signup)

```bash
POST http://localhost:8080/v1/users
```
Body (JSON):
{
"username": "maria",
"email": "maria@test.com",
"password": "12345"
}

2. Login(Get JWT)

```bash
POST http://localhost:8080/auth/login
```
Body (JSON):
**{
"username": "maria",
"password": "12345"
}**

Respons:
**Bearer eyJhbGciOiJIUzI1NiJ9...**

3. Fetch User(Authenticated)

```bash
GET http://localhost:8080/v1/users/{id}
```
Headers: **Authorization: Bearer `<token>`**

4. Update User

```bash
PATCH http://localhost:8080/v1/users/{id}
```
Headers: **Authorization: Bearer `<token>`**

Body (JSON): 
**{
"email": "maria+new@test.com"
}**

5. Delete User

```bash
DELETE http://localhost:8080/v1/users/{id}
```
Headers: **Authorization: Bearer `<token>`**
