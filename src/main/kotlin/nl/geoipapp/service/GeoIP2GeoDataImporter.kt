package nl.geoipapp.service

import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.kotlin.core.eventbus.deliveryOptionsOf
import io.vertx.kotlin.core.file.readFileAwait
import nl.geoipapp.domain.Country
import nl.geoipapp.domain.Region
import nl.geoipapp.domain.events.CountryCreatedEvent
import nl.geoipapp.domain.events.RegionCreatedEvent
import nl.geoipapp.util.getNestedString
import org.slf4j.LoggerFactory

class GeoIP2GeoDataImporter(val vertx: Vertx) : GeoDataImporter {

    val LOG = LoggerFactory.getLogger(GeoIP2GeoDataImporter::class.java)

    override suspend fun readCountries(fileLocation: String) {

        val fileContentsBuffer: Buffer = vertx.fileSystem().readFileAwait(countriesFileLocation())
        val lines: List<String> = fileContentsBuffer.toString().split("\n")

        val geoIdentifiers = mutableSetOf<String>()

        lines.stream()
            .filter { line -> isValidCountriesLine(line) }
            .forEach{ line -> processCountriesAndRegionsLine(line, geoIdentifiers) }
    }

    override suspend fun readGeoIpRanges(fileLocation: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun geoIpRangesFileLocation(): String {
        val fileName = vertx.orCreateContext.config().getNestedString("geoData.geoIpRanges",
            "input/geoipranges.csv")
        LOG.info("Geo ip ranges file name is: ${fileName}")
        return fileName
    }

    private fun countriesFileLocation(): String {
        val fileName = vertx.orCreateContext.config().getNestedString("geoData.countriesandregions",
            "input/countriesandregions.csv")
        LOG.info("Countries and regions file name is: ${fileName}")
        return fileName
    }

    fun isValidCountriesLine(line: String?): Boolean {

        var isValid: Boolean

        if (line == null) {
            isValid = false
        } else {
            isValid = line.isNotBlank() && line.startsWith("[0-9]+")

            val elements: List<String> = line.split(",")
            isValid = isValid && elements.size == 13
        }

        return isValid
    }

    private fun processCountriesAndRegionsLine(line: String, geoIdentifiers: MutableSet<String>) {

        val elements: List<String> = line.split(",")
        val geoIdentifier = elements[0]
        val countryIso = elements[4]
        val countryName = elements[5]
        val regionSubdivision1Code =  elements[6]
        val regionSubdivision1Name =  elements[7]
        val regionSubdivision2Code =  elements[8]
        val regionSubdivision2Name=  elements[9]
        val cityName = elements[10]

        if (geoIdentifier == null || geoIdentifier.isBlank() || geoIdentifiers.contains(geoIdentifier)) {
            return
        }

        geoIdentifiers.add(geoIdentifier)

        if (countryIso?.isNotBlank()) {
            val newCountry = Country(countryIso, countryName, mutableListOf())
            throwCountryCreatedEvent(newCountry)
        }

        if (regionSubdivision1Code?.isNotBlank()) {
            val newRegion = Region(geoIdentifier, regionSubdivision1Code, regionSubdivision1Name,
                regionSubdivision2Code, regionSubdivision2Name, cityName)
            throwRegionCreatedEVent(newRegion, countryIso)
        }
    }

    private fun throwCountryCreatedEvent(country: Country) {
        val eventPayload = CountryCreatedEvent(country).toJson().encodePrettily()
        throwEvent(eventPayload)
    }

    private fun throwRegionCreatedEVent(region: Region, countryIso: String) {
        val eventPayload = RegionCreatedEvent(region, countryIso).toJson().encodePrettily()
        throwEvent(eventPayload)
    }

    private fun throwEvent(eventPayload: String) {
        val deliveryOptions = deliveryOptionsOf()
        vertx.eventBus().publish(eventPayload, deliveryOptions)
    }
}