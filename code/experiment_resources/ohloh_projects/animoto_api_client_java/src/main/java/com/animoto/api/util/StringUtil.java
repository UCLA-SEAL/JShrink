package com.animoto.api.util;

import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class StringUtil {
  public static boolean isBlank(String s) {
    if (s == null || s.trim().equals("")) {
      return true;
    }
    else {
      return false;
    }
  }

  public static String convertStreamToString(InputStream is) throws IOException {
    if (is != null) {
      StringBuilder sb = new StringBuilder();
      String line;
      try {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        while ((line = reader.readLine()) != null) {
          sb.append(line).append("\n");
        }
      } 
      finally {
        is.close();
      }
      return sb.toString();
    } 
    else {           
      return "";
    }
  }
}
