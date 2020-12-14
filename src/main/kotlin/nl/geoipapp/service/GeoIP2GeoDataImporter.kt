package nl.geoipapp.service

import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.kotlin.core.eventbus.deliveryOptionsOf
import io.vertx.kotlin.core.file.readFileAwait
import nl.geoipapp.domain.City
import nl.geoipapp.domain.Country
import nl.geoipapp.domain.GeoIpRange
import nl.geoipapp.domain.Region
import nl.geoipapp.domain.events.CountryFoundEvent

class GeoIP2GeoDataImporter(val vertx: Vertx) : GeoDataImporter {

    override suspend fun readCountries(fileLocation: String): MutableList<Country> {

        val fileContentsBuffer: Buffer = vertx.fileSystem().readFileAwait(countriesFileLocation())
        val lines: List<String> = fileContentsBuffer.toString().split("\n")

        val countries: MutableMap<String, Country> = mutableMapOf()
        val regions: MutableMap<String, Region> = mutableMapOf()
        val cities: MutableMap<String, City> = mutableMapOf()

        lines.stream()
            .filter { line -> isValidCountriesLine(line) }
            .forEach{ line -> processCountriesAndRegionsLine(countries, regions, cities, line) }




        return mutableListOf() // TODO:
    }

    override suspend fun readGeoIpRanges(fileLocation: String): MutableList<GeoIpRange> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun geoIpRangesFileLocation(): String {
        return "input/geoipranges.csv"
    }

    private fun countriesFileLocation(): String {
        return "input/countriesandregions.csv"
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

    private fun processCountriesAndRegionsLine(countries: MutableMap<String, Country>,
        regions: MutableMap<String, Region>, cities: MutableMap<String, City>, line: String) {

        val elements: List<String> = line.split(",")
        val geoIdentifier = elements[0]
        val countryIso = elements[4]
        val countryName = elements[5]
        val regionSubdivision1Code =  elements[6]
        val regionSubdivision1Name =  elements[7]
        val regionSubdivision2Code =  elements[8]
        val regionSubdivision2Name=  elements[9]
        val cityName = elements[10]

        if (geoIdentifier == null || geoIdentifier.isBlank()) {
            return
        }

        if (countryIso !=  null && countryIso.isNotBlank() && !countries.containsKey(countryIso)) {
            val newCountry = Country(countryIso, countryName, mutableListOf())
            throwCountryCreatedEvent(newCountry)
        }

        if (!regions.containsKey(geoIdentifier) && regionSubdivision1Code != null && regionSubdivision1Code.isNotBlank()) {
            val newRegion = Region(geoIdentifier, regionSubdivision1Code, regionSubdivision1Name,
                regionSubdivision2Code, regionSubdivision2Name)
            regions[geoIdentifier] = newRegion
        }

        if (!cities.containsKey(geoIdentifier) && cityName != null && cityName.isNotBlank()) {
            val newCity = City(cityName, geoIdentifier)
            cities[geoIdentifier] = newCity
        }
    }

    private fun throwCountryCreatedEvent(country: Country) {
        val eventPayload = CountryFoundEvent(country).toJson().encodePrettily()
        val deliveryOptions = deliveryOptionsOf()
        vertx.eventBus().send(eventPayload, deliveryOptions)
    }
}