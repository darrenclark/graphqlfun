package ca.darrenclark.graphqlfun.graphql.directives

import ca.darrenclark.graphqlfun.utils.stringValue
import ca.darrenclark.graphqlfun.utils.unwrapDeep
import ca.darrenclark.graphqlfun.utils.unwrapPrimitives
import com.google.gson.*
import graphql.schema.DataFetcher
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.idl.SchemaDirectiveWiring
import graphql.schema.idl.SchemaDirectiveWiringEnvironment

class Constant: SchemaDirectiveWiring {
  override fun onField(environment: SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition>): GraphQLFieldDefinition {
    val field = environment.element
    val parentType = environment.fieldsContainer

    val json = environment.directive.getArgument("json").toAppliedArgument().stringValue!!
    val result = JsonParser.parseString(json).unwrapPrimitives()

    val dataFetcher = DataFetcher<Any> { result }

    environment.codeRegistry.dataFetcher(parentType, field, dataFetcher)

    return field
  }
}
