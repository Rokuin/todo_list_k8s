FROM eclipse-temurin:17-jdk-alpine AS build

# ==================================================
# STAGE 1: BUILD
# ==================================================
# Purpose: Compile Java code and create JAR file
# Base image: eclipse-temurin (Adoptium OpenJDK)
# - 17-jdk: Java 17 with full JDK (needed for Maven compilation)
# - alpine: Lightweight Linux distribution (~5MB vs ~100MB)
# - AS build: Names this stage "build" for reference in next stage

WORKDIR /app

# ==================================================
# MAVEN WRAPPER AND POM.XML
# ==================================================
# Copy Maven wrapper first (for Docker layer caching)
# Docker caches each COPY/RUN as a separate layer
# If these files don't change, Docker reuses cached layer (faster builds!)

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make Maven wrapper executable
RUN chmod +x mvnw

# ==================================================
# DOWNLOAD DEPENDENCIES
# ==================================================
# Download all dependencies BEFORE copying source code
# Why? Dependencies change rarely, source code changes often
# Result: Faster rebuilds (dependencies are cached)

RUN ./mvnw dependency:go-offline -B

# -B = Batch mode (non-interactive, no progress bar)
# dependency:go-offline = Download all dependencies to local repo
# Now Maven has all JARs cached in Docker layer

# ==================================================
# COPY SOURCE CODE AND BUILD
# ==================================================
# Now copy source code (changes frequently)

COPY src src

# Build the application
RUN ./mvnw clean package -DskipTests -B

# clean: Remove old builds
# package: Compile + create JAR
# -DskipTests: Skip tests (run tests in CI/CD instead)
# -B: Batch mode

# Result: target/studybuddy-0.1.0.jar

# ==================================================
# STAGE 2: RUNTIME
# ==================================================
# Purpose: Run the application (smaller image, no build tools)
# Base image: JRE only (no JDK, no Maven)
# - 17-jre: Java Runtime Environment (smaller than JDK)
# - alpine: Lightweight (~40MB vs ~300MB with full JDK)

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# ==================================================
# CREATE NON-ROOT USER (SECURITY BEST PRACTICE)
# ==================================================
# Running as root is a security risk
# If container is compromised, attacker has root privileges
# Solution: Create unprivileged user

RUN addgroup -S spring && adduser -S spring -G spring

# addgroup -S spring: Create system group "spring"
# adduser -S spring -G spring: Create system user "spring" in group "spring"
# -S: System user (no password, no shell, UID < 1000)

# ==================================================
# COPY JAR FROM BUILD STAGE
# ==================================================
# Copy ONLY the JAR file (not source code, not Maven)
# --from=build: Copy from "build" stage
# Result: Small final image

COPY --from=build /app/target/studybuddy-0.1.0.jar app.jar

# Change ownership to spring user
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring

# ==================================================
# EXPOSE PORT
# ==================================================
# Documents which port the app listens on
# NOTE: This is DOCUMENTATION only, doesn't actually open port
# Actual port mapping happens in docker-compose.yml or docker run -p

EXPOSE 8080

# ==================================================
# HEALTHCHECK (OPTIONAL)
# ==================================================
# Docker can check if container is healthy
# If unhealthy → restart container

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# --interval=30s: Check every 30 seconds
# --timeout=3s: If check takes >3s, consider failed
# --start-period=30s: Grace period (app needs time to start)
# --retries=3: Mark unhealthy after 3 consecutive failures
# wget --spider: Check if URL is accessible (don't download)
# || exit 1: Exit with error if wget fails

# ==================================================
# RUN APPLICATION
# ==================================================
# ENTRYPOINT: The command to run (immutable)
# CMD: Arguments to ENTRYPOINT (can be overridden)

ENTRYPOINT ["java"]

# CMD provides default arguments
# -XX:MaxRAMPercentage=75.0: Use max 75% of container memory
# -Djava.security.egd=file:/dev/./urandom: Faster startup (non-blocking random)
# -jar app.jar: Run our application

CMD ["-XX:MaxRAMPercentage=75.0", \
     "-Djava.security.egd=file:/dev/./urandom", \
     "-jar", "app.jar"]

# Why separate ENTRYPOINT and CMD?
# Can override CMD in docker-compose or docker run:
# docker run myapp --spring.profiles.active=prod
# Runs: java --spring.profiles.active=prod (overrides CMD)

# ==================================================
# DOCKER BUILD COMMAND
# ==================================================
# To build this image:
# docker build -t studybuddy:0.1.0 -f docker/Dockerfile .
#
# -t studybuddy:0.1.0: Tag image as "studybuddy:0.1.0"
# -f docker/Dockerfile: Specify Dockerfile location
# .: Build context (current directory)

# ==================================================
# IMAGE SIZE COMPARISON
# ==================================================
# Without multi-stage:
# - JDK + Maven + source + target = ~500MB
#
# With multi-stage:
# - JRE + JAR only = ~140MB
#
# Savings: 360MB (72% smaller!)
# Benefits: Faster downloads, less attack surface, lower storage costs
