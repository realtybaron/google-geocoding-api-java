package com.socotech.location;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by IntelliJ IDEA. User: marc Date: Jan 14, 2011 Time: 5:56:54 AM
 */
public class AddressComponent extends GenericJson {
  @Key("long_name")
  public String longName;
  @Key("short_name")
  public String shortName;
  @Key("types")
  public List<String> types = Lists.newArrayList();
}
