# Timesheet Reporting System - Frontend

React + Next.js frontend application for the Timesheet Reporting System.

## Technology Stack

- **Framework**: Next.js 14 (App Router)
- **UI Library**: React 18
- **Language**: TypeScript
- **Design System**: California State Template (CA.gov)
- **HTTP Client**: Axios
- **State Management**: React Query + Context API
- **Internationalization**: react-i18next

## Getting Started

### Prerequisites

- Node.js 18+ 
- npm or yarn
- Spring Boot backend running on `http://localhost:8080`

### Installation

```bash
# Install dependencies
npm install

# Run development server
npm run dev
```

The application will be available at `http://localhost:3000`

### Build for Production

```bash
npm run build
npm start
```

## Project Structure

```
timesheet-frontend/
├── app/                    # Next.js App Router pages
│   ├── layout.tsx         # Root layout
│   ├── page.tsx           # Home page
│   ├── login/             # Login page
│   ├── dashboard/         # Dashboard page
│   ├── batch-jobs/        # Batch jobs page
│   └── admin/             # Admin pages
├── components/            # React components
│   ├── structure/         # Header, Footer, Navigation
│   └── patterns/          # Banner, CardGrid, ProgressTracker
├── lib/
│   ├── services/          # API service layer
│   ├── contexts/          # React contexts
│   ├── stores/            # State stores
│   └── i18n/              # Internationalization
├── public/                # Static assets
│   └── cagov/             # CA.gov CSS/JS files
└── styles/                # Global styles
```

## Backend Integration

The frontend connects to the Spring Boot backend via API proxy configured in `next.config.js`.

All API calls are prefixed with `/api` and automatically proxied to `http://localhost:8080/api`.

## Features

- ✅ Authentication with Keycloak
- ✅ Role-based access control
- ✅ Real-time report generation
- ✅ Batch job management
- ✅ Field masking configuration
- ✅ Analytics dashboard
- ✅ Multi-language support
- ✅ California State Template design system

## Docker Deployment

### 1. Build the image

```bash
docker build -t timesheet-frontend .
```

### 2. Create a shared Docker network (only once)

```bash
docker network create timesheet-network
```

### 3. Run the container and connect it to the backend network

```bash
docker run --rm \
  --name timesheet-frontend \
  -e NEXT_PUBLIC_API_URL=http://backend:8080 \
  -p 3000:3000 \
  --network timesheet-network \
  timesheet-frontend
```

- Replace `http://backend:8080` with the DNS name of your backend container.
- Ensure the backend container is attached to the same `timesheet-network`.
- Pass additional env vars with `-e KEY=value` or `--env-file .env.local` if you want to reuse the local file.

### docker-compose (optional)

```bash
docker compose -f docker-compose.frontend.yml up --build
```

- Update `docker-compose.frontend.yml` to match your backend service name or remove the placeholder `backend` service if you run it elsewhere.
- The compose file expects an external network named `timesheet-network`. Create it if it does not exist:

```bash
docker network create timesheet-network
```

