package ca.darrenclark.graphqlfun

import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.graphql.GraphQLHandler
import io.vertx.ext.web.handler.graphql.GraphiQLHandler

import io.vertx.ext.web.handler.graphql.GraphiQLHandlerOptions




class MainVerticle : AbstractVerticle() {

  override fun start(startPromise: Promise<Void>) {
    val router = Router.router(vertx)

    router.route().handler(BodyHandler.create())

    router.get("/").handler { req ->
      req.response()
        .putHeader("content-type", "text/plain")
        .end("Hello from Vert.x!")
    }

    router.route("/graphql").handler(GraphQLHandler.create(setupGraphQL()))

    val options = GraphiQLHandlerOptions().setEnabled(true)

    val graphiQLHandler = GraphiQLHandler.create(options)
    router.route("/graphiql").handler(graphiQLHandler)
    router.route("/graphiql/*").handler(graphiQLHandler)


    vertx
      .createHttpServer()
      .requestHandler(router)
      .listen(8888) { http ->
        if (http.succeeded()) {
          startPromise.complete()
          println("HTTP server started on port 8888")
        } else {
          startPromise.fail(http.cause());
        }
      }
  }
}
