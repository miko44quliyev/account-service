FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /build

COPY gradlew .

RUN chmod +x gradlew

COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY src ./src

RUN ./gradlew bootJar -q

FROM eclipse-temurin:17-jre-alpine AS extract
WORKDIR /extract

COPY --from=build /build/build/libs/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# Stage 3: Final runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Security: non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy layers
COPY --from=extract --chown=appuser:appgroup /extract/dependencies/ ./
COPY --from=extract --chown=appuser:appgroup /extract/spring-boot-loader/ ./
COPY --from=extract --chown=appuser:appgroup /extract/snapshot-dependencies/ ./
COPY --from=extract --chown=appuser:appgroup /extract/application/ ./

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+ExitOnOutOfMemoryError", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "org.springframework.boot.loader.launch.JarLauncher"]