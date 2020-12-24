package nl.geoipapp.service

import io.vertx.codegen.annotations.ProxyGen
import io.vertx.codegen.annotations.VertxGen
import io.vertx.core.AsyncResult
import io.vertx.core.Handler

@ProxyGen
@VertxGen
interface GeoDataImporter {

    fun readGeoIpRanges(handler: Handler<AsyncResult<Void>>)
    fun readCountries(handler: Handler<AsyncResult<Void>>)
}