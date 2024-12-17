FROM openjdk:17-jdk-alpine

WORKDIR /app

COPY build/libs/restock-system.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]