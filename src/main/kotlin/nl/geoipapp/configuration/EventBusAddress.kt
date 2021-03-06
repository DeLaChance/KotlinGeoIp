package nl.geoipapp.configuration

enum class EventBusAddress(val address: String) {

    GEO_DATA_IMPORTER_EVENT_BUS_ADDRESS("geo.data.importer.event.bus.address"),
    DOMAIN_EVENTS_LISTENER_ADDRESS("domain.events.listener.address"),
    IN_MEMORY_COUNTRY_REPOSITORY_LISTENER_ADDRESS("country.repository.listener.address"),
    POSTGRESQL_BACKED_COUNTRY_REPOSITORY_LISTENER_ADDRESS("country.postgres.repository.listener.address")
}