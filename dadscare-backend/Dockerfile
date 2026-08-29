# Runtime-only image — the jar is built locally/CI (mvn -DskipTests package) and copied
# in here, not compiled inside the image. Keeps the image build to a few seconds on a
# resource-constrained host (no Maven download/compile step on the VPS itself).
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY target/dadscare-backend-*.jar app.jar

EXPOSE 8090
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
