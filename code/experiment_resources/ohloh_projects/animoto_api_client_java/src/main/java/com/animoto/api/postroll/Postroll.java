package com.animoto.api.postroll;

import com.animoto.api.Jsonable;
import com.google.gson.Gson;

public abstract class Postroll implements Jsonable {
  protected String template;

  public void setTemplate(String template) {
    this.template = template;
  }

  public String getTemplate() {
    return this.template;
  }

  public String toJson() {
    Gson gson = new Gson();
    return gson.toJson(this);
  }
}
