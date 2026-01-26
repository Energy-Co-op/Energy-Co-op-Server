FROM azul/zulu-openjdk-debian:25.0.1-25.30
MAINTAINER em-creations.co.uk
COPY build/libs/energycoop-*.jar application.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/application.jar"]