FROM adoptopenjdk/openjdk11:latest
VOLUME /tmp
EXPOSE 5000
ADD ulake-api-0.1.0-SNAPSHOT.jar ulake-api-0.1.0-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","ulake-api-0.1.0-SNAPSHOT.jar"]