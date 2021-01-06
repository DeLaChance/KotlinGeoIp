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
import org.slf4j.LoggerFactory

class PostgreSQLClient(val vertx: Vertx) {

    private val log = LoggerFactory.getLogger(PostgreSQLClient::class.java)
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

    suspend fun <T> queryAwait(sql: String, rowMapper: RowMapper<T>): List<T> {
        log.info("Running query: $sql")
        return rowMapper.map(client.preparedQuery(sql).executeAwait())
    }

    suspend fun <T> querySingleAwait(sql: String, rowMapper: RowMapper<T>, parameters: List<Any?>): T? {
        log.info("Running query: $sql with $parameters")
        return rowMapper.map(client.preparedQuery(sql).executeAwait(Tuple.wrap(parameters))).firstOrNull()
    }

    suspend fun updateAwait(sql: String, parameters: List<Any?>) {
        log.info("Running query: $sql with $parameters")
        client.preparedQuery(sql).executeAwait(Tuple.wrap(parameters))
    }

}