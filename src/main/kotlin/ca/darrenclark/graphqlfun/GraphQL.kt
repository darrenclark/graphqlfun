package ca.darrenclark.graphqlfun

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
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import graphql.schema.GraphQLDirective
import graphql.schema.TypeResolver
import graphql.schema.idl.*

fun setupGraphQL(): GraphQL {
  val schemaParser = SchemaParser()
  val schemaGenerator = SchemaGenerator()

  val typeRegistry = Resources.getResource("schema.graphqls").openStream().use { schemaParser.parse(it) }

  val schema = schemaGenerator.makeExecutableSchema(typeRegistry, buildDynamicRuntimeWiring())
  return GraphQL.newGraphQL(schema).build()
}

fun buildDynamicRuntimeWiring(): RuntimeWiring {
  val dynamicWiringFactory: WiringFactory = object : WiringFactory {
    override fun providesDataFetcher(environment: FieldWiringEnvironment): Boolean {
      return environment.getDirective("constant") != null
    }

    override fun getDataFetcher(environment: FieldWiringEnvironment): DataFetcher<*> {
      val directive = environment.getDirective("constant")!!

      val setArgs = directive.arguments.filter { it.hasSetValue() }

      if (setArgs.size != 1) {
        return DataFetcher<Any> {
          throw RuntimeException("expected a single argument")
        }
      }

      val arg = setArgs[0]

      when (arg.name) {
        "json" -> {
          val result = JsonParser.parseString((arg.argumentValue.value as StringValue).value).unwrap()
          return DataFetcher<Any> { result }
        }
        "int" -> {
          return DataFetcher<Any> { (arg.argumentValue.value as IntValue).value }
        }
        "float" -> {
          return DataFetcher<Any> { (arg.argumentValue.value as FloatValue).value }
        }
        "boolean" -> {
          return DataFetcher<Any> { (arg.argumentValue.value as BooleanValue).isValue }
        }
        "string" -> {
          return DataFetcher<Any> { (arg.argumentValue.value as StringValue).value }
        }
        else -> {
          return DataFetcher<Any> {
            throw RuntimeException("oops")
          }
        }
      }
    }

    fun FieldWiringEnvironment.getDirective(name: String) : GraphQLDirective? = directives.find { it.name == name}

    fun JsonElement.unwrap() : Any? {
      if (this is JsonPrimitive) {
        when {
          isBoolean -> return asBoolean
          isNumber -> return asNumber
          isString -> return asString
        }
      } else if (isJsonNull) {
        return null
      } else if (this is JsonObject) {
        return entrySet().associate { Pair(it.key, it.value.unwrap()) }
      } else if (this is JsonArray) {
        return map { it.unwrap() }
      }

      throw RuntimeException("unreachable")
    }

//    fun providesTypeResolver(registry: TypeDefinitionRegistry?, definition: InterfaceTypeDefinition?): Boolean {
//      return getDirective(definition, "specialMarker") != null
//    }
//
//    fun providesTypeResolver(registry: TypeDefinitionRegistry?, definition: UnionTypeDefinition?): Boolean {
//      return getDirective(definition, "specialMarker") != null
//    }
//
//    fun getTypeResolver(registry: TypeDefinitionRegistry?, definition: InterfaceTypeDefinition?): TypeResolver? {
//      val directive: Directive = getDirective(definition, "specialMarker")
//      return createTypeResolver(definition, directive)
//    }
//
//    fun getTypeResolver(registry: TypeDefinitionRegistry?, definition: UnionTypeDefinition?): TypeResolver? {
//      val directive: Directive = getDirective(definition, "specialMarker")
//      return createTypeResolver(definition, directive)
//    }
//
//    fun providesDataFetcher(registry: TypeDefinitionRegistry?, definition: FieldDefinition?): Boolean {
//      return getDirective(definition, "dataFetcher") != null
//    }
//
//    fun getDataFetcher(registry: TypeDefinitionRegistry?, definition: FieldDefinition?): DataFetcher? {
//      val directive: Directive = getDirective(definition, "dataFetcher")
//      return createDataFetcher(definition, directive)
//    }
  }
  return RuntimeWiring.newRuntimeWiring()
    .wiringFactory(dynamicWiringFactory).build()
}
