package com.animoto.api.gson.serializer;

import com.google.gson.*;

import java.lang.reflect.*;

public class ValueSerializer implements JsonSerializer {
  public JsonElement serialize(Object object, Type objectType, JsonSerializationContext context) {
    try {
      Method method = object.getClass().getMethod("getValue");
      Object value = method.invoke(object);
      if (value instanceof Number) {
        return new JsonPrimitive((Number) value);
      }
      else if (value instanceof String) {
        return new JsonPrimitive((String) value);
      }
      else {
        return new JsonPrimitive(value.toString());
      }
    }
    catch (Exception e) {
      throw new Error(e);
    }
  }
}
