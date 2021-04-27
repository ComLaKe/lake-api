# lake-api
API for Ulake

# Configure Spring Datasource, JPA, App properties
Open `src/main/resources/application.properties`

```
spring.datasource.url= jdbc:mysql://localhost:3306/ulakedb?useSSL=false
spring.datasource.username= root
spring.datasource.password= 123456
```

Edit your MySQL username and password

## Run Spring Boot application
```
mvn spring-boot:run
```

## Run following SQL insert statements

```
INSERT INTO comlake_roles(name) VALUES('ROLE_USER');
INSERT INTO comlake_roles(name) VALUES('ROLE_ADMIN');
```

