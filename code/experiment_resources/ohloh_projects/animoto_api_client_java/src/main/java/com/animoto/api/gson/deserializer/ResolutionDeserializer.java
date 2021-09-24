package com.animoto.api.gson.deserializer;

import java.lang.reflect.*;

import com.google.gson.*;

import com.animoto.api.enums.Resolution;

public class ResolutionDeserializer implements JsonDeserializer {
  public Object deserialize(JsonElement json, Type classOfT, JsonDeserializationContext context) throws JsonParseException {
    String value = json.getAsString();
    value = value.toUpperCase();
    value = "R_" + value;
    return Resolution.valueOf(value);
  }
}
