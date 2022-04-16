package ca.darrenclark.graphqlfun.graphql.directives

import ca.darrenclark.graphqlfun.utils.stringValue
import ca.darrenclark.graphqlfun.utils.unwrapDeep
import com.google.gson.JsonParser
import graphql.schema.DataFetcher
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.idl.SchemaDirectiveWiring
import graphql.schema.idl.SchemaDirectiveWiringEnvironment
import io.vertx.ext.web.client.WebClient
import java.util.concurrent.CompletableFuture

class Http: SchemaDirectiveWiring {
  override fun onField(environment: SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition>): GraphQLFieldDefinition {
    val field = environment.element
    val parentType = environment.fieldsContainer

    val url = environment.directive.getArgument("url").toAppliedArgument().stringValue!!

    environment.codeRegistry.dataFetcher(parentType, field, httpDataFetcher(url))

    return field
  }

  private fun httpDataFetcher(url: String) = DataFetcher<CompletableFuture<Any>> { environment ->
    val result = CompletableFuture<Any>()

    val webClient: WebClient = environment.graphQlContext["webClient"]!!
    webClient.getAbs(url)
      .send()
      .onSuccess {
        val json = JsonParser.parseString(it.bodyAsString())
        result.complete(json)
      }
      .onFailure {
        result.completeExceptionally(it)
      }

    result
  }
}
