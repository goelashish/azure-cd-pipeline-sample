FROM openjdk:8-jdk

ADD demoapp.jar /opt/

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/opt/demoapp.jar"]
 
