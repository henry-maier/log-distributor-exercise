FROM amazoncorretto:17-alpine
VOLUME /tmp
COPY build/libs/distributor-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]