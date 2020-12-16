package nl.geoipapp.service

interface GeoDataImporter {

    suspend fun readGeoIpRanges(fileLocation: String)
    suspend fun readCountries(fileLocation: String)
}