# Kotlin Geo IP App
Kotlin Geo IP App is a microservice-based application that can process geographical data (countries, regions and cities)
and answer queries mapping IP address (V4) to a country, region and/or city. 

# Technologies
- Kotlin 
- Vertx
- Liquibase
- Postgres 
- Maven

# Setup
You need to have Java 8+ and Maven installed. Furthermore you need to have a PostGres DB running at `localhost:5432`.
If you have it running on a different host and/or port, change the configuration in `conf/config.json`.

Also, you need to create a dummy keystore at `conf/keystore.jks` to satisfy the Vertx SSH server.

Example of `conf/config.json`:

```
{
  "geoData": {
    "countriesandregions": "input/countriesandregions.benelux.csv",
    "geoipranges": "input/geoipranges.eindhoven.csv"
  },
  "http": {
    "port": 5000
  },
  "ssh": {
    "port": 5001
  },

  "db" : {
    "port" : 5432,
      "host" : "<appname>.copsisody40i.eu-central-1.rds.amazonaws.com",
      "name" : "kotlingeoipapp",
      "user" : "postgres",
      "password" : "postgres"
  }

}
```

# Build
Build with `mvn clean install`

# Run
Run with `java -jar target/kotlin-geo-ip-1.0-SNAPSHOT-jar-with-dependencies.jar`

One can trigger the import via a SSH shell running:

`$ ssh vertx@localhost -p 5001`

with password `vertx`

and then run: `% importData countries` or `importData geoipranges`.

Warning: this will delete any existing data.

# API's
One can see the data at:

## Query all countries
[http://localhost:5000/api/countries/](http://localhost:5000/api/countries/)

## Query country by ISO2 code
URL: [http://localhost:5000/api/countries/NL](http://localhost:5000/api/countries/NL) 

Snippet of response:
```
{
  "isoCode2" : "NL",
  "name" : "Netherlands",
  "regions" : [ {
      "subdivision1Code" : "FR",
      "subdivision1Name" : "Friesland",
      "cities" : [ "Abbega", ... ]
    },
    {
        "subdivision1Code" : "GE",
        "subdivision1Name" : "Gelderland",
        "cities" : [ "Aalst", "Aalten", ... ]
    },
    ...
  ]
}
```

## Query by IP V4 address
URL: [http://localhost:5000/api/geoipranges/query/5.132.82.81](http://localhost:5000/api/geoipranges/query/5.132.82.81)

yields (example)

```
{
  "query" : {
    "ipAddress" : "5.132.82.81"
  },
  "country" : {
    "isoCode2" : "NL",
    "name" : "Netherlands",
    "selectedRegion" : {
      "subdivision1Code" : "NB",
      "subdivision1Name" : "North Brabant",
      "selectedCity" : "Eindhoven"
    }
  }
}
```