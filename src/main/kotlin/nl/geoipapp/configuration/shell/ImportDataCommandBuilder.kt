package nl.geoipapp.configuration.shell

import io.vertx.core.cli.Argument
import io.vertx.core.cli.CLI
import io.vertx.ext.shell.command.CommandProcess
import io.vertx.ext.shell.command.impl.CommandBuilderImpl
import nl.geoipapp.service.GeoDataImporter
import nl.geoipapp.service.readCountriesAwait
import nl.geoipapp.service.readGeoIpRangesAwait
import org.slf4j.LoggerFactory

class ImportDataCommandBuilder(val geoDataImporter: GeoDataImporter) : CommandBuilderImpl(
    "importData",
    CLI.create("importData")
        .addArgument(Argument().setRequired(true).setArgName("type")
        .setDescription("Type is either 'countries' or 'geoipranges'"))
) {

    val LOG = LoggerFactory.getLogger(ImportDataCommandBuilder::class.java)

    suspend fun generateProcessHandler(): (suspend (CommandProcess) -> Unit) {

        return { process ->
            try {
                process.writeLine("${process.args().size}")
                val type: String? = process.args()?.first()
                when (type) {
                    "countries" -> {
                        process.writeLine("Starting all countries and regions import.\n")
                        geoDataImporter.readCountriesAwait()
                        process.writeLine("Finished all countries and regions import.\n")
                    }
                    "geoipranges" -> {
                        process.writeLine("Starting all geo ip ranges import.\n")
                        geoDataImporter.readGeoIpRangesAwait()
                        process.writeLine("Finished all geo ip ranges import.\n")
                    }
                    else -> process.writeLine("Type should be either 'countries' or 'geoipranges'.\n")
                }
            } catch (e: Exception) {
                process.writeLine("Error while handling command: ${e.message}.\n")
            } finally {
                process.end()
            }
        }
    }

    private fun CommandProcess.writeLine(message: String) {
        LOG.info(message)
        write(message + "\n")
    }
}