FROM openjdk:alpine

WORKDIR /java-app

COPY /target/bus-container.jar .

ENTRYPOINT ["/usr/bin/java"]

CMD ["-jar", "bus-container.jar"]

