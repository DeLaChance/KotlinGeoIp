#/bin/sh
DB_CONTAINER_ID=`sudo docker ps | egrep 'kotlingeoipapp-db:latest' | awk '{ print $1 }'`
docker exec -it ${DB_CONTAINER_ID} /bin/sh -cx 'cd /home/postgres/ && java -jar liquibase.jar --changeLogFile=db.changelog.xml \
  --username=${POSTGRES_USER}  --password=${POSTGRES_PASSWORD} --classpath=postgresql-42.2.18.jar \
  --url="jdbc:postgresql://${POSTGRES_HOST}:5432/kotlingeoipapp" update'