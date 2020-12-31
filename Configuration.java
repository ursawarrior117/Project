/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
// package Main;

/**
 *
 * @author Hải Đoàn
 */


import java.util.*;

import java.awt.*;

public class Configuration
    extends Object {
  /*
    Nhan cac tham so va dat vao bang de set color cho vien gach
   */

  private static Hashtable config = new Hashtable();

  public static String getValue(String key) {
    if (config.containsKey(key)) {
      return config.get(key).toString();
    }
    else {
      try {
        return System.getProperty(key);
      }
      catch (SecurityException ignore) {
        return null;
      }
    }
  }

  public static String getValue(String key, String def) {
    String value = getValue(key);

    return (value == null) ? def : value;
  }

  public static void setValue(String key, String value) {
    config.put(key, value);
  }

  public static Color getColor(String key, String def) {
    String value = getValue("tetris.color." + key, def);
    Color color;

    color = parseColor(value);
    if (color != null) {
      return color;
    }
    color = parseColor(def);
    if (color != null) {
      return color;
    }
    else {
      return Color.white;
    }
  }

  private static Color parseColor(String value) {
    if (!value.startsWith("#")) {
      return null;
    }
    try {
      return new Color(Integer.parseInt(value.substring(1), 16));
    }
    catch (NumberFormatException ignore) {
      return null;
    }
  }
}

