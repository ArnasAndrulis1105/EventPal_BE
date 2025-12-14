FROM eclipse-temurin:23-jdk AS build
WORKDIR /app

COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle ./
COPY settings.gradle ./
# (no gradle.properties unless you actually have it)

RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

COPY src ./src
RUN ./gradlew --no-daemon clean bootJar -x test


# ---- run stage ----
FROM eclipse-temurin:23-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]