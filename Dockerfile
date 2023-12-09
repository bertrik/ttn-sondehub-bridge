FROM eclipse-temurin:11.0.21_9-jre-alpine
LABEL maintainer="Bertrik Sikken bertrik@gmail.com"

ADD ttn-sondehub-bridge/build/distributions/ttn-sondehub-bridge.tar /opt/

WORKDIR /opt/ttn-sondehub-bridge
ENTRYPOINT ["/opt/ttn-sondehub-bridge/bin/ttn-sondehub-bridge"]

