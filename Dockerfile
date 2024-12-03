# Builder
FROM gradle:7.5.1-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/project
WORKDIR /home/gradle/project
RUN gradle build --no-daemon

# Runner
FROM openjdk:17-jdk-slim
VOLUME /tmp
COPY --from=build /home/gradle/project/build/libs/Lab1_backend-1.0.0.jar app.jar
EXPOSE 8088
CMD ["java", "-jar", "/app.jar"]
