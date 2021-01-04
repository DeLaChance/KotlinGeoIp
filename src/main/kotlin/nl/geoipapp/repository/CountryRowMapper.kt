package nl.geoipapp.repository

import io.vertx.sqlclient.Row
import nl.geoipapp.domain.Country

class CountryRowMapper : RowMapper<Country> {

    override fun map(row: Row): Country = Country(row.getString("isoCode2"), row.getString("name"), mutableSetOf())

}