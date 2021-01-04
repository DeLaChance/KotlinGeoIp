package nl.geoipapp.repository

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.sqlclient.executeAwait
import io.vertx.pgclient.PgConnectOptions
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.PoolOptions
import io.vertx.sqlclient.Tuple
import nl.geoipapp.util.getNestedInteger
import nl.geoipapp.util.getNestedString

class PostgreSQLClient(val vertx: Vertx) {

    private val globalConfig: JsonObject = vertx.orCreateContext.config()

    private val connectOptions: PgConnectOptions = PgConnectOptions()
        .setPort(globalConfig.getNestedInteger("db.postgres.port", 5432))
        .setHost(globalConfig.getNestedString("db.postgres.host", "localhost"))
        .setDatabase("kotlingeoipapp")
        .setUser("postgres")
        .setPassword("postgres")

    private val poolOptions: PoolOptions = PoolOptions()
        .setMaxSize(5)

    val client: PgPool = PgPool.pool(vertx, connectOptions, poolOptions)

    suspend fun <T> queryAwait(sql: String, rowMapper: RowMapper<T>): List<T> = client.preparedQuery(sql).executeAwait()
        .map{ row -> rowMapper.map(row) }

    suspend fun <T> querySingleAwait(sql: String, rowMapper: RowMapper<T>, vararg parameters: Any?): T? =
        client.preparedQuery(sql).executeAwait(Tuple.of(parameters)).map{ row -> rowMapper.map(row) }.firstOrNull()

    suspend fun updateAwait(sql: String, vararg parameters: Any?) {
        client.preparedQuery(sql).executeAwait(Tuple.of(parameters))
    }

}