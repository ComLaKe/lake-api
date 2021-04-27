From openjdk:8
copy ./target/ulake-api-0.0.1-SNAPSHOT.jar ulake-api-0.0.1-SNAPSHOT.jar
CMD ["java","-jar","ulake-api-0.0.1-SNAPSHOT.jar"]