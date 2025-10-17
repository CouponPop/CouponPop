# 1. Build App
FROM gradle:8.5-jdk17 AS build

WORKDIR /app

# 빌드 파일들을 먼저 복사하여 의존성을 캐싱합니다.
COPY build.gradle settings.gradle /app/

# 의존성을 다운로드합니다.
RUN gradle dependencies --no-daemon

# 나머지 소스 코드를 복사합니다.
COPY src /app/src

# 애플리케이션을 빌드합니다. 테스트는 생략합니다.
RUN gradle build --no-daemon -x test


# 2. Make Docker Image
FROM amazoncorretto:17-alpine-jdk

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]