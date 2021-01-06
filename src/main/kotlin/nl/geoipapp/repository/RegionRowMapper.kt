package nl.geoipapp.repository

import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import nl.geoipapp.domain.Region


class RegionRowMapper : RowMapper<Region> {

    override fun map(rows: RowSet<Row>): List<Region> {
        return rows.map{ row -> mapRegion(row) }
    }

    private fun mapRegion(row: Row): Region {
        val id = row.getInteger("id")
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
