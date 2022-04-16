package ca.darrenclark.graphqlfun.graphql.datafetchers

import ca.darrenclark.graphqlfun.utils.unwrapPrimitives
import com.expediagroup.graphql.generator.extensions.unwrapType
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import graphql.schema.PropertyDataFetcher

class DefaultDataFetcher(
  val fieldName: String,
  private val propertyDataFetcher: PropertyDataFetcher<Any?> = PropertyDataFetcher.fetching(fieldName)
): DataFetcher<Any?> {

  override fun get(environment: DataFetchingEnvironment): Any? {
    val source: Any? = environment.getSource()
    return if (source is JsonObject) {
      source.get(fieldName)?.unwrapPrimitives()
    } else {
      propertyDataFetcher.get(environment)
    }
  }
}
