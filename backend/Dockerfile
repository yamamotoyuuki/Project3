# ---- Build Stage ----
# Amazon Corretto 21 (Alpine ベース) でビルドする
FROM amazoncorretto:21-alpine AS builder
WORKDIR /app
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon
COPY src ./src
RUN ./gradlew bootJar --no-daemon

# ---- Runtime Stage ----
# Amazon Corretto 21 (Alpine ベース) で実行する
# Corretto は JRE 単体イメージを提供しないため JDK イメージを使用する
FROM amazoncorretto:21-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
