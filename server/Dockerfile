# 1단계: 빌드 환경
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew || true
RUN ./gradlew clean bootJar -x test || gradle clean bootJar -x test

# 2단계: 실행 환경 (가비아 클라우드 배포용 경량 이미지)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
