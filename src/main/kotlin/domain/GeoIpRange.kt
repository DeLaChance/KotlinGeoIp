package main.domain

class GeoIpRange(
    val id: Int,
    val beginIpNumeric: Int,
    val endIpNumeric: Int,
    val beginIp: String,
    val endIp: String,
    val country: Country,
    val region: Region,
    val city: City?)