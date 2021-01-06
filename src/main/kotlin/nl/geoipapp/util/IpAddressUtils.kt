package nl.geoipapp.util

import com.google.common.net.InetAddresses
import nl.geoipapp.domain.GeoIpRange
import java.net.InetAddress
import java.util.regex.Pattern

val GEO_IP_RANGE_COMPARATOR_BY_PRIORITY: Comparator<GeoIpRange> = Comparator {
    a: GeoIpRange, b: GeoIpRange -> a.priority.compareTo(b.priority) }

val cidrPattern = Pattern.compile("(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})/(\\d{1,3})")

fun ipToNumeric(ipAddressV4: String): Int {
    val ipAddress: InetAddress = InetAddresses.forString(ipAddressV4)
    return InetAddresses.coerceToInteger(ipAddress)
}

fun isValidIpV4Range(ipAddressV4: String): Boolean = cidrPattern.matcher(ipAddressV4).matches()