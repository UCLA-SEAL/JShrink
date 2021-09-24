package com.animoto.api.visual;

import com.animoto.api.enums.VisualType;

import com.google.gson.annotations.SerializedName;

public abstract class BaseVisual implements Visual {

  @SerializedName("type") protected VisualType visualType = null;

  public String getType() {
    if (visualType == null) {
      return null;
    }
    else {
      return visualType.getValue();
    }
  }
}
