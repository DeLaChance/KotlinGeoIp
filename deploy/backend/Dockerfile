FROM openjdk:11-jre-slim AS backend

WORKDIR /root/backend/
COPY kotlin-geo-ip-1.0-SNAPSHOT-jar-with-dependencies.jar ./

RUN mkdir -p conf/
COPY keystore.jks ./conf/
COPY config.json.template ./conf/
RUN mv conf/config.json.template conf/config.json

ENV POSTGRES_USER="postgres"
ENV POSTGRES_PASSWORD="postgres"
ENV POSTGRES_HOST="db"

RUN sed -i "s:dbHost:${POSTGRES_HOST}:g" conf/config.json
RUN sed -i "s:dbUser:${POSTGRES_USER}:g" conf/config.json
RUN sed -i "s:dbPassword:${POSTGRES_PASSWORD}:g" conf/config.json
RUN cat conf/config.json

RUN mkdir -p input/
COPY geoipranges.csv ./input/
COPY countriesandregions.csv ./input/

RUN ls ./
RUN ls ./conf/
RUN ls ./input/

EXPOSE 5000
EXPOSE 5001

CMD [ "java", "-jar", "kotlin-geo-ip-1.0-SNAPSHOT-jar-with-dependencies.jar" ]