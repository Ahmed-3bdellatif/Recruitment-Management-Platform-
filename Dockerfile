# Stage 1: Build Stage
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copy maven project files for dependency layer caching
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Fetch dependencies in offline mode to maximize layer caching
RUN mvn dependency:go-offline -B || true

# Copy application source and build JAR
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Production Runtime Stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root system user for security best practices
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Create directories with proper user ownership
RUN mkdir -p /app/uploads && chown -R appuser:appgroup /app

# Copy compiled jar from build stage
COPY --from=builder /build/target/*.jar app.jar

# Switch to non-privileged user
USER appuser

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
