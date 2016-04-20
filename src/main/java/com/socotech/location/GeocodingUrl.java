package com.socotech.location;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.util.Key;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA. User: marc Date: Jan 14, 2011 Time: 6:17:51 AM
 */
public class GeocodingUrl extends GenericUrl {
    @Key
    public String key;
    @Key
    public String latlng;
    @Key
    public String sensor;
    @Key
    public String address;

    /**
     * Construct a new
     *
     * @param s url pattern
     */
    public GeocodingUrl(String s) {
        super(s);
        this.sensor = "false";
    }

    public static GeocodingUrl get() {
        return new GeocodingUrl("http://maps.googleapis.com/maps/api/geocode/json");
    }

    public static GeocodingUrl get(String address) {
        GeocodingUrl result = get();
        result.address = address;
        return result;
    }

    public static GeocodingUrl get(BigDecimal lat, BigDecimal lng) {
        GeocodingUrl result = get();
        result.latlng = lat.toPlainString() + "," + lng.toPlainString();
        return result;
    }
}
