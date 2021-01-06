package nl.geoipapp.service

import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.awaitResult
import nl.geoipapp.configuration.EventBusAddress

suspend fun GeoDataImporter.readCountriesAwait() {
    awaitResult<Void> { handler -> readCountries(handler) }
}

suspend fun GeoDataImporter.readGeoIpRangesAwait() {
    awaitResult<Void> { handler -> readGeoIpRanges(handler) }
}

fun createGeoDataImporterDelegate(vertx: Vertx): GeoDataImporter = MaxMindGeoDataImporter(vertx)

fun createGeoDataImporterProxy(vertx: Vertx): GeoDataImporter = GeoDataImporterVertxEBProxy(vertx, EventBusAddress
    .GEO_DATA_IMPORTER_EVENT_BUS_ADDRESS.address)