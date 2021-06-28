# lake-api
API for Ulake

# Prerequisites
- Java
- JDK 
- Maven 
- MySQL 

With Comlake Core: 
- IPFS 
- PostgreSQL 

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

## Set up MySQL database
Run following SQL insert statements

```
INSERT INTO clake_roles(name) VALUES('ROLE_USER');
INSERT INTO clake_roles(name) VALUES('ROLE_ADMIN');
```

Open and run `src/main/resources/acl-schema.sql` and `src/main/resources/acl-data.sql`

## Get an executable contains all the resources and dependencies Jar file
```
mvn clean install
```
