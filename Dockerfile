# Use the official gradle image to create a build artifact.
# https://hub.kube.com/_/gradle
FROM gradle:8.5.0-jdk21-alpine as builder

COPY build.gradle .
COPY gradle.properties .
COPY settings.gradle .
COPY config ./config
COPY src ./src
COPY ./bumpVersion.gradle .

# Build a release artifact.
RUN gradle bootJar

# Use AdoptOpenJDK for base image.
# It's important to use OpenJDK 8u191 or above that has container support enabled.
# https://hub.kube.com/r/adoptopenjdk/openjdk8
# https://docs.kube.com/develop/develop-images/multistage-build/#use-multi-stage-builds
FROM openjdk:22-ea-21-slim

# Copy the jar to the production image from the builder stage.
COPY --from=builder /home/gradle/build/libs/*.jar /auth.jar

EXPOSE 60238

# Run the web service on container startup.
CMD [ "java", "-jar", "-Djava.security.egd=file:/dev/./urandom","/auth.jar" ]
