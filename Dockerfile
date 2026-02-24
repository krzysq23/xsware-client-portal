FROM gradle:8.7-jdk21 AS build
WORKDIR /home/gradle/project

COPY --chown=gradle:gradle build.gradle* settings.gradle* gradle.properties* ./
COPY --chown=gradle:gradle gradle ./gradle
RUN gradle --no-daemon dependencies || true

COPY --chown=gradle:gradle . .
RUN gradle --no-daemon clean bootJar -x test

FROM eclipse-temurin:21-jre
WORKDIR /app

RUN useradd -m appuser
USER appuser

COPY --from=build /home/gradle/project/build/libs/*.jar /app/app.jar

EXPOSE 8081
ENV SERVER_PORT=8081

ENTRYPOINT ["java","-jar","/app/app.jar"]