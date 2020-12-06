package main.service

import main.domain.GeoIpRange

interface GeoIpRangeService {

    /**
     * Finds a {@link GeoIpRange} by an ip address (v4).
     *
     * @return if it exists, the {@link GeoIpRange} with the highest priority that contains the ip address. Otherwise
     * a null reference is returned.
     */
    fun findByIpAddress(ipAddressV4: String): GeoIpRange?

    /**
     * Adds a list of {@link GeoIpRange} to the index. Existing or overlapping ranges are updated w.r.t. the highest
     * priority.
     */
    fun save(geoIpRange: List<GeoIpRange>)
}