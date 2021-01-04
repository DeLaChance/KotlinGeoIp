package nl.geoipapp.service

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.eventbus.deliveryOptionsOf
import io.vertx.kotlin.core.file.existsAwait
import io.vertx.kotlin.core.file.readFileAwait
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import nl.geoipapp.configuration.EventBusAddress
import nl.geoipapp.domain.Country
import nl.geoipapp.domain.Region
import nl.geoipapp.domain.command.ClearDataCommand
import nl.geoipapp.domain.events.CountryCreatedEvent
import nl.geoipapp.domain.events.RegionCreatedEvent
import nl.geoipapp.util.getNestedString
import org.apache.commons.lang3.RegExUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.StringUtils.isNotBlank
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.FileNotFoundException
import kotlin.coroutines.CoroutineContext

private const val PRINT_JOB_STATUS_LINE_FREQUENCY = 1000
private const val ELEMENTS_PER_LINE = 14
private const val MESSAGE_SEND_TIMEOUT = 1000L

class GeoIP2GeoDataImporter(val vertx: Vertx) : GeoDataImporter, CoroutineScope {

    private val log: Logger = LoggerFactory.getLogger(GeoIP2GeoDataImporter::class.java)

    override val coroutineContext: CoroutineContext by lazy { vertx.dispatcher() }

    override fun readCountries(handler: Handler<AsyncResult<Void>>) {
        launch {
            clearExistingData()
            readCountriesAwait()
            handler.handle(Future.succeededFuture())
        }
    }

    override fun readGeoIpRanges(handler: Handler<AsyncResult<Void>>) {
        handler.handle(Future.succeededFuture())
    }

    private fun geoIpRangesFileLocation(): String {
        val fileName = vertx.orCreateContext.config().getNestedString("geoData.geoIpRanges",
            "input/geoipranges.csv")
        log.info("Geo ip ranges file name is: ${fileName}")
        return fileName
    }

    private fun countriesFileLocation(): String {
        val fileName = vertx.orCreateContext.config().getNestedString("geoData.countriesandregions",
            "input/countriesandregions.csv")
        log.info("Countries and regions file name is: ${fileName}")
        return fileName
    }

    private suspend fun clearExistingData() {
        val clearDataCommand = ClearDataCommand()
        sendPayloadToEventBus(clearDataCommand.toJson())
    }

    private suspend fun readCountriesAwait() {
        /*
         * TODO: feels stupid to do this, as this is also defined in GeoDataImporterConfig.kt, but do not see an
         * alternative while using suspend-functions like (readFileAwait) for now.
         */
        val countriesFileLocation = countriesFileLocation()
        if (!vertx.fileSystem().existsAwait(countriesFileLocation)) {
            throw FileNotFoundException("No file at location $countriesFileLocation")
        }

        val fileContentsBuffer: Buffer = vertx.fileSystem().readFileAwait(countriesFileLocation)
        val lines: List<String> = fileContentsBuffer.toString().split("\n")

        log.info("There are ${lines.size} lines in file $countriesFileLocation")

        val geoIdentifiers = mutableSetOf<String>()
        val jobStatus = JobStatus()

        log.info("Start processing input coutries and regions...")

        for (line in lines) {
            if (isValidCountriesLine(line)) {
                processCountriesAndRegionsLine(line, geoIdentifiers, jobStatus)
            }
        }

        log.info("Completed processing input coutries and regions...")
    }


    fun isValidCountriesLine(line: String?): Boolean {

        var isValid: Boolean

        if (line == null) {
            isValid = false
        } else {
            isValid = line.isNotBlank()

            val elements: List<String> = line.split(",")
            isValid = isValid && elements.size == ELEMENTS_PER_LINE

            if (isValid) {
                val geoIdentifier = elements[0]
                isValid = isValid && StringUtils.isNumeric(geoIdentifier)

                val countryIso = elements[4]
                isValid = isValid && isNotBlank(countryIso)
            }
        }

        if (!isValid) {
            log.info("Skipping line $line")
        }

        return isValid
    }

    private suspend fun processCountriesAndRegionsLine(line: String, geoIdentifiers: MutableSet<String>, jobStatus: JobStatus) {

        jobStatus.totalCount += 1
        if (jobStatus.totalCount % PRINT_JOB_STATUS_LINE_FREQUENCY == 0) {
            log.info("Update about importing countries status: $jobStatus")
        }

        val elements: List<String> = line.split(",")
        val geoIdentifier = elements[0]
        val countryIso = elements[4]
        val countryName = elements[5]
        val regionSubdivision1Code =  elements[6]
        val regionSubdivision1Name =  RegExUtils.replaceAll(elements[7], "[\"\']", "")
        val regionSubdivision2Code =  elements[8]
        val regionSubdivision2Name=  RegExUtils.replaceAll(elements[9], "[\"\']", "")
        val cityName = RegExUtils.replaceAll(elements[10], "[\"\']", "")

        if (geoIdentifier == null || geoIdentifier.isBlank() || geoIdentifiers.contains(geoIdentifier)) {
            jobStatus.skippedCount += 1
            return
        }

        geoIdentifiers.add(geoIdentifier)

        var success = false

        if (countryIso?.isNotBlank()) {
            success = true

            val newCountry = Country(countryIso, countryName, mutableSetOf())
            throwCountryCreatedEvent(newCountry)
        }

        if (regionSubdivision1Code?.isNotBlank()) {
            success = true

            val newRegion = Region(regionSubdivision1Code, regionSubdivision1Name, regionSubdivision2Code,
                regionSubdivision2Name, mutableSetOf(cityName))
            throwRegionCreatedEvent(newRegion, countryIso)
        }

        if (success) {
            jobStatus.successCount += 1
        } else {
            jobStatus.errorCount += 1
        }
    }

    private suspend fun throwCountryCreatedEvent(country: Country) {
        val eventPayload = CountryCreatedEvent(country).toJson()
        sendPayloadToEventBus(eventPayload)
    }

    private suspend fun throwRegionCreatedEvent(region: Region, countryIso: String) {
        val eventPayload = RegionCreatedEvent(region, countryIso).toJson()
        sendPayloadToEventBus(eventPayload)
    }

    private suspend fun sendPayloadToEventBus(payload: JsonObject) {

        awaitResult<Message<JsonObject>> { replyHandler ->
            val deliveryOptions = deliveryOptionsOf().setSendTimeout(MESSAGE_SEND_TIMEOUT)
            vertx.eventBus().request(EventBusAddress.DOMAIN_EVENTS_LISTENER_ADDRESS.address, payload,
                deliveryOptions, replyHandler)
        }
    }
}