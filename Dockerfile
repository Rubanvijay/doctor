FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY . .

# Make mvnw executable
RUN chmod +x mvnw

# Build the project
RUN ./mvnw -DskipTests package

# Run the jar
CMD ["java", "-jar", "target/doctor-0.0.1-SNAPSHOT.jar"]
