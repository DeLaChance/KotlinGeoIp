package main.configuration

import io.vertx.core.AbstractVerticle

class MainVerticle : AbstractVerticle() {

  // Called when verticle is deployed
  override fun start() {
      println("Main verticle is deployed")
  }

  // Optional - called when verticle is undeployed
  override fun stop() {
  }

}