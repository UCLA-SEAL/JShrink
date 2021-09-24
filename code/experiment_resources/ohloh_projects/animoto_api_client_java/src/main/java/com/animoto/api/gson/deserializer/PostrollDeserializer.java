package com.animoto.api.gson.deserializer;

import java.lang.reflect.*;

import com.google.gson.*;
import com.google.gson.JsonElement;

import com.animoto.api.postroll.*;

public class PostrollDeserializer implements JsonDeserializer<Postroll> {
  public Postroll deserialize(JsonElement json, Type classOfT, JsonDeserializationContext context) throws JsonParseException {
    if (!json.isJsonObject()) {
      throw(new JsonParseException("Postroll value is not expected type (Object)"));
    }
    JsonObject jsonPostroll = json.getAsJsonObject();
    JsonElement jsonTemplate = jsonPostroll.get("template");
    if ((jsonTemplate == null) || !(jsonTemplate.isJsonPrimitive() && jsonTemplate.getAsJsonPrimitive().isString())) {
      throw(new JsonParseException("Postroll value \"template\" is not expected type (String)"));
    }
    String template = jsonTemplate.getAsString();
    if (template.equals("custom_footage")) {
      CustomFootagePostroll postroll = new CustomFootagePostroll();
      JsonElement jsonSourceUrl = jsonPostroll.get("source_url");
      if ((jsonSourceUrl == null) || !(jsonSourceUrl.isJsonPrimitive() && jsonSourceUrl.getAsJsonPrimitive().isString())) {
        throw(new JsonParseException("Postroll value \"source_url\" is not expected type (String)"));
      }
      postroll.setSourceUrl(jsonSourceUrl.getAsString());
      return postroll;
    } else {
      BasicPostroll postroll = new BasicPostroll();
      postroll.setTemplate(template);
      return postroll;
    }
  }
}
