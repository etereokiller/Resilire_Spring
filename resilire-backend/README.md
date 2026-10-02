# Resilire Backend — Release 1

Spring Boot (Java 17) backend covering Release 1 scope: patient/doctor
registration, JWT authentication, role-based authorization, doctor
profile/availability, and basic admin user management.

## Prerequisites

- Java 17 (a JDK 17 install is required; the Maven Wrapper handles Maven itself)
- PostgreSQL running locally, with a database named `resilire_db`

```sql
CREATE DATABASE resilire_db;
```

## Configuration

Default connection settings (`src/main/resources/application.yml`) assume:

- `jdbc:postgresql://localhost:5432/resilire_db`
- username `postgres`, password `postgres`

Override via environment variables if different:

```
DB_USERNAME=postgres
DB_PASSWORD=your_password
JWT_SECRET=<base64-encoded-secret>
CORS_ALLOWED_ORIGINS=http://localhost:5173
```

## Running

```bash
./mvnw spring-boot:run
```

On first run, Flyway applies the schema (`V1__init_schema.sql`) and seeds a
default admin account (`V2__seed_admin.sql`):

- email: `admin@resilire.com`
- password: `Admin@123`

Change this password (or the user directly in the database) before any
non-local use.

The API listens on `http://localhost:8080`.

## Key Endpoints

| Method | Path | Access |
|--------|------|--------|
| POST | `/api/auth/register/patient` | Public |
| POST | `/api/auth/register/doctor` | Public |
| POST | `/api/auth/login` | Public |
| GET | `/api/auth/me` | Authenticated |
| GET/PUT | `/api/patients/me` | PATIENT |
| GET/PUT | `/api/doctors/me` | DOCTOR |
| GET/POST | `/api/doctors/me/availability` | DOCTOR |
| DELETE | `/api/doctors/me/availability/{id}` | DOCTOR |
| GET | `/api/admin/users` | ADMIN |
| PUT | `/api/admin/users/{id}/enable` \| `/disable` | ADMIN |

## Out of scope for Release 1

Payment, appointment booking, video consultation, prescriptions,
consultation history, and third-party leave integration are intentionally
not implemented — see the Application Scope Agreement document.
