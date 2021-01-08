package nl.geoipapp.adapter.http

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import nl.geoipapp.domain.Country
import nl.geoipapp.domain.Region
import nl.geoipapp.util.addAll

class CountryDto(
    val isoCode2: String,
    val name: String,
    val regions: List<RegionDto>?,
    val selectedRegion: RegionDto?
) {

    fun toJson(): JsonObject {
        val jsonObject = JsonObject()
            .put("isoCode2", isoCode2)
            .put("name", name)

        if (regions != null) {
            jsonObject.put("regions", JsonArray().addAll(regions.map{ it.toJson() }))
        } else if (selectedRegion != null) {
            jsonObject.put("selectedRegion", selectedRegion)
        }

        return jsonObject
    }

    companion object {

        fun from(countries: List<Country>): List<CountryDto> = countries.sortedWith(compareBy{ it.name })
            .map{ from(it) }

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
            return CountryDto(country.isoCode2, country.name, regionDtos, null)
        }

        fun from(country: Country, selectedRegion: Region, city: String?): CountryDto {

            val regionDto: RegionDto? = country.regions
                .filter{ region -> region.geoIdentifier == selectedRegion.geoIdentifier }
                .map{ region -> RegionDto.from(region, city) }
                .firstOrNull()

            if (regionDto == null) {
                throw IllegalArgumentException("Region does not exist in country")
            } else {
                return CountryDto(country.isoCode2, country.name, null, regionDto)
            }
        }
    }
}