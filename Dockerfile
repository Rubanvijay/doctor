FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY . .
RUN ./mvnw -DskipTests package
CMD ["java", "-jar", "target/doctor-0.0.1-SNAPSHOT.jar"]
