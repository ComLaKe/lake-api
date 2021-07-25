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
Create database ulakedb in MySQL

```
CREATE DATABASE ulakedb;
```

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

## With core
### Set up Core
- Install IPFS Desktop
- Setting up postgres account: User: postgres. Password: postgres. 
```
ALTER ROLE postgres WITH PASSWORD 'postgres';
```
- PostgreSQL: create database comlake. 
```
CREATE DATABASE comlake;
```
- Create tables: (remember to \c comlake first).
```
DROP TABLE IF EXISTS content CASCADE;
CREATE TABLE content (cid text PRIMARY KEY,
                      type text,
                      extra jsonb);
DROP TABLE IF EXISTS dataset;
CREATE TABLE dataset (id bigserial PRIMARY KEY,
                      file text REFERENCES content,
                      description text,
                      source text,
                      topics text[],
                      extra jsonb,
                      parent bigint,
                      FOREIGN KEY (parent) REFERENCES dataset(id));
```

### Running Core
Running the jar file:

```
java -jar comlake.core-0.4.0-standalone.jar
```

* Note that: core is now updated to 0.4.1

## Get an executable contains all the resources and dependencies Jar file
```
mvn clean install
```
