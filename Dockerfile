# ----------------------
# 1. Maven build stage
# ----------------------
FROM maven:3.9.6-eclipse-temurin-17 AS maven_builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# ----------------------
# 2. Layer extraction stage
# ----------------------
FROM openjdk:17-jdk-slim AS layer_extractor
WORKDIR /layers
COPY --from=maven_builder /app/target/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# ----------------------
# 3. Final runtime stage
# ----------------------
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=layer_extractor /layers/dependencies/ ./
COPY --from=layer_extractor /layers/spring-boot-loader/ ./
COPY --from=layer_extractor /layers/snapshot-dependencies/ ./
COPY --from=layer_extractor /layers/application/ ./

EXPOSE 8080
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
