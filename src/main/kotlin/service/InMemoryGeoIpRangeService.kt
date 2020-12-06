package main.service

import main.domain.GeoIpRange
import main.util.GEO_IP_RANGE_COMPARATOR_BY_PRIORITY
import main.util.ipToNumeric
import java.util.stream.Collectors

class InMemoryGeoIpRangeService : GeoIpRangeService {

    /**
     * List should be sorted on property 'beginIpNum' of {@link GeoIpRange}.
     */
    val geoIpRangesList: MutableList<GeoIpRange> = mutableListOf()

    override fun findByIpAddress(ipAddressV4: String): GeoIpRange? {

        val beginIpNumeric = ipToNumeric(ipAddressV4)
        val matchingRanges = geoIpRangesList.stream()
            .filter { it.containsIpNumeric(beginIpNumeric) }
            .sorted(GEO_IP_RANGE_COMPARATOR_BY_PRIORITY)
            .collect(Collectors.toList())

        val geoIpRange: GeoIpRange?
        if (matchingRanges.isEmpty()) {
            geoIpRange = null
        } else {
            geoIpRange = matchingRanges[0]
        }

        return geoIpRange
    }

    override fun save(newGeoIpRanges: List<GeoIpRange>) {
        geoIpRangesList.addAll(newGeoIpRanges)
    }
}