package nl.geoipapp.configuration

import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.toChannel
import nl.geoipapp.domain.GeoIpRange
import nl.geoipapp.util.ipToNumeric
import org.slf4j.LoggerFactory
import java.util.*
import io.vertx.serviceproxy.ServiceBinder
import nl.geoipapp.domain.events.CountryCreatedEvent
import nl.geoipapp.domain.events.RegionCreatedEvent
import nl.geoipapp.repository.*
import nl.geoipapp.service.*


class MainVerticle : CoroutineVerticle() {

  val LOGGER = LoggerFactory.getLogger(MainVerticle::class.java)
  var countryRepository: CountryRepository? = null

  // Called when verticle is deployed
  override suspend fun start() {

    setupServices()
    startEventListeners()
  }

  // Optional - called when verticle is undeployed
  override suspend fun stop() {
  }

  private suspend fun setupServices() {
    setupGeoIpRangeService()
    setupCountryRepository()

    startEventListeners()
  }

  private suspend fun setupGeoIpRangeService() {
    var delegate = create(vertx)
    ServiceBinder(vertx)
      .setAddress(GEO_IPRANGE_SERVICE_EVENT_BUS_ADDRESS)
      .register(GeoIpRangeService::class.java, delegate)

    var proxy = createProxy(vertx)
    proxy.saveAwait(Arrays.asList(GeoIpRange(0, ipToNumeric("0.0.0.0"), ipToNumeric("1.1.1.1"),
      "0.0.0.0", "1.1.1.1", null, null, 0)))
    val geoIpRange: GeoIpRange? = proxy.findByIpAddressAwait("0.0.0.0")
    if (geoIpRange != null) {
      LOGGER.info("${geoIpRange.beginIp} : ${geoIpRange.endIp}")
    }
  }

  private fun setupCountryRepository() {
    var delegate = createCountryRepositoryDelegate(vertx)
    ServiceBinder(vertx)
      .setAddress(EventBusAddress.COUNTRY_REPOSITORY_LISTENER_ADDRESS.address)
      .register(CountryRepository::class.java, delegate)

    countryRepository = createCountryRepositoryProxy(vertx)
  }

  private suspend fun startEventListeners() {
    var messageConsumer = vertx.eventBus().consumer<JsonObject>(EventBusAddress.DOMAIN_EVENTS_LISTENER_ADDRESS
      .address)
    val channel = messageConsumer.toChannel(vertx)

    for (eventBusMessage in channel) {
      handleMessage(eventBusMessage)
    }
  }

  private suspend fun handleMessage(eventBusMessage: Message<JsonObject>?) {
    if (eventBusMessage != null) {
      val payload: JsonObject = eventBusMessage.body()
      val type = payload.getString("type")
      LOGGER.info("Received event of type ${type} with payload ${payload}")

      if (type == CountryCreatedEvent::class.simpleName) {
        val event = CountryCreatedEvent(payload)
        countryRepository?.saveCountryAwait(event.country)
      } else if (type == RegionCreatedEvent::class.simpleName) {
        val event = RegionCreatedEvent(payload)
        countryRepository?.addRegionToCountryAwait(event.region, event.countryIso)
      }
    }
  }


}