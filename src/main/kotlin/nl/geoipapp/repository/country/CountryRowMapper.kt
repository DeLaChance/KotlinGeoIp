package nl.geoipapp.repository.country

import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import nl.geoipapp.domain.City
import nl.geoipapp.domain.Country
import nl.geoipapp.domain.Region
import nl.geoipapp.repository.RowMapper

class CountryRowMapper : RowMapper<Country> {

    override fun map(rows: RowSet<Row>): List<Country> {

        val countriesMap: MutableMap<String, Country> = mutableMapOf()
        val regionsMap: MutableMap<String, Region> = mutableMapOf()

        for (row in rows) {
            val isoCode = row.getString("isoCode2")

            if (!countriesMap.containsKey(isoCode)) {
                val country = Country(isoCode, row.getString("name"), mutableSetOf())
                countriesMap[isoCode] = country
            }

            val region = mapRegion(row)
            if (region != null) {
                if (!regionsMap.containsKey(region.geoIdentifier)) {
                    regionsMap[region.geoIdentifier] = region
                    countriesMap[isoCode]?.regions?.add(region)
                }

                val city = mapCity(row)
                if (city != null) {
                    regionsMap[region.geoIdentifier]?.cities?.add(city)
                }
            }
        }

        return countriesMap.values.toList()
    }

    private fun mapRegion(row: Row): Region? {
        val regionIntIdentifier = row.getInteger("regionIdentifier")
        if (regionIntIdentifier == null) {
            return null
        } else {
            return Region(
                regionIntIdentifier,
                row.getString("regionGeoIdentifier"),
                row.getString("isoCode2"),
                row.getString("subdivision1Code"),
                row.getString("subdivision1Name"),
                row.getString("subdivision2Code"),
                row.getString("subdivision2Name"),
                mutableListOf()
            )
        }
    }

    private fun mapCity(row:Row): City? {

        val cityGeoIdentifier: String? = row.getString("cityGeoIdentifier")
        if (cityGeoIdentifier == null) {
            return null
        } else {
            val cityName: String = row.getString("cityName")
            val regionIntIdentifier = row.getInteger("regionIntIdentifier")

            return City(
                intIdentifier = row.getInteger("cityIdentifier"),
                geoNameIdentifier = cityGeoIdentifier,
                cityName = cityName,
                regionIntIdentifier = regionIntIdentifier
            )
        }
    }

}