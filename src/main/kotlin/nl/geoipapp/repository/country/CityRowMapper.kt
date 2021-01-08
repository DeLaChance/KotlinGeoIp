package nl.geoipapp.repository.country

import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import nl.geoipapp.domain.City
import nl.geoipapp.repository.RowMapper

class CityRowMapper : RowMapper<City> {

    override fun map(rows: RowSet<Row>): List<City> {

        val cities: MutableList<City> = mutableListOf()
        for (row in rows) {
            val city = mapCity(row)
            if (city != null) {
                cities.add(city)
            }
        }

        return cities
    }

    private fun mapCity(row:Row): City? {

        val cityGeoIdentifier: String = row.getString("cityGeoIdentifier")
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
