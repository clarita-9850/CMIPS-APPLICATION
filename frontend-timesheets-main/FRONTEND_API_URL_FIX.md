# Frontend API URL Configuration Fix

## Issue
The frontend is trying to connect to `http://api-gateway:8080/api/auth/login` which is a Docker internal hostname that won't work from the browser.

## Solution

The frontend needs to use `http://localhost:8090` (the gateway's external port) when running in the browser.

### Steps to Fix:

1. **Update `.env.local` file** (if it exists):
   ```
   NEXT_PUBLIC_API_URL=http://localhost:8090
   ```
   (Change from `http://localhost:8080` to `http://localhost:8090`)

2. **If frontend is running in Docker**, make sure the environment variable is set correctly:
   ```bash
   NEXT_PUBLIC_API_URL=http://localhost:8090
   ```

3. **Restart the frontend** after making changes to pick up new environment variables

## Current Configuration

- API Gateway external port: **8090**
- API Gateway internal port: **8080**
- Browser must use: **http://localhost:8090**
- Docker containers can use: **http://api-gateway:8080** (internal only)

## Files Updated

- `lib/services/api.ts` - Now defaults to `localhost:8090`
- `lib/services/auth.service.ts` - Already defaults to `localhost:8090`
- `next.config.js` - Updated to use `localhost:8090`

## Test

After fixing, login should work with:
- URL: `http://localhost:8090/api/auth/login`
- Not: `http://api-gateway:8080/api/auth/login` ❌

