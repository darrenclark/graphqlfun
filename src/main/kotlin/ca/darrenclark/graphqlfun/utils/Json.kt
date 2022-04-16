package ca.darrenclark.graphqlfun.utils

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive

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
