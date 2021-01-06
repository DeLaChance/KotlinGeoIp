package nl.geoipapp.repository

import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet

interface RowMapper<T> {

    fun map(rows: RowSet<Row>): List<T>
}
