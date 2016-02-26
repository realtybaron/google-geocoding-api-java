package com.socotech.location;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by IntelliJ IDEA. User: marc Date: Jan 14, 2011 Time: 6:06:32 AM
 */
public class GeocodingResult extends GenericJson {
    @Key("formatted_address")
    public String formattedAddress;
    @Key("partial_match")
    public boolean partial;
    @Key
    public Geometry geometry;
    @Key("types")
    public List<String> types = Lists.newArrayList();
    @Key("address_components")
    public List<AddressComponent> components = Lists.newArrayList();
}
