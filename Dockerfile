# 미리 빌드된 애플리케이션을 실행할 최종 이미지를 만듭니다.
FROM amazoncorretto:17-alpine-jdk

WORKDIR /app

COPY build/libs/*.jar app.jar

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

RUN mkdir /config && chown appuser:appgroup /config

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]