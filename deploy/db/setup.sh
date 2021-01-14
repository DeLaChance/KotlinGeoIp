#!/bin/sh
set -ex

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE kotlingeoipapp;
EOSQL

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "kotlingeoipapp" <<-EOSQL
    GRANT ALL PRIVILEGES ON DATABASE kotlingeoipapp TO "$POSTGRES_USER";
    CREATE SCHEMA IF NOT EXISTS kotlingeoipapp;
EOSQL

cd /home/postgres/
ls ./