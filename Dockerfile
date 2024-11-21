FROM eclipse-temurin:17.0.12_7-jre-alpine

LABEL maintainer="Bertrik Sikken bertrik@gmail.com"
LABEL org.opencontainers.image.source="https://github.com/bertrik/ttn-sondehub-bridge"
LABEL org.opencontainers.image.licenses="MIT"

ADD ttn-sondehub-bridge/build/distributions/ttn-sondehub-bridge.tar /opt/

WORKDIR /opt/ttn-sondehub-bridge
ENTRYPOINT ["/opt/ttn-sondehub-bridge/bin/ttn-sondehub-bridge"]

