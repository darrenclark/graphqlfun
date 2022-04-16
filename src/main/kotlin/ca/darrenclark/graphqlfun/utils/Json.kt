package ca.darrenclark.graphqlfun.utils

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive

fun JsonElement.unwrapPrimitives() = unwrapPrimitives(unwrapArrays = true)

private fun JsonElement.unwrapPrimitives(unwrapArrays: Boolean = false) : Any? {
  if (this is JsonPrimitive) {
    when {
      isBoolean -> return asBoolean
      isNumber -> return asNumber
      isString -> return asString
    }
  } else if (isJsonNull) {
    return null
  } else if (this is JsonArray && unwrapArrays) {
    return map { it.unwrapPrimitives(unwrapArrays = false) }
  }

  return this
}

fun JsonElement.unwrapDeep() : Any? {
  if (this is JsonPrimitive) {
    when {
      isBoolean -> return asBoolean
      isNumber -> return asNumber
      isString -> return asString
    }
  } else if (isJsonNull) {
    return null
  } else if (this is JsonObject) {
    return entrySet().associate { Pair(it.key, it.value.unwrapDeep()) }
  } else if (this is JsonArray) {
    return map { it.unwrapDeep() }
  }

  throw RuntimeException("unreachable")
}
