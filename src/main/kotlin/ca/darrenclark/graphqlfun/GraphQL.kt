package ca.darrenclark.graphqlfun

import ca.darrenclark.graphqlfun.filesystem.Filesystem
import ca.darrenclark.graphqlfun.graphql.datafetchers.DefaultDataFetcher
import ca.darrenclark.graphqlfun.graphql.directives.Constant
import ca.darrenclark.graphqlfun.graphql.directives.Http
import ca.darrenclark.graphqlfun.graphql.directives.Json
import com.expediagroup.graphql.generator.execution.FunctionDataFetcher
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.TypeFactory
import com.google.common.io.Resources
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.google.gson.stream.JsonReader
import graphql.GraphQL
import graphql.language.BooleanValue
import graphql.language.Directive
import graphql.language.FieldDefinition
import graphql.language.FloatValue
import graphql.language.IntValue
import graphql.language.InterfaceTypeDefinition
import graphql.language.StringValue
import graphql.language.UnionTypeDefinition
import graphql.schema.AsyncDataFetcher
import graphql.schema.DataFetcher
import graphql.schema.DataFetcherFactories
import graphql.schema.DataFetchingEnvironment
import graphql.schema.GraphQLDirective
import graphql.schema.PropertyDataFetcher
import graphql.schema.TypeResolver
import graphql.schema.idl.*
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.reduce
import java.net.URL
import java.util.concurrent.CompletableFuture
import javax.print.DocFlavor.STRING
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.reflect

suspend fun setupGraphQL(fs: Filesystem): GraphQL {
  val schemaParser = SchemaParser()
  val schemaGenerator = SchemaGenerator()

  val typeRegistry =
    fs.listFiles("**/*.graphqls")
      .map {
        val fileContents = fs.getFileContents(it)
        schemaParser.parse(fileContents)
      }
      .reduce { accumulator, value -> accumulator.merge(value) }

  val schema = schemaGenerator.makeExecutableSchema(typeRegistry, buildDynamicRuntimeWiring())
  return GraphQL.newGraphQL(schema).build()
}

fun buildDynamicRuntimeWiring(): RuntimeWiring {
  return RuntimeWiring.newRuntimeWiring()
    .wiringFactory(object : WiringFactory {
      override fun getDefaultDataFetcher(environment: FieldWiringEnvironment): DataFetcher<*> {
        return DefaultDataFetcher(environment.fieldDefinition.name)
      }
    })
    .directive("http", Http())
    .directive("constant", Constant())
    .directive("json", Json())
    .build()
}
