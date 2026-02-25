# Environment Setup Guide

## Local Development Setup

Create a `.env.local` file in the `timesheet-frontend` directory with the following content:

```env
# Keycloak Configuration (sajeevs-codebase-main)
NEXT_PUBLIC_KEYCLOAK_URL=http://localhost:8085
NEXT_PUBLIC_KEYCLOAK_REALM=cmips
NEXT_PUBLIC_KEYCLOAK_CLIENT_ID=cmips-app
NEXT_PUBLIC_KEYCLOAK_CLIENT_SECRET=cmips-app-secret-key-2024

# Backend API Configuration (CMIPS application)
NEXT_PUBLIC_API_URL=http://localhost:8080
```

## Docker Setup

The Docker configuration has been updated to include Keycloak environment variables. The frontend container will automatically use:
- `http://cmips-keycloak:8080` for Keycloak (Docker network)
- `http://spring-app:8080` for the backend API

## Verification

After setup, verify the configuration:

1. **Keycloak is running**: `http://localhost:8085`
2. **Backend is running**: `http://localhost:8080`
3. **Frontend is running**: `http://localhost:3000`

## Testing Login

1. Navigate to `http://localhost:3000/login`
2. Use credentials from Keycloak (e.g., `supervisor1` / `password123`)
3. Login should now work and redirect to dashboard

