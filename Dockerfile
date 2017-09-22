FROM openjdk:alpine

WORKDIR /java-app

COPY /target/java-app.jar .

ENTRYPOINT ["/usr/bin/java"]

CMD ["-jar", "java-app.jar"]

