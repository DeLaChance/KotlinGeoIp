package main.configuration

import io.vertx.core.AbstractVerticle
import io.vertx.kotlin.core.deployVerticleAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import main.adapter.http.HttpServerVerticle
import org.slf4j.LoggerFactory

class MainVerticle : CoroutineVerticle() {

  val LOG = LoggerFactory.getLogger(MainVerticle::class.java)

  // Called when verticle is deployed
  override suspend fun start() {
    vertx.deployVerticleAwait("main.adapter.http.HttpServerVerticle")
  }

  // Optional - called when verticle is undeployed
  override suspend fun stop() {
  }

}