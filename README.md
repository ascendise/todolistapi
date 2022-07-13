# TodolistAPI

This is a RESTful Web API for creating tasks and organizing them into checklists.

## Application.properties
When the application is first started and no application.properties are specified, a template with the most important 
properties is generated and the placeholders have to be replaced with the real values.

### Authentication

```
spring.security.oauth2.resourceserver.jwt.issuer-uri
spring.security.oauth2.resourceserver.jwt.jwk-set-uri
```

These values are needed to register the authorization-server used for authentication. The issuer uri has to match with 
the "iss"-claim of the JWT. The jwk-set-uri is where the app gets the info to validate the signature of your JWT.
It should be something along the lines of "*/.well-known/jwks.json"

### Datasource

```
spring.datasource.url
spring.datasource.username
spring.datasource.password
```
To save all the users and tasks and all of that data, you need to specify a database. This spring boot application uses 
MySQL for persistance.
You will have to specify the url of your mysql server as well as the username and password of the MySQL-User this 
application should use. I recommend creating a separate user for this application with the minimum rights needed for the 
application. The application only needs access to its own database and has to be able to create own tables and create, read, update and delete records in those tables.

The driver-class-name needed is already specified and should not be changed.

> I am not sure if there is a way to add your own driver-classes to an already built spring-boot-application. Right now, 
> you would have to add the dependency for another DBMS to the pom.xml yourself and rebuild the application. Then you 
> could specify the needed driver-class-name in the application.properties file.
> If you know any other way to setup a different DBMS, please create an issue or make a pull request with the updated 
> README.md, thank you!

### Colourful terminal

`spring.output.ansi.enabled`

The template sets up the terminal to output text with colourful highlights. If you don't want that, you can just remove 
this property

## Running the application

One way to run the application is to just use the command
```
java -jar todolistAPI-1.0.0.jar
```
### [Docker](https://hub.docker.com/r/ascendise/todolistAPI)

To run the docker container, you can run the following command

```
docker run -dp {your-port}:8080 ascendise/todolistAPI
```

You will have to specify the values from application.properties as environment variables. Specify the following values 
either directly in the command or by specifying the values in a .env-file

```
spring.security.oauth2.resourceserver.jwt.issuer-uri={your-issuer-uri}
spring.security.oauth2.resourceserver.jwt.jwkt-set-uri={your-jwkt-set-uri}
spring.jpa.hibernate.ddl-auto=update
spring.datasource.url={your-mysql-url}
spring.datasource.username={your-mysql-username}
spring.datasource.password={your-mysql-password}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.output.ansi.enabled=DETECT #Optional
```
## Cross-Origin Resource Sharing (CORS)
By default, the API allows any origin. You can specify only specific origins by setting the following property in application.properties

```
allowedOriginPatterns=https://example.com,https://example2.com
```
Make sure to not have any spaces behind commas. You can also use wildcards in the properties.

## API-Documentation
The documentation for the API is a Swagger-page located at {your-url}/swagger-ui/index.html

### Authentication
The API uses OAuth2 with JWTs to authenticate the user. The API should work fine with any authorization server adhering
to the OpenIdConnect specs.

## Troubleshooting

If you encounter any bugs or steps from this documentation don't work, then feel free to create an issue.

