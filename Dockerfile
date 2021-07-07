FROM java:11
VOLUME /tmp
EXPOSE 8090
ADD comlake.core-0.4.0-standalone.jar comlake.core-0.4.0-standalone.jar
ENTRYPOINT ["java","-jar","comlake.core-0.4.0-standalone.jar"]