FROM openjdk:17-slim

ENV TZ=Europe/Oslo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

VOLUME /tmp
COPY /target/terms-and-conditions.jar app.jar

CMD java -jar $JAVA_OPTS app.jar
