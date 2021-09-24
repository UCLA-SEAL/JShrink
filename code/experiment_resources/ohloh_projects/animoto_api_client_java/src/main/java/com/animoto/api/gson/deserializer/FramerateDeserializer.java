package com.animoto.api.gson.deserializer;

import java.lang.reflect.*;

import com.google.gson.*;

import com.animoto.api.enums.Framerate;

public class FramerateDeserializer implements JsonDeserializer {
  public Object deserialize(JsonElement json, Type classOfT, JsonDeserializationContext context) throws JsonParseException {
    String value = json.getAsString();
    value = "F_" + value;
    return Framerate.valueOf(value);
  }
}
