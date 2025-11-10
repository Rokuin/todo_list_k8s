#!/bin/bash

# ==================================================
# StudyBuddy API Test Script
# ==================================================
# Automated testing for Docker deployment
# Usage: ./test-api.sh

set -e  # Exit on error

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Base URL
BASE_URL="http://localhost:8080"

# Counter for tests
PASSED=0
FAILED=0

# ==================================================
# Helper Functions
# ==================================================

print_test() {
    echo ""
    echo "========================================"
    echo "TEST: $1"
    echo "========================================"
}

print_success() {
    echo -e "${GREEN}✓ PASSED${NC}: $1"
    ((PASSED++))
}

print_error() {
    echo -e "${RED}✗ FAILED${NC}: $1"
    ((FAILED++))
}

print_warning() {
    echo -e "${YELLOW}⚠ WARNING${NC}: $1"
}

wait_for_service() {
    echo "Waiting for service to be ready..."
    local max_attempts=30
    local attempt=0

    while [ $attempt -lt $max_attempts ]; do
        if curl -s ${BASE_URL}/actuator/health > /dev/null 2>&1; then
            echo -e "${GREEN}Service is ready!${NC}"
            return 0
        fi
        echo "Attempt $((attempt+1))/$max_attempts - waiting..."
        sleep 2
        ((attempt++))
    done

    echo -e "${RED}Service failed to start after $max_attempts attempts${NC}"
    return 1
}

# ==================================================
# Pre-flight Checks
# ==================================================

print_test "Pre-flight Checks"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    print_error "Docker is not running"
    exit 1
fi
print_success "Docker is running"

# Check if containers are running
if ! docker compose ps | grep -q "Up"; then
    print_warning "Containers not running. Starting..."
    docker compose up -d
    wait_for_service
else
    print_success "Containers are running"
fi

# ==================================================
# Test 1: Health Check
# ==================================================

print_test "Health Check"

response=$(curl -s ${BASE_URL}/actuator/health)
status=$(echo $response | grep -o '"status":"UP"' || echo "")

if [ -n "$status" ]; then
    print_success "Health endpoint returned UP"
else
    print_error "Health endpoint failed"
    echo "Response: $response"
fi

# ==================================================
# Test 2: Create Profile
# ==================================================

print_test "Create Profile"

response=$(curl -s -X POST ${BASE_URL}/api/v1/profiles \
  -H "Content-Type: application/json" \
  -d '{"name":"Test User","email":"test@example.com"}')

profile_id=$(echo $response | grep -o '"id":[0-9]*' | grep -o '[0-9]*' || echo "")

if [ -n "$profile_id" ]; then
    print_success "Profile created with ID: $profile_id"
else
    print_error "Failed to create profile"
    echo "Response: $response"
    exit 1
fi

# ==================================================
# Test 3: Get Profile
# ==================================================

print_test "Get Profile"

response=$(curl -s ${BASE_URL}/api/v1/profiles/${profile_id})
email=$(echo $response | grep -o '"email":"test@example.com"' || echo "")

if [ -n "$email" ]; then
    print_success "Retrieved profile successfully"
else
    print_error "Failed to get profile"
    echo "Response: $response"
fi

# ==================================================
# Test 4: Create Subject
# ==================================================

print_test "Create Subject"

response=$(curl -s -X POST ${BASE_URL}/api/v1/subjects \
  -H "Content-Type: application/json" \
  -d "{\"profileId\":${profile_id},\"name\":\"Mathematics\",\"description\":\"Math course\"}")

subject_id=$(echo $response | grep -o '"id":[0-9]*' | grep -o '[0-9]*' | head -1 || echo "")

if [ -n "$subject_id" ]; then
    print_success "Subject created with ID: $subject_id"
else
    print_error "Failed to create subject"
    echo "Response: $response"
fi

# ==================================================
# Test 5: List Subjects
# ==================================================

print_test "List Subjects"

response=$(curl -s "${BASE_URL}/api/v1/subjects?profileId=${profile_id}")
subject_name=$(echo $response | grep -o '"name":"Mathematics"' || echo "")

if [ -n "$subject_name" ]; then
    print_success "Listed subjects successfully"
else
    print_error "Failed to list subjects"
    echo "Response: $response"
fi

# ==================================================
# Test 6: Create Plan Item
# ==================================================

print_test "Create Plan Item"

response=$(curl -s -X POST ${BASE_URL}/api/v1/plan-items \
  -H "Content-Type: application/json" \
  -d "{\"profileId\":${profile_id},\"subjectId\":${subject_id},\"title\":\"Complete Chapter 5\",\"targetMinutes\":120}")

plan_item_id=$(echo $response | grep -o '"id":[0-9]*' | grep -o '[0-9]*' | head -1 || echo "")

if [ -n "$plan_item_id" ]; then
    print_success "Plan item created with ID: $plan_item_id"
else
    print_error "Failed to create plan item"
    echo "Response: $response"
fi

# ==================================================
# Test 7: Create Session Log
# ==================================================

print_test "Create Session Log"

response=$(curl -s -X POST ${BASE_URL}/api/v1/session-logs \
  -H "Content-Type: application/json" \
  -d "{\"profileId\":${profile_id},\"subjectId\":${subject_id},\"planItemId\":${plan_item_id},\"durationMinutes\":90,\"notes\":\"Test session\"}")

session_id=$(echo $response | grep -o '"id":[0-9]*' | grep -o '[0-9]*' | head -1 || echo "")

if [ -n "$session_id" ]; then
    print_success "Session log created with ID: $session_id"
else
    print_error "Failed to create session log"
    echo "Response: $response"
fi

# ==================================================
# Test 8: Dashboard Summary
# ==================================================

print_test "Dashboard Summary"

response=$(curl -s "${BASE_URL}/api/v1/dashboard/summary?profileId=${profile_id}")
total_minutes=$(echo $response | grep -o '"totalStudyMinutes":90' || echo "")

if [ -n "$total_minutes" ]; then
    print_success "Dashboard returned correct statistics"
else
    print_error "Dashboard statistics incorrect"
    echo "Response: $response"
fi

# ==================================================
# Test 9: Mark Plan Item as Done
# ==================================================

print_test "Mark Plan Item as Done"

response=$(curl -s -X PATCH "${BASE_URL}/api/v1/plan-items/${plan_item_id}/done?profileId=${profile_id}")
status=$(echo $response | grep -o '"status":"DONE"' || echo "")

if [ -n "$status" ]; then
    print_success "Plan item marked as done"
else
    print_error "Failed to mark plan item as done"
    echo "Response: $response"
fi

# ==================================================
# Test 10: Delete Resources
# ==================================================

print_test "Delete Resources"

# Delete subject (cascades to plan items and session logs)
curl -s -X DELETE "${BASE_URL}/api/v1/subjects/${subject_id}?profileId=${profile_id}" > /dev/null

# Verify deletion - should get 404
response_code=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/api/v1/subjects/${subject_id}?profileId=${profile_id}")

if [ "$response_code" == "404" ]; then
    print_success "Subject deleted successfully"
else
    print_error "Failed to delete subject"
fi

# ==================================================
# Final Summary
# ==================================================

echo ""
echo "========================================"
echo "TEST SUMMARY"
echo "========================================"
echo -e "${GREEN}Passed: $PASSED${NC}"
echo -e "${RED}Failed: $FAILED${NC}"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}✓ All tests passed!${NC}"
    echo "Docker deployment is working correctly."
    exit 0
else
    echo -e "${RED}✗ Some tests failed!${NC}"
    echo "Check the output above for details."
    exit 1
fi
