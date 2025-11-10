# Docker Testing Checklist for StudyBuddy

## Prerequisites

Before starting, ensure you have:
- [ ] Docker Desktop installed and running
- [ ] Git repository cloned to your local machine
- [ ] Terminal/Command Prompt open

---

## Step 1: Build and Start Services

### Commands:
```bash
cd /path/to/todo_list_k8s/docker
docker compose up --build
```

### Expected Output:
```
✓ Creating network "docker_studybuddy-network"
✓ Creating volume "docker_postgres_data"
✓ Building api...
✓ [+] Building 120.0s (15/15) FINISHED
✓ Container studybuddy-db - Created
✓ Container studybuddy-api - Created
✓ Container studybuddy-db - Healthy
✓ Container studybuddy-api - Started
```

### What to Watch For:

**PostgreSQL Startup:**
```
studybuddy-db   | database system is ready to accept connections
studybuddy-db   | PostgreSQL init process complete; ready for start up
```

**Spring Boot Startup:**
```
studybuddy-api  | Flyway: Successfully validated 1 migration
studybuddy-api  | Flyway: Successfully applied 1 migration to schema "public"
studybuddy-api  | Started StudyBuddyApplication in X.XXX seconds
```

### Timing:
- First build: 2-5 minutes (downloads dependencies)
- Subsequent builds: 30-60 seconds (uses cache)

**✅ CHECKPOINT:** Leave this terminal running. Open a NEW terminal for testing.

---

## Step 2: Verify Containers Are Running

### Command:
```bash
# In NEW terminal
cd /path/to/todo_list_k8s/docker
docker compose ps
```

### Expected Output:
```
NAME                IMAGE               STATUS              PORTS
studybuddy-api      studybuddy:0.1.0    Up 30 seconds       0.0.0.0:8080->8080/tcp
studybuddy-db       postgres:15-alpine  Up 45 seconds       0.0.0.0:5432->5432/tcp
```

**✅ CHECKPOINT:** Both containers should show "Up" status.

### Troubleshooting:
If status shows "Restarting" or "Exited":
```bash
# Check logs
docker compose logs api
docker compose logs db
```

---

## Step 3: Test Health Endpoint

### Command:
```bash
curl http://localhost:8080/actuator/health
```

### Expected Output:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP"
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

**✅ CHECKPOINT:** Status should be "UP" and db component should be "UP".

### Alternative (if curl not available):
Open browser: http://localhost:8080/actuator/health

---

## Step 4: Test API - Create Profile

### Command:
```bash
curl -X POST http://localhost:8080/api/v1/profiles \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","email":"john@example.com"}'
```

### Expected Output:
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

**✅ CHECKPOINT:** Should return profile with id=1.

### Troubleshooting:
- **400 Bad Request:** Check JSON syntax
- **500 Internal Server Error:** Check database connection
- **Connection refused:** App not started yet (wait 30 seconds)

---

## Step 5: Test API - Get Profile

### Command:
```bash
curl http://localhost:8080/api/v1/profiles/1
```

### Expected Output:
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

**✅ CHECKPOINT:** Should return the profile we just created.

---

## Step 6: Test API - Create Subject

### Command:
```bash
curl -X POST http://localhost:8080/api/v1/subjects \
  -H "Content-Type: application/json" \
  -d '{"profileId":1,"name":"Mathematics","description":"Math course"}'
```

### Expected Output:
```json
{
  "id": 1,
  "profileId": 1,
  "name": "Mathematics",
  "description": "Math course",
  "createdAt": "2024-01-15T10:31:00Z",
  "updatedAt": "2024-01-15T10:31:00Z"
}
```

**✅ CHECKPOINT:** Should return subject with id=1.

---

## Step 7: Test API - List Subjects

### Command:
```bash
curl http://localhost:8080/api/v1/subjects?profileId=1
```

### Expected Output:
```json
[
  {
    "id": 1,
    "profileId": 1,
    "name": "Mathematics",
    "description": "Math course",
    "createdAt": "2024-01-15T10:31:00Z",
    "updatedAt": "2024-01-15T10:31:00Z"
  }
]
```

**✅ CHECKPOINT:** Should return array with one subject.

---

## Step 8: Test API - Create Plan Item

### Command:
```bash
curl -X POST http://localhost:8080/api/v1/plan-items \
  -H "Content-Type: application/json" \
  -d '{
    "profileId": 1,
    "subjectId": 1,
    "title": "Complete Chapter 5",
    "targetMinutes": 120,
    "deadline": "2024-12-31T23:59:59Z"
  }'
```

### Expected Output:
```json
{
  "id": 1,
  "profileId": 1,
  "subjectId": 1,
  "subjectName": "Mathematics",
  "title": "Complete Chapter 5",
  "targetMinutes": 120,
  "deadline": "2024-12-31T23:59:59Z",
  "status": "OPEN",
  "createdAt": "...",
  "updatedAt": "..."
}
```

**✅ CHECKPOINT:** Should return plan item with status="OPEN".

---

## Step 9: Test API - Create Session Log

### Command:
```bash
curl -X POST http://localhost:8080/api/v1/session-logs \
  -H "Content-Type: application/json" \
  -d '{
    "profileId": 1,
    "subjectId": 1,
    "planItemId": 1,
    "durationMinutes": 90,
    "notes": "Completed exercises 1-10"
  }'
```

### Expected Output:
```json
{
  "id": 1,
  "profileId": 1,
  "subjectId": 1,
  "subjectName": "Mathematics",
  "planItemId": 1,
  "planItemTitle": "Complete Chapter 5",
  "studiedAt": "2024-01-15T10:35:00Z",
  "durationMinutes": 90,
  "notes": "Completed exercises 1-10",
  "createdAt": "...",
  "updatedAt": "..."
}
```

**✅ CHECKPOINT:** Should return session log with id=1.

---

## Step 10: Test API - Dashboard Summary

### Command:
```bash
curl http://localhost:8080/api/v1/dashboard/summary?profileId=1
```

### Expected Output:
```json
{
  "totalStudyMinutes": 90,
  "totalStudyHours": 1.5,
  "totalSessions": 1,
  "openPlanItemsCount": 1,
  "donePlanItemsCount": 0,
  "subjectStats": [
    {
      "subjectId": 1,
      "subjectName": "Mathematics",
      "totalMinutes": 90,
      "totalHours": 1.5,
      "sessionCount": 1
    }
  ]
}
```

**✅ CHECKPOINT:** Should show statistics for our test data.

---

## Step 11: Test Database Connection

### Command:
```bash
docker compose exec db psql -U studybuddy -d studybuddy -c "\dt"
```

### Expected Output:
```
              List of relations
 Schema |         Name          | Type  |   Owner
--------+-----------------------+-------+------------
 public | flyway_schema_history | table | studybuddy
 public | plan_items            | table | studybuddy
 public | profiles              | table | studybuddy
 public | reminder_rules        | table | studybuddy
 public | session_logs          | table | studybuddy
 public | subjects              | table | studybuddy
(6 rows)
```

**✅ CHECKPOINT:** All 6 tables should exist.

### Check Data:
```bash
docker compose exec db psql -U studybuddy -d studybuddy -c "SELECT * FROM profiles;"
```

Should show the profile we created.

---

## Step 12: View Logs

### Commands:
```bash
# View all logs
docker compose logs

# Follow logs (real-time)
docker compose logs -f

# Specific service logs
docker compose logs api
docker compose logs db

# Last 50 lines
docker compose logs --tail=50 api
```

**✅ CHECKPOINT:** No errors in logs.

---

## Step 13: Test Container Health

### Command:
```bash
docker inspect studybuddy-api --format='{{.State.Health.Status}}'
docker inspect studybuddy-db --format='{{.State.Health.Status}}'
```

### Expected Output:
```
healthy
healthy
```

**✅ CHECKPOINT:** Both containers should be "healthy".

---

## Step 14: Test Restart Behavior

### Commands:
```bash
# Stop API container
docker compose stop api

# Check if it auto-restarts
docker compose ps

# Manually start it
docker compose start api

# Wait 30 seconds
sleep 30

# Test health again
curl http://localhost:8080/actuator/health
```

**✅ CHECKPOINT:** API should restart and be accessible.

---

## Step 15: Check Resource Usage

### Command:
```bash
docker compose stats
```

### Expected Output:
```
NAME             CPU %   MEM USAGE / LIMIT    MEM %   NET I/O
studybuddy-api   0.50%   300MiB / 2GiB       15%     1kB / 2kB
studybuddy-db    0.10%   50MiB / 2GiB        2.5%    1kB / 1kB
```

**✅ CHECKPOINT:** Reasonable CPU and memory usage.

---

## Step 16: Clean Restart Test

### Commands:
```bash
# Stop and remove containers (keep volumes)
docker compose down

# Verify containers are gone
docker compose ps

# Start again
docker compose up -d

# Wait for startup
sleep 30

# Test API
curl http://localhost:8080/api/v1/profiles/1
```

**✅ CHECKPOINT:** Data should persist (profile still exists).

---

## Step 17: Full Reset Test (Optional)

**⚠️ WARNING: This deletes all data!**

### Commands:
```bash
# Stop and remove everything (including volumes)
docker compose down -v

# Start fresh
docker compose up -d

# Wait for startup
sleep 30

# Try to get profile (should fail - 404)
curl http://localhost:8080/api/v1/profiles/1

# Create new profile
curl -X POST http://localhost:8080/api/v1/profiles \
  -H "Content-Type: application/json" \
  -d '{"name":"New Start","email":"new@example.com"}'
```

**✅ CHECKPOINT:** Old data gone, can create new data.

---

## Final Checklist

- [ ] Containers start successfully
- [ ] Health endpoint returns UP
- [ ] Can create profile
- [ ] Can get profile by ID
- [ ] Can create subject
- [ ] Can list subjects
- [ ] Can create plan item
- [ ] Can create session log
- [ ] Dashboard shows correct statistics
- [ ] Database has all tables
- [ ] Flyway migrations applied
- [ ] No errors in logs
- [ ] Containers are healthy
- [ ] Data persists after restart
- [ ] Resource usage is reasonable

---

## Stopping Services

### For Development (keep data):
```bash
docker compose stop
```

### To Resume:
```bash
docker compose start
```

### Complete Cleanup (delete data):
```bash
docker compose down -v
```

---

## Common Issues & Solutions

### Issue: Port 8080 already in use
**Solution:**
```bash
# Find what's using port 8080
lsof -i :8080  # Mac/Linux
netstat -ano | findstr :8080  # Windows

# Kill the process or change port in docker-compose.yml:
ports:
  - "8081:8080"
```

### Issue: Connection refused
**Solution:** Wait longer, Spring Boot takes 30-60 seconds to start

### Issue: Flyway migration error
**Solution:** Reset database
```bash
docker compose down -v
docker compose up
```

### Issue: Out of disk space
**Solution:**
```bash
docker system prune -a --volumes
```

### Issue: Build fails
**Solution:**
```bash
# Clear Docker cache
docker compose build --no-cache

# Check logs
docker compose logs api
```

---

## Performance Benchmarks

### Expected Timings:
- First build: 2-5 minutes
- Subsequent builds: 30-60 seconds
- Startup time: 30-60 seconds
- API response time: <100ms
- Health check: <50ms

---

## Next Steps After Testing

Once all tests pass:
1. Stop services: `docker compose down`
2. Commit any changes: `git add . && git commit -m "Verified Docker setup"`
3. Ready for Kubernetes deployment!

---

## Need Help?

If something doesn't work:
1. Check the logs: `docker compose logs -f`
2. Check container status: `docker compose ps`
3. Verify Docker is running: `docker version`
4. Restart services: `docker compose restart`
5. Full reset: `docker compose down -v && docker compose up --build`
