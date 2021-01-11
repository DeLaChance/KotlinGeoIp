package nl.geoipapp.repository.geoiprange

import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import nl.geoipapp.domain.GeoIpRange
import nl.geoipapp.repository.RowMapper

class GeoIpRangeRowMapper : RowMapper<GeoIpRange> {

    override fun map(rows: RowSet<Row>): List<GeoIpRange> = rows.map{ mapGeoIpRange(it) }.filterNotNull().toList()

    private fun mapGeoIpRange(row: Row): GeoIpRange? {

        val geoIpRange: GeoIpRange?
        if (row == null) {
            geoIpRange = null
        } else {
            geoIpRange = GeoIpRange(
                id = row.getInteger("id"),
                beginIpNumeric = row.getInteger("beginIpNumeric"),
                endIpNumeric = row.getInteger("endIpNumeric"),
                beginIp = row.getString("beginIp"),
                endIp = row.getString("endIp"),
                country = null,
                countryIso = row.getString("country"),
                region = null,
                regionGeoIdentifier = row.getString("region"),
                city = null,
                cityGeoIdentifier = row.getString("city"),
                priority = row.getInteger("priority")
            )
        }

        return geoIpRange
    }

}