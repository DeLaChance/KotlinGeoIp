package nl.geoipapp.configuration

import com.fasterxml.jackson.annotation.JsonInclude
import geoipapp.adapter.http.HttpServerVerticle
import io.vertx.config.ConfigRetriever
import io.vertx.config.ConfigRetrieverOptions
import io.vertx.config.ConfigStoreOptions
import io.vertx.core.eventbus.Message
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.core.net.JksOptions
import io.vertx.ext.shell.ShellService
import io.vertx.ext.shell.ShellServiceOptions
import io.vertx.ext.shell.command.CommandProcess
import io.vertx.ext.shell.command.CommandRegistry
import io.vertx.ext.shell.command.CommandResolver
import io.vertx.ext.shell.command.impl.CommandBuilderImpl
import io.vertx.ext.shell.term.SSHTermOptions
import io.vertx.kotlin.config.getConfigAwait
import io.vertx.kotlin.core.deployVerticleAwait
import io.vertx.kotlin.core.deploymentOptionsOf
import io.vertx.kotlin.core.file.existsAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.ext.shell.command.registerCommandAwait
import io.vertx.kotlin.ext.shell.startAwait
import io.vertx.serviceproxy.ServiceBinder
import kotlinx.coroutines.launch
import nl.geoipapp.configuration.shell.ImportDataCommandBuilder
import nl.geoipapp.configuration.shell.SSHAuthOptions
import nl.geoipapp.domain.City
import nl.geoipapp.domain.Country
import nl.geoipapp.domain.GeoIpRange
import nl.geoipapp.domain.Region
import nl.geoipapp.domain.command.ClearCountriesDataCommand
import nl.geoipapp.domain.command.ClearGeoIpRangesDataCommand
import nl.geoipapp.domain.event.CityCreatedEvent
import nl.geoipapp.domain.event.CountryCreatedEvent
import nl.geoipapp.domain.event.GeoIpRangeCreatedEvent
import nl.geoipapp.domain.event.RegionCreatedEvent
import nl.geoipapp.repository.PostgreSQLClient
import nl.geoipapp.repository.country.*
import nl.geoipapp.repository.geoiprange.*
import nl.geoipapp.service.GeoDataImporter
import nl.geoipapp.service.createGeoDataImporterDelegate
import nl.geoipapp.service.createGeoDataImporterProxy
import nl.geoipapp.util.generateAcknowledgement
import nl.geoipapp.util.generateError
import nl.geoipapp.util.getNestedInteger
import nl.geoipapp.util.getNestedString
import org.slf4j.LoggerFactory
import java.io.FileNotFoundException


class MainVerticle : CoroutineVerticle() {

  private val EVERY_5_MINUTES: Long = (5 * 60 * 1000)

  private val log = LoggerFactory.getLogger(MainVerticle::class.java)
  private lateinit var postGreSqlClient: PostgreSQLClient
  private lateinit var countryRepository: CountryRepository
  private lateinit var geoIpRangeRepository: GeoIpRangeRepository
  private lateinit var geoIpDataImporter: GeoDataImporter
  private var applicationConfig: JsonObject? = null

  // Called when verticle is deployed
  override suspend fun start() {
    Json.prettyMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
    Json.mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY)

    readConfiguration()
    setupServices()
    startEventListeners()
    startVerticles()
    setupPeriodicCacheClear()
  }

  // Optional - called when verticle is undeployed
  override suspend fun stop() {
  }

  private suspend fun readConfiguration() {
    var configPath = "conf/config.json"

    if (!vertx.fileSystem().existsAwait(configPath)) {
      log.info("Tried reading configuration from path ${configPath}, but does not exist. Defaulting to classpath.")
      configPath = "config.json"
    }

    val configStoreOptions = ConfigStoreOptions()
      .setType("file")
      .setOptional(true)
      .setConfig(JsonObject().put("path", configPath))
    log.info("Reading configuration from path ${configPath}")

    val configRetrieverOptions = ConfigRetrieverOptions()
      .addStore(configStoreOptions)
    val retriever = ConfigRetriever.create(vertx, configRetrieverOptions)
    applicationConfig = retriever.getConfigAwait()

    vertx.orCreateContext.config().clear()
    applicationConfig?.stream()?.forEach{ vertx.orCreateContext.config().put(it.key, it.value)}

    log.info("Printing startup application config:\n${vertx.orCreateContext.config().encodePrettily()}")
  }

  private suspend fun setupServices() {
    log.info("Start setting up services")

    setupPostGreSqlClient()
    setupPostGreSQLBackedCountryRepository()
    setupGeoIpRangeService()
    setupGeoIpDataImporter()
    setupShellService()

    log.info("Completed setting up services")
  }

  private fun setupPostGreSqlClient() {
    postGreSqlClient = PostgreSQLClient(vertx)
  }

  private suspend fun setupShellService() {

    val importDataCommandBuilder = ImportDataCommandBuilder(geoIpDataImporter)
    importDataCommandBuilder.processHandlerAwait(importDataCommandBuilder.generateProcessHandler())
    val importData = importDataCommandBuilder.build(vertx)

    val registry = CommandRegistry.getShared(vertx)
    registry.registerCommandAwait(importData)

    val globalConfig = vertx.orCreateContext.config()
    val sshPort = globalConfig.getNestedInteger("ssh.port", 5000)
    val sshHost = globalConfig.getNestedString("ssh.host", "localhost")
    val keyStoreLocation = globalConfig.getNestedString("ssh.keystoreLocation","conf/keystore.jks")

    if (!vertx.fileSystem().existsBlocking(keyStoreLocation)) {
      throw FileNotFoundException("Missing jks keystore at $keyStoreLocation")
    }

    val options = ShellServiceOptions().setSSHOptions(SSHTermOptions().setHost(sshHost).setPort(sshPort)
      .setAuthOptions(SSHAuthOptions())
      .setKeyPairOptions(JksOptions().setPath(keyStoreLocation).setPassword("foobar"))
    )

    log.info("Starting shell service at ${sshHost}:${sshPort}")
    val shellService = ShellService.create(vertx, options)
    val resolver = CommandResolver.baseCommands(vertx)
    resolver.commands().add(importData)
    shellService.server().registerCommandResolver(resolver)
    shellService.startAwait()
  }

  private fun setupGeoIpRangeService() {
    var delegate = create(postGreSqlClient, countryRepository)
    ServiceBinder(vertx)
      .setAddress(GEO_IPRANGE_SERVICE_EVENT_BUS_ADDRESS)
      .register(GeoIpRangeRepository::class.java, delegate)
    geoIpRangeRepository = createProxy(vertx)
  }

  private fun setupGeoIpDataImporter() {
    var delegate = createGeoDataImporterDelegate(vertx)
    ServiceBinder(vertx)
      .setAddress(EventBusAddress.GEO_DATA_IMPORTER_EVENT_BUS_ADDRESS.address)
      .register(GeoDataImporter::class.java, delegate)
    geoIpDataImporter = createGeoDataImporterProxy(vertx)
  }

  private suspend fun setupPostGreSQLBackedCountryRepository() {
    var delegate = createPostGreSqlBackedRepositoryDelegate(postGreSqlClient)
    ServiceBinder(vertx)
      .setAddress(EventBusAddress.POSTGRESQL_BACKED_COUNTRY_REPOSITORY_LISTENER_ADDRESS.address)
      .register(CountryRepository::class.java, delegate)
    countryRepository = createPostGreSQLBackedRepositoryProxy(vertx)

    countryRepository.refillCacheAwait() // Fills up the cache
  }

  private fun startEventListeners() {
    log.info("Start setting up event listeners")

    vertx.eventBus().consumer<JsonObject>(EventBusAddress.DOMAIN_EVENTS_LISTENER_ADDRESS
      .address) { eventBusMessage -> handleMessage(eventBusMessage) }

    log.info("Completing setting up event listeners")
  }

  private suspend fun startVerticles() {
    deployVerticle(HttpServerVerticle::class.java.canonicalName)
  }

  private suspend fun deployVerticle(className: String) {
    log.info("Deploying verticle ${className}")
    vertx.deployVerticleAwait(className, deploymentOptionsOf(applicationConfig))
    log.info("Deployed verticle ${className}")
  }

  private fun handleMessage(eventBusMessage: Message<JsonObject>?) {
    if (eventBusMessage != null) {
      val payload: JsonObject = eventBusMessage.body()
      val type = payload.getString("type")
      log.info("Received event of type ${type}.")

      launch(vertx.dispatcher()) {

        try {
          if (type == CountryCreatedEvent::class.simpleName) {
            val event = CountryCreatedEvent(payload)
            countryRepository.saveCountryAwait(event.country)
          } else if (type == RegionCreatedEvent::class.simpleName) {
            val event = RegionCreatedEvent(payload)
            countryRepository.addRegionToCountryAwait(event.region, event.countryIso)
          } else if (type == CityCreatedEvent::class.simpleName) {
            val event = CityCreatedEvent(payload)
            countryRepository.addCityToRegionAwait(event.region, event.city)
          } else if (type == ClearCountriesDataCommand::class.simpleName) {
            countryRepository.clearAwait()
          } else if (type == GeoIpRangeCreatedEvent::class.simpleName) {
            handleGeoIpRangeCreatedEvent(payload)
          } else if (type == ClearGeoIpRangesDataCommand::class.simpleName) {
            geoIpRangeRepository.clearAwait()
          }

          eventBusMessage.reply(generateAcknowledgement())
        } catch (e: Exception) {
          eventBusMessage.reply(generateError(e.message.orEmpty()))
        }
      }
    }
  }

  private suspend fun handleGeoIpRangeCreatedEvent(payload: JsonObject) {

    val event = GeoIpRangeCreatedEvent(payload)
    val city: City? = countryRepository.findCityByGeoIdentifierAwait(event.geoNameIdentifier)
    if (city != null && city.regionIntIdentifier != null) {
      val region: Region? = countryRepository.findRegionByIdAwait(city.regionIntIdentifier)
      if (region != null) {
        val country: Country? = countryRepository.findCountryByIdAwait(region.countryIsoCode)
        if (country != null) {
          val geoIpRange = GeoIpRange.from(event.cidrRange, region, country, city)
          geoIpRangeRepository.saveSingleAwait(geoIpRange)
        } else {
          log.error("No country found with: countryIsoCode='${region.countryIsoCode}'")
        }
      } else {
        log.error("No region found with: regionIntIdentifier='${city.regionIntIdentifier}'")
      }
    } else {
      // TODO: region without cities case
      log.error("No city found with: geoIdentifier='${event.geoNameIdentifier}'")
    }
  }

  private suspend fun setupPeriodicCacheClear() {
    vertx.setPeriodic(EVERY_5_MINUTES, { timerId ->
      launch (vertx.dispatcher()) {
        geoIpRangeRepository.refillCacheAwait()
        countryRepository.refillCacheAwait()
      }
    })
  }

  suspend fun CommandBuilderImpl.processHandlerAwait(fn: suspend (CommandProcess) -> Unit): CommandBuilderImpl {
    return processHandler { commandProcess ->
      launch(commandProcess.vertx().dispatcher()) {
        try {
          fn(commandProcess)
        } catch (e: Exception) {
          log.error("Error while handling command: ", e)
        }
      }
    }
  }

}