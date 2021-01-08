package nl.geoipapp.util

import com.google.common.net.InetAddresses
import nl.geoipapp.domain.GeoIpRange
import org.apache.commons.net.util.SubnetUtils
import java.net.InetAddress
import java.util.regex.Pattern

val GEO_IP_RANGE_COMPARATOR_BY_PRIORITY: Comparator<GeoIpRange> = Comparator {
    a: GeoIpRange, b: GeoIpRange -> a.priority.compareTo(b.priority) }

val cidrPattern = Pattern.compile("(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})/(\\d{1,3})")

fun ipToNumeric(ipAddressV4: String): Int {
    val ipAddress: InetAddress = InetAddresses.forString(ipAddressV4)
    return InetAddresses.coerceToInteger(ipAddress)
}

fun isValidCidrIp(cidrIp: String): Boolean {
    var isValid = true
    try {
        SubnetUtils(cidrIp)
    } catch (e: IllegalArgumentException) {
        isValid = false
    }

    return isValid
}

fun mapToLowEndIp(cidrIp: String): String = SubnetUtils(cidrIp).info.lowAddress

fun mapToHighEndIp(cidrIp: String): String = SubnetUtils(cidrIp).info.highAddress
