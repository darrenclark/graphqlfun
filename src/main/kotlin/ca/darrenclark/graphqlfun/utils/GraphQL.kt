package ca.darrenclark.graphqlfun.utils

import graphql.language.StringValue
import graphql.schema.GraphQLAppliedDirectiveArgument

val GraphQLAppliedDirectiveArgument.stringValue: String?
  get() = (argumentValue.value as? StringValue)?.value
