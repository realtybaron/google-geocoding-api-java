package com.socotech.location;

import com.google.api.client.util.Key;

/**
 * Created by IntelliJ IDEA. User: marc Date: Jan 31, 2011 Time: 5:53:06 AM
 */
public class Geometry {
  @Key("location_type")
  public String locationType;
  @Key
  public Location location;
}
