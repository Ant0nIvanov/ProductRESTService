FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/product-service-1.0.0.jar /app/product-service.jar
ENTRYPOINT ["java", "-jar", "product-service.jar"]