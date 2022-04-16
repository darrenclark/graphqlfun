package ca.darrenclark.graphqlfun

import ca.darrenclark.graphqlfun.filesystem.LocalFilesystem
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.ext.web.Router
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.graphql.GraphQLHandler
import io.vertx.ext.web.handler.graphql.GraphQLHandlerOptions
import io.vertx.ext.web.handler.graphql.GraphiQLHandler

import io.vertx.ext.web.handler.graphql.GraphiQLHandlerOptions
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await


class MainVerticle : CoroutineVerticle() {

  override suspend fun start() {
    val router = Router.router(vertx)

    router.route().handler(BodyHandler.create())

    router.get("/").handler { req ->
      req.response()
        .putHeader("content-type", "text/plain")
        .end("Hello from Vert.x!")
    }

    val webClient = WebClient.create(vertx)

    val fs = LocalFilesystem("./src/main/resources/")

    val handler = GraphQLHandler.create(setupGraphQL(fs)).beforeExecute {
      it.builder().graphQLContext(mapOf("webClient" to webClient))
    }

    router.route("/graphql").handler(handler)

    val options = GraphiQLHandlerOptions().setEnabled(true)

    val graphiQLHandler = GraphiQLHandler.create(options)
//    router.route("/graphiql").handler { it.reroute("/graphiql/"); it.next() }
    router.route("/graphiql/*").handler(graphiQLHandler)

    val server = vertx
      .createHttpServer()
      .requestHandler(router)
      .listen(8888)
      .await()

    println("""
      Server started at http://localhost:8888
    """.trimIndent())
  }
}
