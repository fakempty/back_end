FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/back_end1-1.0-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]