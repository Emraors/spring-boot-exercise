# ── Stage 1: extract Spring Boot layers ───────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine AS builder
WORKDIR /app
COPY payment-app/target/payment-app-*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# ── Stage 2: lean runtime image ───────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy layers in dependency-change-frequency order (least → most volatile)
COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/application/ ./

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
