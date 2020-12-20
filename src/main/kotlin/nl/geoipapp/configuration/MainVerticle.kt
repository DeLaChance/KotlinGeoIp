package nl.geoipapp.configuration

import geoipapp.adapter.http.HttpServerVerticle
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.deployVerticleAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.serviceproxy.ServiceBinder
import kotlinx.coroutines.launch
import nl.geoipapp.domain.events.CountryCreatedEvent
import nl.geoipapp.domain.events.RegionCreatedEvent
import nl.geoipapp.repository.*
import nl.geoipapp.service.GEO_IPRANGE_SERVICE_EVENT_BUS_ADDRESS
import nl.geoipapp.service.GeoIpRangeService
import nl.geoipapp.service.create
import nl.geoipapp.service.createProxy
import org.slf4j.LoggerFactory
import io.vertx.config.ConfigRetriever
import io.vertx.config.ConfigRetrieverOptions
import io.vertx.config.ConfigStoreOptions
import io.vertx.kotlin.config.configRetrieverOptionsOf
import io.vertx.kotlin.config.getConfigAwait
import io.vertx.kotlin.core.deploymentOptionsOf
import io.vertx.kotlin.core.file.existsAwait
import java.lang.IllegalStateException


class MainVerticle : CoroutineVerticle() {

  val LOG = LoggerFactory.getLogger(MainVerticle::class.java)
  var countryRepository: CountryRepository? = null
  var geoIpRangeService: GeoIpRangeService? = null
  var applicationConfig: JsonObject? = null

  // Called when verticle is deployed
  override suspend fun start() {
    readConfiguration()
    setupServices()
    startEventListeners()
    startVerticles()
  }

  // Optional - called when verticle is undeployed
  override suspend fun stop() {
  }

  private suspend fun readConfiguration() {
    var configPath = "conf/config.json"

    if (!vertx.fileSystem().existsAwait(configPath)) {
      LOG.info("Tried reading configuration from path ${configPath}, but does not exist. Defaulting to classpath.")
      configPath = "config.json"
    }

    val configStoreOptions = ConfigStoreOptions()
      .setType("file")
      .setOptional(true)
      .setConfig(JsonObject().put("path", configPath))
    LOG.info("Reading configuration from path ${configPath}")

    val configRetrieverOptions = ConfigRetrieverOptions()
      .addStore(configStoreOptions)
    val retriever = ConfigRetriever.create(vertx, configRetrieverOptions)
    applicationConfig = retriever.getConfigAwait()

    LOG.info("Printing startup application config:\n${applicationConfig?.encodePrettily()}")
  }

  private fun setupServices() {
    LOG.info("Start setting up services")

    setupGeoIpRangeService()
    setupCountryRepository()

    LOG.info("Completed setting up services")
  }

  private fun setupGeoIpRangeService() {
    var delegate = create(vertx)
    ServiceBinder(vertx)
      .setAddress(GEO_IPRANGE_SERVICE_EVENT_BUS_ADDRESS)
      .register(GeoIpRangeService::class.java, delegate)
    geoIpRangeService = createProxy(vertx)
  }

  private fun setupCountryRepository() {
    var delegate = createCountryRepositoryDelegate(vertx)
    ServiceBinder(vertx)
      .setAddress(EventBusAddress.COUNTRY_REPOSITORY_LISTENER_ADDRESS.address)
      .register(CountryRepository::class.java, delegate)
    countryRepository = createCountryRepositoryProxy(vertx)
  }

  private fun startEventListeners() {
    LOG.info("Start setting up event listeners")

    vertx.eventBus().consumer<JsonObject>(EventBusAddress.DOMAIN_EVENTS_LISTENER_ADDRESS
      .address, { eventBusMessage -> handleMessage(eventBusMessage) })

    LOG.info("Completing setting up event listeners")
  }

  private suspend fun startVerticles() {
    deployVerticle(HttpServerVerticle::class.java.canonicalName)
  }

  private suspend fun deployVerticle(className: String) {
    LOG.info("Deploying verticle ${className}")
    vertx.deployVerticleAwait(className, deploymentOptionsOf(applicationConfig))
    LOG.info("Deployed verticle ${className}")
  }

  private fun handleMessage(eventBusMessage: Message<JsonObject>?) {
    if (eventBusMessage != null) {
      val payload: JsonObject = eventBusMessage.body()
      val type = payload.getString("type")
      LOG.info("Received event of type ${type} with payload ${payload}")

      launch(vertx.dispatcher()) {

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

}