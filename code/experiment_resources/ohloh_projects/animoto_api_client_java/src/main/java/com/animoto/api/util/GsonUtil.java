package com.animoto.api.util;

import com.animoto.api.enums.*;
import com.animoto.api.postroll.Postroll;
import com.animoto.api.gson.serializer.*;
import com.animoto.api.gson.deserializer.*;

import com.google.gson.*;

public class GsonUtil {
  public static Gson create() {
    GsonBuilder gsonBuilder = new GsonBuilder();
    gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
    gsonBuilder.registerTypeAdapter(Style.class, new ValueSerializer());
    gsonBuilder.registerTypeAdapter(VisualType.class, new ValueSerializer());
    gsonBuilder.registerTypeAdapter(Rotation.class, new ValueSerializer());
    gsonBuilder.registerTypeAdapter(Resolution.class, new ValueSerializer());
    gsonBuilder.registerTypeAdapter(Format.class, new ValueSerializer());
    gsonBuilder.registerTypeAdapter(Framerate.class, new ValueSerializer());
  
    gsonBuilder.registerTypeAdapter(Resolution.class, new ResolutionDeserializer());
    gsonBuilder.registerTypeAdapter(Framerate.class, new FramerateDeserializer());
    gsonBuilder.registerTypeAdapter(Format.class, new FormatDeserializer());

    gsonBuilder.registerTypeAdapter(Postroll.class, new PostrollDeserializer());
    return gsonBuilder.create();
  }
}
