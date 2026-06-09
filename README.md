# Account Service

REST API for managing users, bank accounts, and transaction history. Built with Spring Boot 4 and PostgreSQL.

## Live Demo

| | |
|---|---|
| **API Base URL** | [https://account-service.onrender.com](https://account-service-d1y8.onrender.com) |
| **Bruno Collection** | [`/bruno`](./bruno) |

---

## Local Setup

**Prerequisites:** Docker + Docker Compose

```bash
git clone https://github.com/your-username/account-service.git
cd account-service
docker compose up --build
```

App → `http://localhost:8080`

---

## API Overview

All responses follow `{ success, message, data, timestamp }`.

### Users — `/api/v1/users`
| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/` | Create user |
| `GET` | `/{id}` | Get user |
| `GET` | `/` | List users |
| `PATCH` | `/{id}` | Update user |
| `DELETE` | `/{id}` | Delete user |

### Accounts — `/api/v1/accounts`
| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/users/{userId}` | Open account |
| `GET` | `/{accountId}` | Get account |
| `GET` | `/users/{userId}` | Get user's accounts |
| `POST` | `/{accountId}/deposit` | Deposit |
| `POST` | `/{accountId}/withdraw` | Withdraw |
| `POST` | `/{accountId}/transfer` | Transfer |
| `POST` | `/{accountId}/freeze` | Freeze |
| `POST` | `/{accountId}/unfreeze` | Unfreeze |
| `POST` | `/{accountId}/close` | Close |

### Transactions — `/api/v1`
| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/accounts/{accountId}/transactions` | List (filterable by `type`, `from`/`to`) |
| `GET` | `/transactions/{id}` | Get by ID |
| `GET` | `/transactions/reference/{ref}` | Get by reference |

---

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_URL` ` | Postgres url |
| `DB_NAME`  | Database name |
| `DB_USERNAME` | DB user |
| `DB_PASSWORD`  | DB password |
| `SPRING_PROFILES_ACTIVE` | Active profile |

---

## Profiles

| Profile | Use case |
|---------|----------|
| `local` | IDE / `gradlew` — connects to `localhost:5432` |
| `docker` | docker-compose — connects to `db` service |
| `prod` | Render |

---

## Architecture Notes

- **One account per user** is not enforced — a user can open multiple accounts
- **Transfers** are atomic: both account updates and both transaction records commit in one DB transaction
- **Layered Docker image** (4-stage build) keeps rebuilds fast and the final image minimal
- **Non-root container user** for security