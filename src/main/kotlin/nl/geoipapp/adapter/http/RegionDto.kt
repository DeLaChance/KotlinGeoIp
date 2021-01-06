package nl.geoipapp.adapter.http

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import nl.geoipapp.domain.Country
import nl.geoipapp.domain.Region
import nl.geoipapp.util.addAll

class RegionDto(
    val subdivision1Code: String,
    val subdivision1Name: String,
    val subdivision2Code: String?,
    val subdivision2Name: String?,
    val cities: List<String>
) {

    fun toJson(): JsonObject {
        return JsonObject()
            .put("subdivision1Code", subdivision1Code)
            .put("subdivision1Name", subdivision1Name)
            .put("subdivision2Code", subdivision2Code)
            .put("subdivision2Name", subdivision2Name)
            .put("cities", JsonArray().addAll(cities))
    }

    companion object {
        fun from(region: Region): RegionDto {
            val sortedCitiesList: List<String> = region?.cities?.toMutableList()?.sortedWith(compareBy{ it }).orEmpty()
            return RegionDto(region.subdivision1Code, region.subdivision1Name, region.subdivision2Code, region.subdivision2Name,
                sortedCitiesList)
        }
    }
}