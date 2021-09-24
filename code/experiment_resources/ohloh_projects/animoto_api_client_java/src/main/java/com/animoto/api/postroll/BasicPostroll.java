package com.animoto.api.postroll;

import com.animoto.api.postroll.Postroll;
import com.animoto.api.Jsonable;

public class BasicPostroll extends Postroll implements Jsonable {
  public static final String POWERED_BY_ANIMOTO = "powered_by_animoto";
  public static final String WHITE_LABEL = "white_label";

  public BasicPostroll() {
    template = BasicPostroll.POWERED_BY_ANIMOTO;
  }
}
