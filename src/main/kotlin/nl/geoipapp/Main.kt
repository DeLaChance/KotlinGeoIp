package nl.geoipapp

import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import nl.geoipapp.configuration.MainVerticle

fun main(args : Array<String>) {
    val vertx = Vertx.vertx()
    val options = DeploymentOptions()
    vertx.deployVerticle(MainVerticle::class.java, options)
}

