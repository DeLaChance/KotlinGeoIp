package nl.geoipapp.repository

import io.vertx.sqlclient.Row

interface RowMapper<T> {

    fun map(row: Row): T
}
