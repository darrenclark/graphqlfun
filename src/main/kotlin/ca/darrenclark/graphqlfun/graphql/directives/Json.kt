package ca.darrenclark.graphqlfun.graphql.directives

import ca.darrenclark.graphqlfun.utils.stringValue
import ca.darrenclark.graphqlfun.utils.unwrapPrimitives
import com.google.gson.JsonParser
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.spi.json.GsonJsonProvider
import graphql.schema.DataFetcher
import graphql.schema.DataFetcherFactories
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.idl.SchemaDirectiveWiring
import graphql.schema.idl.SchemaDirectiveWiringEnvironment

class Json: SchemaDirectiveWiring {
  companion object {
    val jsonPathConfiguration = Configuration.defaultConfiguration().jsonProvider(GsonJsonProvider())
  }

  override fun onField(environment: SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition>): GraphQLFieldDefinition {
    val field = environment.element
    val parentType = environment.fieldsContainer

    val path = environment.directive.getArgument("path").toAppliedArgument().stringValue!!
    val jsonPath = JsonPath.compile(path)

    val originalDataFetcher = environment.codeRegistry.getDataFetcher(parentType, field)

    val dataFetcher = DataFetcherFactories.wrapDataFetcher(originalDataFetcher) { environment, value ->
      jsonPath.read(value, jsonPathConfiguration)
    }

    environment.codeRegistry.dataFetcher(parentType, field, dataFetcher)

    return field
  }
}
