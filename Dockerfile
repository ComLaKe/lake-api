From openjdk:8
EXPOSE 3306 
EXPOSE 5000 
CMD ["java","-jar","ulake-api-0.0.1-SNAPSHOT.jar"]