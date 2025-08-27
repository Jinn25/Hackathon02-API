# ---- Build stage ----
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# gradle 캐시 최적화
COPY gradlew /app/gradlew
COPY gradle /app/gradle
RUN chmod +x gradlew && sed -i 's/\r$//' gradlew

# 의존성만 먼저 받기 (변경 적을 때 캐시 재사용)
COPY build.gradle settings.gradle /app/
# 멀티모듈이면 필요한 gradle 파일들 더 추가
RUN ./gradlew --no-daemon dependencies || true

# 실제 소스 복사 후 빌드
COPY . /app
RUN ./gradlew --no-daemon --stacktrace --info clean bootJar -x test

# ---- Run stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

# Railway는 PORT 환경변수로 포트를 넘겨줌 (application.yml에서 ${PORT} 사용 중이니 OK)
EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
