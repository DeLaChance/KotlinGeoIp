FROM postgres:latest as kotlingeoipapp-db

WORKDIR /home/postgres/

RUN apt-get update -y
RUN apt-get install -y default-jre-headless

COPY setup.sh /docker-entrypoint-initdb.d/

COPY liquibase.jar ./
COPY db.changelog.xml ./
COPY postgresql-42.2.18.jar ./

ENV POSTGRES_USER="postgres"
ENV POSTGRES_PASSWORD="postgres"
ENV POSTGRES_HOST="0.0.0.0"

EXPOSE 5432