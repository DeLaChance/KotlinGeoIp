package main

import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import main.configuration.MainVerticle

fun main(args : Array<String>) {
    val vertx = Vertx.vertx()
    val options = DeploymentOptions()
    vertx.deployVerticle(MainVerticle::class.java, options)
}

