package com.socotech.location;

import com.google.api.client.util.Key;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA. User: marc Date: Jan 31, 2011 Time: 6:05:21 AM
 */
public class Location {
  @Key("lat")
  public BigDecimal latitude;
  @Key("lng")
  public BigDecimal longitude;
}
