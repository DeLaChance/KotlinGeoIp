package nl.geoipapp.configuration

enum class EventBusAddress(val address: String) {

    DOMAIN_EVENTS_LISTENER_ADDRESS("domain.events.listener.address"),

    COUNTRY_REPOSITORY_LISTENER_ADDRESS("country.repository.listener.address")
}