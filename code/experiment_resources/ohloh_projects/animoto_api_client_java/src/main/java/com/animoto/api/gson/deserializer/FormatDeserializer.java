package com.animoto.api.gson.deserializer;

import java.lang.reflect.*;

import com.google.gson.*;

import com.animoto.api.enums.Format;

public class FormatDeserializer implements JsonDeserializer {
  public Object deserialize(JsonElement json, Type classOfT, JsonDeserializationContext context) throws JsonParseException {
    String value = json.getAsString();
    value = value.toUpperCase();
    return Format.valueOf(value);
  }
}
