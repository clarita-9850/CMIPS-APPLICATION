# CMIPS Application Reference

## Project Structure

### Frontend
- **Location**: `/Users/mythreya/Desktop/timesheet-frontend`
- **Docker Compose File**: `docker-compose.frontend.yml`
- **Container Name**: `timesheet-frontend`
- **Port**: 3000
- **Rebuild Command**:
  ```bash
  cd /Users/mythreya/Desktop/timesheet-frontend && docker-compose -f docker-compose.frontend.yml up -d --build
  ```

### Backend
- **Location**: `/Users/mythreya/Desktop/CMIPS-main 2`
- **Docker Compose File**: `docker-compose.yml`
- **Container Name**: `cmips-backend`
- **Port**: 8081 (internal), 8090 (gateway)
- **Rebuild Command**:
  ```bash
  cd "/Users/mythreya/Desktop/CMIPS-main 2" && docker-compose up -d --build cmips-backend
  ```

## All Containers (from CMIPS-main 2)
| Container | Service | Port |
|-----------|---------|------|
| cmips-backend | Spring Boot API | 8081 |
| api-gateway | API Gateway | 8090 |
| cmips-keycloak | Keycloak Auth | 8080 |
| cmips-postgres | PostgreSQL | 5432 |
| cmips-kafka | Kafka | 9092 |
| cmips-zookeeper | Zookeeper | 2181 |

## Database
- **Container**: `cmips-postgres`
- **Database**: `cmips_mvp`
- **User**: `cmips_user`
- **Query Command**:
  ```bash
  docker exec cmips-postgres psql -U cmips_user -d cmips_mvp -c "YOUR_SQL_HERE"
  ```

## Keycloak Users
| Username | Password | Role | County |
|----------|----------|------|--------|
| caseworker1 | password123 | CASE_WORKER | CT1 |
| caseworker2 | password123 | CASE_WORKER | CT2 |
| caseworker3 | password123 | CASE_WORKER | CT3 |
| caseworker4 | password123 | CASE_WORKER | CT4 |
| caseworker5 | password123 | CASE_WORKER | CT5 |
| supervisor1 | password123 | SUPERVISOR | CT1 |
| supervisor2 | password123 | SUPERVISOR | CT2 |
| supervisor3 | password123 | SUPERVISOR | CT3 |
| supervisor4 | password123 | SUPERVISOR | CT4 |
| supervisor5 | password123 | SUPERVISOR | CT5 |

## County Theme Mapping
| County Code | County Name | Theme Color |
|-------------|-------------|-------------|
| CT1 | Orange | Orange |
| CT2 | Sacramento | Green |
| CT3 | Riverside | Blue |
| CT4 | Los Angeles | Purple |
| CT5 | Alameda | Teal |

## Key API Endpoints
- Login: `POST http://localhost:8090/api/auth/login`
- Providers: `GET http://localhost:8090/api/providers?page=0&size=50`
- Recipients: `GET http://localhost:8090/api/recipients?page=0&size=50`
- Provider Search: `GET http://localhost:8090/api/providers/search?providerNumber=&firstName=&lastName=&countyCode=`
- Recipient Search: `GET http://localhost:8090/api/recipients/search?cin=&firstName=&lastName=&countyCode=&personType=`

## Performance Notes
- Providers/Recipients APIs now support pagination (default: 50 records)
- Field-level authorization removed from list endpoints for performance
- 5000+ providers, 8000+ recipients in database

## Important Backend Entities & Field Names

### ProviderEntity (providers table)
| Entity Field | DB Column | Frontend Uses |
|--------------|-----------|---------------|
| providerNumber | provider_number | providerId display |
| firstName | first_name | firstName |
| lastName | last_name | lastName |
| providerStatus | provider_status | status (ACTIVE, ON_LEAVE, TERMINATED) |
| dojCountyCode | doj_county_code | countyCode |
| primaryPhone | primary_phone | phone |
| backgroundCheckCompleted | background_check_completed | certificationStatus |
| sickLeaveAccruedHours | sick_leave_accrued_hours | sickLeaveHoursAvailable |

### RecipientEntity (recipients table)
| Entity Field | DB Column | Frontend Uses |
|--------------|-----------|---------------|
| cin | cin | cin |
| firstName | first_name | firstName |
| lastName | last_name | lastName |
| personType | person_type | type (RECIPIENT, APPLICANT, OPEN_REFERRAL, CLOSED_REFERRAL) |
| residenceCounty | residence_county | county |
| primaryPhone | primary_phone | phone |

## Common Issues & Fixes

### Frontend not connecting to backend
- Frontend MUST run in Docker container to access backend on Docker network
- Rebuild: `cd /Users/mythreya/Desktop/timesheet-frontend && docker-compose -f docker-compose.frontend.yml up -d --build`

### API returning empty objects {}
- Check `FieldLevelAuthorizationService.java` - it filters fields based on Keycloak permissions
- If no permissions configured, it should return ALL fields (fix applied)

### Login fails with 401
- Reset user password in Keycloak:
  ```bash
  docker exec cmips-keycloak /opt/keycloak/bin/kcadm.sh config credentials --server http://localhost:8080 --realm master --user admin --password admin
  docker exec cmips-keycloak /opt/keycloak/bin/kcadm.sh set-password -r cmips --username USERNAME --new-password password123
  ```

### Theme not changing based on county
- Check `ThemeContext.tsx` - theme loads based on `user.countyId` from JWT token
- County ID must be set in Keycloak user attributes

## NEVER DO
- Don't run frontend with `npm run dev` locally - it won't connect to Docker backend
- Don't guess docker-compose file locations - check container labels or this doc
- Don't waste time with unnecessary inspection commands
