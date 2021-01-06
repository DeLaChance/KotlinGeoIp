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
import nl.geoipapp.domain.command.ClearDataCommand
import nl.geoipapp.domain.events.CityCreatedEvent
import nl.geoipapp.domain.events.CountryCreatedEvent
import nl.geoipapp.domain.events.RegionCreatedEvent
import nl.geoipapp.repository.*
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

  private val LOG = LoggerFactory.getLogger(MainVerticle::class.java)
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

  private suspend fun setupServices() {
    LOG.info("Start setting up services")

    setupPostGreSqlClient()
    setupGeoIpRangeService()
    setupPostGreSQLBackedCountryRepository()
    setupGeoIpDataImporter()
    setupShellService()

    LOG.info("Completed setting up services")
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

    LOG.info("Starting shell service at ${sshHost}:${sshPort}")
    val shellService = ShellService.create(vertx, options)
    val resolver = CommandResolver.baseCommands(vertx)
    resolver.commands().add(importData)
    shellService.server().registerCommandResolver(resolver)
    shellService.startAwait()
  }

  private fun setupGeoIpRangeService() {
    var delegate = create(vertx)
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

    countryRepository.findAllCountriesAwait() // Fills up the cache
  }

  private fun startEventListeners() {
    LOG.info("Start setting up event listeners")

    vertx.eventBus().consumer<JsonObject>(EventBusAddress.DOMAIN_EVENTS_LISTENER_ADDRESS
      .address) { eventBusMessage -> handleMessage(eventBusMessage) }

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
      LOG.info("Received event of type ${type}.")

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
          } else if (type == ClearDataCommand::class.simpleName) {
            countryRepository.clearAwait()
          }

          eventBusMessage.reply(generateAcknowledgement())
        } catch (e: Exception) {
          eventBusMessage.reply(generateError(e.message.orEmpty()))
        }
      }
    }
  }

  suspend fun CommandBuilderImpl.processHandlerAwait(fn: suspend (CommandProcess) -> Unit): CommandBuilderImpl {
    return processHandler { commandProcess ->
      launch(commandProcess.vertx().dispatcher()) {
        try {
          fn(commandProcess)
        } catch (e: Exception) {
          LOG.error("Error while handling command: ", e)
        }
      }
    }
  }

}