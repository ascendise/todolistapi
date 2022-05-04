FROM openjdk:17-alpine AS build
WORKDIR /app
COPY . .
RUN ./mvnw -Dmaven.test.skip package

FROM openjdk:17-alpine
WORKDIR /usr/bin/todolistapi/
COPY --from=build /app/target/todolistapi-1.0.0.jar ./
ENTRYPOINT ["java", "-jar", "./todolistapi-1.0.0.jar"]