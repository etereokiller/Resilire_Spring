# Resilire Frontend — Release 1

React (Vite) single-page app covering Release 1 scope: landing/public pages,
patient and doctor registration/login, profile management, doctor
availability management, and a basic admin user list.

## Prerequisites

- Node.js 18+
- The Resilire backend running at `http://localhost:8080` (see
  `../resilire-backend/README.md`)

## Configuration

`.env` (already created, copy from `.env.example` if missing):

```
VITE_API_BASE_URL=http://localhost:8080/api
```

## Running

```bash
npm install
npm run dev
```

The app runs at `http://localhost:5173`.

## Structure

```
src/
  api/        axios client + one module per backend resource
  context/    AuthContext (login/register/logout, JWT stored in localStorage)
  components/ Navbar, Footer, ProtectedRoute (role-gated routing)
  pages/      landing/public pages, auth pages, and role-specific dashboards
```

## Out of scope for Release 1

Doctor search, appointment booking, payments, video consultation and
prescriptions are intentionally not implemented in this release.
