# syntax=docker/dockerfile:1

ARG NODE_VERSION=18-alpine

# ------------------------------
# 1) Install dependencies layer
# ------------------------------
FROM node:${NODE_VERSION} AS deps
WORKDIR /app
COPY package*.json ./
RUN npm install --include=dev

# ------------------------------
# 2) Build Next.js application
# ------------------------------
FROM node:${NODE_VERSION} AS builder
WORKDIR /app
ENV NODE_ENV=production \
    NEXT_TELEMETRY_DISABLED=1
COPY --from=deps /app/node_modules ./node_modules
COPY . .
# Default to localhost:8090 for browser access (can be overridden via build args)
ARG NEXT_PUBLIC_API_URL=http://localhost:8090

ENV NEXT_PUBLIC_API_URL=${NEXT_PUBLIC_API_URL}

RUN npm run build

# ------------------------------
# 3) Production runtime image
# ------------------------------
FROM node:${NODE_VERSION} AS runner
WORKDIR /app
ENV NODE_ENV=production \
    NEXT_TELEMETRY_DISABLED=1 \
    PORT=3000 \
    HOSTNAME=0.0.0.0

# Copy standalone server output
COPY --from=builder /app/public ./public
COPY --from=builder /app/.next/standalone ./
COPY --from=builder /app/.next/static ./.next/static

EXPOSE 3000
CMD ["node", "server.js"]

