FROM openjdk:17-alpine AS build
WORKDIR /app
COPY . .
RUN ./mvnw -Dmaven.test.skip package

FROM openjdk:17-alpine
WORKDIR /usr/bin/todolistapi/
COPY --from=build /app/target/todolistapi.jar ./
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
ENTRYPOINT ["java", "-jar", "./todolistapi.jar"]