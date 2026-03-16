# ==========================================
# STAGE 1: Build the application
# ==========================================
# 1. Start with a Linux machine that has Maven and Java 21 installed
FROM maven:3.9.6-eclipse-temurin-21 AS build

# 2. Set the working folder inside the Linux machine
WORKDIR /app

# 3. Copy your pom.xml and source code into the Linux machine
COPY pom.xml .
COPY src ./src

# 4. Run Maven to compile the code and build the .jar file (Skip tests to save time)
RUN mvn clean package -DskipTests

# ==========================================
# STAGE 2: Run the application
# ==========================================
# 5. Start fresh with a TINY Linux machine that ONLY has Java 21 (No Maven, saves RAM!)
FROM eclipse-temurin:21-jre-jammy

# 6. Set the working folder
WORKDIR /app

# 7. Copy ONLY the finished .jar file from Stage 1 into this new, tiny machine
COPY --from=build /app/target/*.jar app.jar

# 8. Expose port 8080 so the internet can talk to it
EXPOSE 8080

# 9. The command that actually starts your Spring Boot server!
ENTRYPOINT ["java", "-jar", "app.jar"]
