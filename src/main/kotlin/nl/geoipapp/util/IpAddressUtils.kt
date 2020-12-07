package nl.geoipapp.util

import com.google.common.net.InetAddresses
import nl.geoipapp.domain.GeoIpRange
import java.net.InetAddress
import java.util.stream.Collectors

val GEO_IP_RANGE_COMPARATOR_BY_PRIORITY: Comparator<GeoIpRange> = Comparator {
    a: GeoIpRange, b: GeoIpRange -> a.priority.compareTo(b.priority) }

fun ipToNumeric(ipAddressV4: String): Int {
    val ipAddress: InetAddress = InetAddresses.forString(ipAddressV4)
    return InetAddresses.coerceToInteger(ipAddress)
}
