package nl.geoipapp.repository

import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import nl.geoipapp.domain.Country
import nl.geoipapp.domain.Region
import java.util.*

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
                if (!regionsMap.containsKey(region.stringIdentifier)) {
                    regionsMap[region.stringIdentifier] = region
                    countriesMap[isoCode]?.regions?.add(region)
                }

                val cityName: String? = row.getString("cityName")
                if (cityName != null) {
                    regionsMap[region.stringIdentifier]?.cities?.add(cityName)
                }
            }
        }

        return countriesMap.values.toList()
    }

    private fun mapRegion(row: Row): Region? {
        val id = row.getInteger("id")
        if (id == null) {
            return null
        } else {
            return Region(
                id,
                row.getString("isoCode2"),
                row.getString("subdivision1Code"),
                row.getString("subdivision1Name"),
                row.getString("subdivision2Code"),
                row.getString("subdivision2Name"),
                mutableSetOf()
            )
        }
    }

}