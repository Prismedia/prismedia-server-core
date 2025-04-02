FROM gradle:7.6.1-jdk17 AS build

WORKDIR /app

# 소스 코드 복사
COPY . .

# 애플리케이션 빌드
RUN gradle build -x test --no-daemon

# 실행 이미지 생성
FROM openjdk:17-jdk-slim

WORKDIR /app

# 빌드된 jar 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 환경 변수 설정
ENV SPRING_PROFILES_ACTIVE=prod
ENV GOOGLE_CLIENT_ID=${GOOGLE_CLIENT_ID}
ENV GOOGLE_CLIENT_SECRET=${GOOGLE_CLIENT_SECRET}
ENV APP_AUTH_TOKEN_SECRET=${APP_AUTH_TOKEN_SECRET}

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
