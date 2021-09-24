package com.animoto.api.postroll;

import com.animoto.api.postroll.Postroll;
import com.animoto.api.Jsonable;

public class CustomFootagePostroll extends Postroll implements Jsonable {
  private String sourceUrl;

  public CustomFootagePostroll() {
    template = "custom_footage";
  }

  public void setSourceUrl(String sourceUrl) {
    this.sourceUrl = sourceUrl;
  }

  public String getSourceUrl() {
    return this.sourceUrl;
  }

  @Override
  public void setTemplate(String template) throws RuntimeException {
    throw(new RuntimeException("The template of a CustomFootagePostroll cannot be changed"));
  }
}
