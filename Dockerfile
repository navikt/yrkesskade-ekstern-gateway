FROM navikt/java:11-appdynamics

ENV APPLICATION_NAME=yrkesskade-ekstern-gateway
ENV APPD_ENABLED=TRUE
ENV JAVA_OPTS="-Dhttps.protocols=TLSv1,TLSv1.1,TLSv1.2"

COPY ./target/yrkesskade-ekstern-gateway-1.0.0-SNAPSHOT.jar "app.jar"