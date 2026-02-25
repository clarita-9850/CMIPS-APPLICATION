# Build stage - Create React App
FROM node:20-alpine AS builder

WORKDIR /app

# Install dependencies
COPY package*.json ./
RUN npm ci

# Copy source and build
COPY . .

# Build args — baked into JS at build time
ARG REACT_APP_API_BASE_URL=http://localhost:8090
ARG REACT_APP_DISABLE_AUTH=true
ARG REACT_APP_KEYCLOAK_URL=http://localhost:8080
ARG REACT_APP_KEYCLOAK_REALM=cmips
ARG REACT_APP_KEYCLOAK_CLIENT_ID=cmips-frontend

ENV REACT_APP_API_BASE_URL=$REACT_APP_API_BASE_URL
ENV REACT_APP_DISABLE_AUTH=$REACT_APP_DISABLE_AUTH
ENV REACT_APP_KEYCLOAK_URL=$REACT_APP_KEYCLOAK_URL
ENV REACT_APP_KEYCLOAK_REALM=$REACT_APP_KEYCLOAK_REALM
ENV REACT_APP_KEYCLOAK_CLIENT_ID=$REACT_APP_KEYCLOAK_CLIENT_ID
ENV GENERATE_SOURCEMAP=false

RUN npm run build

# Production stage - nginx serve static files
FROM nginx:alpine

COPY --from=builder /app/build /usr/share/nginx/html

# SPA routing + cache headers
RUN echo 'server { \
  listen 3000; \
  root /usr/share/nginx/html; \
  index index.html; \
  location /static/ { \
    expires 1y; \
    add_header Cache-Control "public, immutable"; \
  } \
  location / { \
    add_header Cache-Control "no-cache, no-store, must-revalidate"; \
    add_header Pragma "no-cache"; \
    try_files $uri $uri/ /index.html; \
  } \
}' > /etc/nginx/conf.d/default.conf

EXPOSE 3000

CMD ["nginx", "-g", "daemon off;"]
