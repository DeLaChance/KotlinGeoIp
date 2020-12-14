package nl.geoipapp.service

import nl.geoipapp.domain.Country
import nl.geoipapp.domain.GeoIpRange

interface GeoDataImporter {

    suspend fun readGeoIpRanges(fileLocation: String): MutableList<GeoIpRange>
    suspend fun readCountries(fileLocation: String): MutableList<Country>
}