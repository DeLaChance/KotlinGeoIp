package nl.geoipapp.service

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.eventbus.deliveryOptionsOf
import io.vertx.kotlin.core.eventbus.requestAwait
import io.vertx.kotlin.core.file.existsAwait
import io.vertx.kotlin.core.file.readFileAwait
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import nl.geoipapp.configuration.EventBusAddress
import nl.geoipapp.domain.City
import nl.geoipapp.domain.Country
import nl.geoipapp.domain.Region
import nl.geoipapp.domain.command.ClearCountriesDataCommand
import nl.geoipapp.domain.command.ClearGeoIpRangesDataCommand
import nl.geoipapp.domain.event.CityCreatedEvent
import nl.geoipapp.domain.event.CountryCreatedEvent
import nl.geoipapp.domain.event.GeoIpRangeCreatedEvent
import nl.geoipapp.domain.event.RegionCreatedEvent
import nl.geoipapp.util.getNestedString
import nl.geoipapp.util.isValidCidrIp
import org.apache.commons.lang3.RegExUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.StringUtils.isNotBlank
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.FileNotFoundException
import kotlin.coroutines.CoroutineContext

private const val PRINT_JOB_STATUS_LINE_FREQUENCY = 1000
private const val ELEMENTS_PER_LINE = 14
private const val GEO_IP_RANGE_ELEMENTS_PER_LINE = 10
private const val MESSAGE_SEND_TIMEOUT = 30_000L

class MaxMindGeoDataImporter(val vertx: Vertx) : GeoDataImporter, CoroutineScope {

    private val log: Logger = LoggerFactory.getLogger(MaxMindGeoDataImporter::class.java)

    override val coroutineContext: CoroutineContext by lazy { vertx.dispatcher() }

    override fun readCountries(handler: Handler<AsyncResult<Void>>) {
        launch {
            clearExistingData()

            val jobStatus = JobStatus()
            readCountriesAwait(ImportType.COUNTRIES, jobStatus)
            readCountriesAwait(ImportType.REGIONS, jobStatus)
            readCountriesAwait(ImportType.CITIES, jobStatus)

            handler.handle(Future.succeededFuture())
        }
    }

    override fun readGeoIpRanges(handler: Handler<AsyncResult<Void>>) {
        launch {
            clearExistingGeoIpRangesData()
            readGeoIpRangesAwait()
            handler.handle(Future.succeededFuture())
        }
    }

    private fun geoIpRangesFileLocation(): String {
        val fileName = vertx.orCreateContext.config().getNestedString("geodata.geoipranges",
            "input/geoipranges.csv")
        log.info("Geo ip ranges file name is: ${fileName}")
        return fileName
    }

    private fun countriesFileLocation(): String {
        val fileName = vertx.orCreateContext.config().getNestedString("geodata.countriesandregions",
            "input/countriesandregions.csv")
        log.info("Countries and regions file name is: ${fileName}")
        return fileName
    }

    private suspend fun clearExistingData() {
        val clearDataCommand = ClearCountriesDataCommand()
        sendPayloadToEventBus(clearDataCommand.toJson())
    }

    private suspend fun clearExistingGeoIpRangesData() {
        val clearDataCommand = ClearGeoIpRangesDataCommand()
        sendPayloadToEventBus(clearDataCommand.toJson())
    }

    private suspend fun readCountriesAwait(importType: ImportType, jobStatus: JobStatus) {
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
        try {
            log.info("Start processing input coutries and regions...")

            for (line in lines) {
                if (isValidCountriesLine(line)) {
                    processCountriesAndRegionsLine(line, jobStatus, importType)
                }
            }

            log.info("Completed processing input countries and regions...")
        } catch (e: Exception) {
            log.error("Exception while processing input: ", e)
        }
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

    private suspend fun processCountriesAndRegionsLine(line: String, jobStatus: JobStatus, importType: ImportType) {

        jobStatus.totalCount += 1
        if (jobStatus.totalCount % PRINT_JOB_STATUS_LINE_FREQUENCY == 0) {
            log.info("Update about importing countries status: $jobStatus")
        }

        val elements: List<String> = line.split(",")
        val geoIdentifier = elements[0]
        val countryIso = cleanUpString(elements[4])
        val countryName = cleanUpString(elements[5])
        val regionSubdivision1Code = cleanUpString(elements[6])
        val regionSubdivision1Name = cleanUpString(elements[7])
        val regionSubdivision2Code = cleanUpString(elements[8])
        val regionSubdivision2Name = cleanUpString(elements[9])
        val cityName = cleanUpString(elements[10])

        if (geoIdentifier == null || geoIdentifier.isBlank() || jobStatus.geoIdentifiers.contains(geoIdentifier)) {
            jobStatus.skippedCount += 1
            return
        }

        var success = false

        if (importType == ImportType.COUNTRIES && countryIso.isNotBlank() && regionSubdivision1Code.isNullOrBlank()
            && cityName.isNullOrBlank()) {
            // Country has country code but no region code and no city
            success = true
            jobStatus.geoIdentifiers.add(geoIdentifier)

            val newCountry = Country(countryIso, countryName, mutableSetOf())
            throwCountryCreatedEvent(newCountry)
        } else if (importType == ImportType.REGIONS && regionSubdivision1Code.isNotBlank() && cityName.isNullOrBlank()) {
            // Region has country code and region code and no city
            success = true
            jobStatus.geoIdentifiers.add(geoIdentifier)

            val newRegion = Region(null, geoIdentifier, countryIso, regionSubdivision1Code,
                regionSubdivision1Name, regionSubdivision2Code, regionSubdivision2Name, mutableListOf())
            throwRegionCreatedEvent(newRegion, countryIso)
        } else if (importType == ImportType.CITIES && countryIso.isNotBlank() && regionSubdivision1Code.isNotBlank()
            && cityName.isNotBlank()) {

            success = true
            jobStatus.geoIdentifiers.add(geoIdentifier)

            val city = City(intIdentifier = null, geoNameIdentifier = geoIdentifier, cityName = cityName,
                regionIntIdentifier = null)
            // TODO: we do not know the region geo identifier here. Can this be fixed?
            val existingRegion = Region(null, "", countryIso, regionSubdivision1Code,
                regionSubdivision1Name, regionSubdivision2Code, regionSubdivision2Name, mutableListOf())
            throwCityCreatedEvent(existingRegion, city)
        }

        if (success) {
            jobStatus.successCount += 1
        } else {
            jobStatus.errorCount += 1
        }
    }

    private suspend fun readGeoIpRangesAwait() {
        val geoIpRangesFileLocation = geoIpRangesFileLocation()
        if (!vertx.fileSystem().existsAwait(geoIpRangesFileLocation)) {
            throw FileNotFoundException("No file at location $geoIpRangesFileLocation")
        }

        val fileContentsBuffer: Buffer = vertx.fileSystem().readFileAwait(geoIpRangesFileLocation)
        val lines: List<String> = fileContentsBuffer.toString().split("\n")

        log.info("There are ${lines.size} lines in file $geoIpRangesFileLocation")

        val jobStatus = JobStatus()

        try {
            log.info("Start processing input geo ip ranges...")

            for (line in lines) {
                if (isValidGeoIpRangeLine(line)) {
                    processGeoIpRangeLine(line, jobStatus)
                }
            }

            log.info("Completed processing input geo ip ranges...")
        } catch (e: Exception) {
            log.error("Exception while processing input: ", e)
        }
    }

    private fun isValidGeoIpRangeLine(line: String): Boolean {

        var isValid: Boolean

        if (line == null) {
            isValid = false
        } else {
            isValid = line.isNotBlank()

            val elements: List<String> = line.split(",")
            isValid = isValid && elements.size == GEO_IP_RANGE_ELEMENTS_PER_LINE

            if (isValid) {
                val ipRange = elements[0]
                isValid = isValid && isValidCidrIp(ipRange)

                val geoIdentifier = elements[1]
                isValid = isValid && isNotBlank(geoIdentifier)
            }
        }

        if (!isValid) {
            log.info("Skipping line $line")
        }

        return isValid
    }

    private suspend fun processGeoIpRangeLine(line: String, jobStatus: JobStatus) {
        val elements: List<String> = line.split(",")
        val ipRange = elements[0]
        val geoIdentifier = elements[1]
        throwGeoIpRangeCreatedEvent(ipRange, geoIdentifier)
    }

    private fun cleanUpString(input: String): String = RegExUtils.replaceAll(input, "[\"\']", "")

    private suspend fun throwCountryCreatedEvent(country: Country) {
        val eventPayload = CountryCreatedEvent(country).toJson()
        sendPayloadToEventBus(eventPayload)
    }

    private suspend fun throwRegionCreatedEvent(region: Region, countryIso: String) {
        val eventPayload = RegionCreatedEvent(region, countryIso).toJson()
        sendPayloadToEventBus(eventPayload)
    }

    private suspend fun throwCityCreatedEvent(region: Region, city: City) {
        val eventPayload = CityCreatedEvent(region, city).toJson()
        sendPayloadToEventBus(eventPayload)
    }

    private suspend fun throwGeoIpRangeCreatedEvent(ipRange: String, geoIdentifier: String) {
        val eventPayload = GeoIpRangeCreatedEvent(geoNameIdentifier = geoIdentifier, cidrRange = ipRange).toJson()
        sendPayloadToEventBus(eventPayload)
    }

    private suspend fun sendPayloadToEventBus(payload: JsonObject) {

        log.info("Sending event bus message with payload: ${payload}")

        val deliveryOptions = deliveryOptionsOf().setSendTimeout(MESSAGE_SEND_TIMEOUT)
        val reply: Message<JsonObject> = vertx.eventBus().requestAwait(EventBusAddress.DOMAIN_EVENTS_LISTENER_ADDRESS.address,
            payload, deliveryOptions)

        val errorMessage: String? = reply.body().getString("error", null)
        if (errorMessage != null) {
            throw Exception(errorMessage)
        }
    }
}