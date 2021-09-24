package com.animoto.api.dto;

import com.animoto.api.ApiError;

import com.google.gson.annotations.SerializedName;

public class Status {
  private int code;
  @SerializedName("errors") private ApiError[] apiErrors;

  public int getCode() {
    return code;
  }

  public ApiError[] getApiErrors() {
    return apiErrors;
  }
}
