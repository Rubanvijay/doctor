FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY . .

# Give execute permission to mvnw
RUN chmod +x mvnw

# Build the project
RUN ./mvnw -DskipTests package

# Run the jar
CMD ["java", "-jar", "target/doctor-0.0.1-SNAPSHOT.jar"]
