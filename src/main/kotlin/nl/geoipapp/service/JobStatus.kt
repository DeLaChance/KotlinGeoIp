package nl.geoipapp.service

class JobStatus(
    var totalCount: Int = 0,
    var successCount: Int = 0,
    var errorCount: Int = 0,
    var skippedCount: Int = 0,
    val geoIdentifiers: MutableSet<String> = mutableSetOf()
) {

    override fun toString(): String {
        return "JobStatus(totalCount=$totalCount, successCount=$successCount, errorCount=$errorCount, skippedCount=$skippedCount)"
    }
}

enum class ImportType {
    COUNTRIES,
    REGIONS,
    CITIES
}