package com.socotech.timezone;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.maps.GeoApiContext;
import com.google.maps.TimeZoneApi;
import com.google.maps.model.LatLng;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.UnknownHostException;
import java.util.TimeZone;

/**
 * Created by IntelliJ IDEA. User: marc Date: Jan 14, 2011 Time: 6:13:33 AM
 */
public class GoogleTimeZoner {

    private GeoApiContext context;
    private LoadingCache<LatLng, TimeZoneResponse> cache;

    public TimeZoneResponse findByLatLng(LatLng latLng) {
        return this.findByLatLng(latLng, false);
    }

    public TimeZoneResponse findByLatLng(LatLng latLng, boolean refresh) {
        try {
            if (refresh) {
                cache.invalidate(latLng);
            }
            return cache.get(latLng);
        } catch (Exception e) {
            return TimeZoneResponse.EMPTY;
        }
    }

    /**
     * Use geo-coordinates to determine nearest address
     *
     * @param lat latitude
     * @param lng longitude
     * @return address
     */
    public TimeZoneResponse findByLatLng(BigDecimal lat, BigDecimal lng) {
        return this.findByLatLng(lat, lng, false);
    }

    /**
     * Use geo-coordinates to determine nearest address
     *
     * @param lat latitude
     * @param lng longitude
     * @return address
     */
    public TimeZoneResponse findByLatLng(BigDecimal lat, BigDecimal lng, boolean refresh) {
        lat = lat.setScale(6, RoundingMode.HALF_EVEN);
        lng = lng.setScale(6, RoundingMode.HALF_EVEN);
        LatLng latLng = new LatLng(lat.doubleValue(), lng.doubleValue());
        try {
            if (refresh) {
                cache.invalidate(latLng);
            }
            return cache.get(latLng);
        } catch (Exception e) {
            return TimeZoneResponse.EMPTY;
        }
    }

    public static class Builder {
        private int cacheSize = 100;
        private Proxy proxy;
        private String apiKey;
        private boolean appEngine;

        public Builder proxy(String s, int i) throws UnknownHostException {
            Preconditions.checkNotNull(s, "Proxy address cannot be null");
            Preconditions.checkArgument(i > 0, "Proxy port is invalid");
            InetSocketAddress sa = new InetSocketAddress(Inet4Address.getByName(s), i);
            this.proxy = new Proxy(Proxy.Type.HTTP, sa);
            return this;
        }

        public Builder apiKey(String key) {
            this.apiKey = key;
            return this;
        }

        public Builder appEngine() {
            this.appEngine = true;
            return this;
        }

        public Builder cacheSize(int size) {
            this.cacheSize = size;
            return this;
        }

        public GoogleTimeZoner build() {
            GoogleTimeZoner googleTimeZone = new GoogleTimeZoner();
            // build transport
            googleTimeZone.context = new GeoApiContext.Builder().apiKey(apiKey).build();
            // address cache
            googleTimeZone.cache = CacheBuilder.newBuilder().maximumSize(cacheSize).build(new CacheLoader<LatLng, TimeZoneResponse>() {
                @Override
                public TimeZoneResponse load(LatLng latLng) {
                    try {
                        TimeZone result = TimeZoneApi.getTimeZone(googleTimeZone.context, latLng).await();
                        return new TimeZoneResponse(true, result);
                    } catch (Exception e) {
                        return null;
                    }
                }
            });
            // return product
            return googleTimeZone;
        }
    }

}
