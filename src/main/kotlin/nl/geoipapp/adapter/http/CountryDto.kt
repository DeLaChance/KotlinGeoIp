package nl.geoipapp.adapter.http

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import nl.geoipapp.domain.Country
import nl.geoipapp.domain.Region
import nl.geoipapp.util.addAll

class CountryDto(
    val isoCode2: String,
    val name: String,
    val regions: List<RegionDto>)
{
    fun toJson(): JsonObject {
        return JsonObject()
            .put("isoCode2", isoCode2)
            .put("name", name)
            .put("regions", JsonArray().addAll(regions.map{ it.toJson() }))
    }

    companion object {

        fun from(countries: List<Country>): List<CountryDto> =
            countries.sortedWith(compareBy{ it.name }).map{ from(it) }

        fun fromNullable(country: Country?): CountryDto? {
            if (country == null) {
                return null
            } else {
                return from(country)
            }
        }

        fun from(country: Country): CountryDto {
            val regionDtos: List<RegionDto> = country.regions.sortedWith(compareBy{ "${it.subdivision1Name} ${it.subdivision2Name}" })
                .map{ region -> RegionDto.from(region)}
            return CountryDto(country.isoCode2, country.name, regionDtos)
        }
    }
}