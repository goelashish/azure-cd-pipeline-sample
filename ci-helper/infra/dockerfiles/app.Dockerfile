FROM openjdk:8-jre-alpine

ADD demoapp.jar /opt/

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/opt/demoapp.jar"]