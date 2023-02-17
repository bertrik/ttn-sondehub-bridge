FROM adoptopenjdk/openjdk14:jre-14.0.2_12-alpine
MAINTAINER Bertrik Sikken bertrik@gmail.com

ADD ttn-sondehub-bridge/build/distributions/ttn-sondehub-bridge.tar /opt/

WORKDIR /opt/ttn-sondehub-bridge
ENTRYPOINT /opt/ttn-sondehub-bridge/bin/ttn-sondehub-bridge

