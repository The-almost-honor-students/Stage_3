FROM maven:3.8.5-openjdk-17 AS dependencies
LABEL authors="The-almost-honor-students"
WORKDIR /app
COPY pom.xml pom.xml
RUN mvn -B -e org.apache.maven.plugins:maven-dependency-plugin:3.1.2:go-offline -DexcludeArtifactIds=search

FROM maven:3.8.5-openjdk-17 AS builder

WORKDIR /app
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar
COPY --from=dependencies /root/.m2 /root/.m2
COPY --from=dependencies /app/ /app
COPY pom.xml pom.xml
COPY src /app/src
RUN mvn -B -e clean install -DskipTests

FROM openjdk:17.0.2-jdk-slim

WORKDIR /app
COPY --from=builder /app/target/search-app.jar /app.jar
COPY --from=builder /app/opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar
ENV JAVA_TOOL_OPTIONS="-XX:-UseContainerSupport -javaagent:/app/opentelemetry-javaagent.jar"

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]