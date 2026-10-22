# Online Voting System

A REST API for running secure online elections, built with **Spring Boot 3**, **Spring Security + JWT**, and **MySQL**.

Admins create elections and candidates; registered voters cast exactly one vote per election; results are published once an election ends.

## Features

- JWT-based stateless authentication with two roles: `ADMIN` and `VOTER`
- Passwords hashed with BCrypt
- Elections with automatic status: `UPCOMING`, `ACTIVE`, or `CLOSED` (derived from start/end time)
- One vote per user per election, enforced in code **and** by a unique database constraint (safe against double-clicks and race conditions)
- Result visibility rules: admins can view results anytime, voters only after the election ends
- Consistent JSON error responses with validation messages

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2.5 (Web, Data JPA, Validation) |
| Security | Spring Security, JJWT 0.12.5 |
| Database | MySQL 8+ |
| Build | Maven |

## Project Structure

```
src/main/java/com/voting
├── config/        DataInitializer (creates default admin on startup)
├── controller/    AuthController, AdminController, ElectionController
├── dto/           Request/response records
├── exception/     ApiException, GlobalExceptionHandler
├── model/         User, Election, Candidate, Vote, Role
├── repository/    Spring Data JPA repositories
├── security/      JwtUtil, JwtAuthFilter, SecurityConfig, CustomUserDetailsService
└── service/       AuthService, ElectionService, VoteService
```

## Getting Started

### Prerequisites

- Java 17 or later
- Maven 3.6+ (or the one bundled in STS/Eclipse)
- MySQL running locally on port 3306

### Configuration

Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/voting_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: root
    password: <your-mysql-password>

app:
  jwt:
    secret: <long-random-secret>     # at least 32 characters
    expiration-ms: 86400000          # 24 hours
  admin:
    name: Administrator
    email: admin@voting.com
    password: Admin@123
```

The `voting_db` database and all tables are created automatically on first run.

> **Security note:** do not commit real passwords or the JWT secret. Change the default admin password and JWT secret before deploying anywhere public.

### Run from the command line

```bash
mvn spring-boot:run
```

The API starts on `http://localhost:8080`.

### Run in STS / Eclipse

1. File > Import > Maven > Existing Maven Projects, then select this folder.
2. Right-click the project > Maven > Update Project (Alt+F5).
3. Right-click `OnlineVotingApplication.java` > Run As > Spring Boot App.

### Default admin account

| Email | Password |
|---|---|
| `admin@voting.com` | `Admin@123` |

Created automatically on first startup if it does not already exist.

## API Reference

All endpoints except `/api/auth/**` require the header:

```
Authorization: Bearer <token>
```

| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Register a new voter, returns a token |
| POST | `/api/auth/login` | Public | Log in, returns a token |
| POST | `/api/admin/elections` | ADMIN | Create an election |
| POST | `/api/admin/elections/{id}/candidates` | ADMIN | Add a candidate (only before the election starts) |
| GET | `/api/elections` | Logged in | List all elections with status |
| GET | `/api/elections/{id}/candidates` | Logged in | List candidates of an election |
| POST | `/api/elections/{id}/vote` | VOTER | Cast a vote (election must be active) |
| GET | `/api/elections/{id}/has-voted` | Logged in | Check whether you already voted |
| GET | `/api/elections/{id}/results` | Logged in | Admin: anytime. Voter: only after the election ends |

### Sample requests

**Register**
```json
POST /api/auth/register
{ "name": "Ravi", "email": "ravi@mail.com", "password": "secret123" }
```

**Login**
```json
POST /api/auth/login
{ "email": "admin@voting.com", "password": "Admin@123" }
```
Response:
```json
{ "token": "eyJhbGciOi...", "type": "Bearer", "name": "Administrator", "email": "admin@voting.com", "role": "ADMIN" }
```

**Create election** (dates use `yyyy-MM-ddTHH:mm:ss`)
```json
POST /api/admin/elections
{ "title": "Class Rep", "description": "Vote", "startTime": "2026-10-17T09:00:00", "endTime": "2026-10-30T18:00:00" }
```

**Add candidate**
```json
POST /api/admin/elections/1/candidates
{ "name": "Alice", "party": "Blue" }
```

**Vote**
```json
POST /api/elections/1/vote
{ "candidateId": 1 }
```

**Results**
```json
GET /api/elections/1/results
[ { "candidateId": 1, "name": "Alice", "party": "Blue", "votes": 12 } ]
```
Results are sorted by votes, highest first.

### Business rules and error responses

| Situation | Status | Message |
|---|---|---|
| Email already registered | 409 | Email already registered |
| Wrong email or password | 401 | Invalid email or password |
| Missing or invalid token | 401 | (empty body) |
| Non-admin calls an `/api/admin/**` endpoint | 403 | Access denied |
| `endTime` not after `startTime` | 400 | endTime must be after startTime |
| Adding a candidate after the election started | 400 | Cannot add candidates after the election has started |
| Voting outside the start/end window | 400 | Election is not active |
| Candidate belongs to a different election | 400 | Candidate does not belong to this election |
| Admin tries to vote | 403 | Admins cannot vote |
| Voting twice in one election | 409 | You have already voted in this election |
| Voter views results before the election ends | 403 | Results are available after the election ends |
| Election or candidate not found | 404 | Election not found: {id} / Candidate not found |
| Validation failure | 400 | Map of field name to message |

Errors share one shape:
```json
{ "timestamp": "2026-10-22T10:15:30", "status": 409, "error": "You have already voted in this election" }
```

## Quick test walkthrough

1. Log in as admin and copy the token.
2. Create an election with `startTime` a few minutes from now.
3. Add two or more candidates to it.
4. Register a voter via `/api/auth/register` and copy that token.
5. Wait for the election to start, then vote with the voter token.
6. Try voting again to confirm you get `409`.
7. View results as admin (works immediately) or as the voter after `endTime`.
