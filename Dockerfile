# ============================================================
# Stage 1: Build the application
# ============================================================
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /build

# Copy Maven wrapper and pom.xml first.
# This allows Docker to cache the dependency download layer
# separately from the source code layer.
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies. This layer only invalidates when pom.xml changes.
RUN ./mvnw dependency:go-offline -B

# Now copy the source code and build.
# Source changes won't invalidate the dependency layer above.
COPY src ./src

# Build the JAR. Skip tests in container build —
# tests run in CI separately.
RUN ./mvnw clean package -DskipTests

# ============================================================
# Stage 2: Runtime image
# ============================================================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Run as an unprivileged user: the app needs no root inside the container.
RUN useradd --system --uid 1001 --home /app --shell /usr/sbin/nologin app \
    && chown app:app /app

# Copy only the built JAR from the builder stage.
# This is the multi-stage magic — none of the build environment
# carries into the runtime image.
COPY --from=builder /build/target/*.jar app.jar

USER app

# Document the port the application listens on.
EXPOSE 8080

# Run the JAR. exec form so signals (like SIGTERM from docker stop)
# reach the Java process directly, not a shell wrapper.
ENTRYPOINT ["java", "-jar", "app.jar"]