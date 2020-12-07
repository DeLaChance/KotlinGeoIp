package nl.geoipapp.service

import io.vertx.core.Vertx

val GEO_IPRANGE_SERVICE_EVENT_BUS_ADDRESS = "GeoIpRangeService"

fun create(vertx: Vertx): GeoIpRangeService = InMemoryGeoIpRangeService()

fun createProxy(vertx: Vertx): GeoIpRangeService = GeoIpRangeServiceVertxEBProxy(vertx, GEO_IPRANGE_SERVICE_EVENT_BUS_ADDRESS)
