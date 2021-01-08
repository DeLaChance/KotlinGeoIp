# Kotlin Geo IP App
Kotlin Geo IP App is a microservice-based application that can process geographical data (countries, regions and cities)
and answer queries mapping IP address (V4) to a country, region and/or city. 

# Technologies
- Kotlin 
- Vertx 
- Maven

# Build
Build with `mvn clean install`

# Run
Run with `java -jar target/kotlin-geo-ip-1.0-SNAPSHOT-jar-with-dependencies.jar`

One can trigger the import via a SSH shell running:

`$ ssh vertx@localhost -p 5000`

with password `vertx`

and then run: `% importData countries` or `importData geoipranges`.

# API's
One can see the data at:

[http://localhost:8081/api/countries/](http://localhost:8081/api/countries/)
[http://localhost:8081/api/countries/NL](http://localhost:8081/api/countries/NL)
[http://localhost:8081/api/geoipranges/query/217.105.36.0]([http://localhost:8081/api/geoipranges/query/217.105.36.0)