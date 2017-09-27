FROM openjdk:alpine

WORKDIR /bus-container

COPY /target/bus-container.jar .

ENV logging.pattern.console="%-5level [%-24thread{24}] %-45logger{45} : %msg%n"

ENTRYPOINT ["/usr/bin/java"]

CMD ["-jar", "bus-container.jar"]

