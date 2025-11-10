# Docker Setup for StudyBuddy

## Quick Start

```bash
# Navigate to docker directory
cd docker

# Start all services (PostgreSQL + Spring Boot)
docker compose up --build

# Access the application
# API: http://localhost:8080
# Health: http://localhost:8080/actuator/health
# Database: localhost:5432
```

## Docker Compose Commands

### Start Services

```bash
# Start in foreground (see logs)
docker compose up

# Start in background (detached)
docker compose up -d

# Rebuild and start (after code changes)
docker compose up --build
```

### View Logs

```bash
# All services
docker compose logs -f

# Specific service
docker compose logs -f api
docker compose logs -f db
```

### Stop Services

```bash
# Stop containers (keep data)
docker compose stop

# Stop and remove containers (keep volumes/data)
docker compose down

# Remove everything including data (⚠️ DATA LOSS!)
docker compose down -v
```

### Check Status

```bash
# List running containers
docker compose ps

# View resource usage
docker compose stats
```

## Accessing Services

### Spring Boot API

```bash
# Health check
curl http://localhost:8080/actuator/health

# Create profile
curl -X POST http://localhost:8080/api/v1/profiles \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","email":"john@example.com"}'
```

### PostgreSQL Database

```bash
# Connect with psql
docker compose exec db psql -U studybuddy

# From host (if psql installed)
psql -h localhost -U studybuddy -d studybuddy
# Password: studybuddy123
```

### Execute Commands in Containers

```bash
# Shell in API container
docker compose exec api sh

# Shell in DB container
docker compose exec db sh
```

## Troubleshooting

### Port Already in Use

If port 8080 or 5432 is already in use, change in `docker-compose.yml`:

```yaml
ports:
  - "8081:8080"  # Use 8081 on host
```

### Cannot Connect to Database

Check if database is healthy:

```bash
docker compose ps
docker compose logs db
```

Wait for message: "database system is ready to accept connections"

### Application Won't Start

Check application logs:

```bash
docker compose logs -f api
```

Common issues:
- Flyway migration errors → Reset database with `docker compose down -v`
- Port conflicts → Change port mapping
- Build errors → Check Java version, Maven dependencies

### Changes Not Reflected

Rebuild the image:

```bash
docker compose up --build
```

Or force rebuild:

```bash
docker compose build --no-cache
docker compose up
```

## Development Workflow

1. **Make code changes**
2. **Rebuild and restart:**
   ```bash
   docker compose up --build
   ```
3. **Test changes:**
   ```bash
   curl http://localhost:8080/api/v1/profiles
   ```
4. **View logs:**
   ```bash
   docker compose logs -f api
   ```

## Environment Variables

Override default values in `docker-compose.yml`:

```yaml
environment:
  DB_HOST: db
  DB_PORT: 5432
  DB_NAME: studybuddy
  DB_USER: studybuddy
  DB_PASSWORD: studybuddy123
```

Or use environment file:

```bash
# Create .env file
echo "DB_PASSWORD=secret123" > .env

# Use it
docker compose --env-file .env up
```

## Data Persistence

Data is stored in Docker volumes:

```bash
# List volumes
docker volume ls | grep studybuddy

# Inspect volume
docker volume inspect studybuddy_postgres_data

# Backup database
docker compose exec db pg_dump -U studybuddy studybuddy > backup.sql

# Restore database
docker compose exec -T db psql -U studybuddy studybuddy < backup.sql
```

## Clean Up

```bash
# Stop and remove containers
docker compose down

# Remove containers + volumes (⚠️ deletes data)
docker compose down -v

# Remove images
docker rmi studybuddy:0.1.0

# Remove all unused Docker resources
docker system prune -a --volumes
```

## Production Notes

**⚠️ This setup is for LOCAL DEVELOPMENT only!**

For production:
- Use Docker secrets for passwords
- Use managed database (AWS RDS, etc.)
- Add resource limits
- Use proper logging drivers
- Enable SSL/TLS
- Use reverse proxy (Nginx)
- Implement proper health checks
- Use Docker Swarm or Kubernetes
