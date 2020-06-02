package com.socotech.location;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.UnknownHostException;

/**
 * Created by IntelliJ IDEA. User: marc Date: Jan 14, 2011 Time: 6:13:33 AM
 */
public class GoogleGeocoding {

    private GeoApiContext context;
    private LoadingCache<LatLng, GeocodingResponse> latLngCache;
    private LoadingCache<String, GeocodingResponse> addressCache;
    private LoadingCache<String, GeocodingResponse> countryCache;

    /**
     * Use free-text address to determine geo location
     *
     * @param address free-text address
     * @return geometry
     */
    public GeocodingResponse findByAddress(String address) {
        return this.findByAddress(address, false);
    }

    /**
     * Use free-text address to determine geo location
     *
     * @param address free-text address
     * @return geometry
     */
    public GeocodingResponse findByAddress(String address, boolean refresh) {
        try {
            if (refresh) {
                addressCache.invalidate(address);
            }
            return addressCache.get(address);
        } catch (Exception e) {
            return GeocodingResponse.EMPTY;
        }
    }

    /**
     * Use free-text address to determine geo location
     *
     * @param country free-text address
     * @return geometry
     */
    public GeocodingResponse findByCountry(String country) {
        return this.findByCountry(country, false);
    }

    /**
     * Use free-text address to determine geo location
     *
     * @param country free-text address
     * @return geometry
     */
    public GeocodingResponse findByCountry(String country, boolean refresh) {
        try {
            if (refresh) {
                countryCache.invalidate(country);
            }
            return countryCache.get(country);
        } catch (Exception e) {
            return GeocodingResponse.EMPTY;
        }
    }

    /**
     * Use geo-coordinates to determine nearest address
     *
     * @param lat latitude
     * @param lng longitude
     * @return address
     */
    public GeocodingResponse findByGeometry(BigDecimal lat, BigDecimal lng) {
        return this.findByGeometry(lat, lng, false);
    }

    /**
     * Use geo-coordinates to determine nearest address
     *
     * @param lat latitude
     * @param lng longitude
     * @return address
     */
    public GeocodingResponse findByGeometry(BigDecimal lat, BigDecimal lng, boolean refresh) {
        lat = lat.setScale(6, RoundingMode.HALF_EVEN);
        lng = lng.setScale(6, RoundingMode.HALF_EVEN);
        LatLng latLng = new LatLng(lat.doubleValue(), lng.doubleValue());
        try {
            if (refresh) {
                latLngCache.invalidate(latLng);
            }
            return latLngCache.get(latLng);
        } catch (Exception e) {
            return GeocodingResponse.EMPTY;
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

        public GoogleGeocoding build() {
            GoogleGeocoding geocoder = new GoogleGeocoding();
            // build transport
            geocoder.context = new GeoApiContext.Builder().apiKey(apiKey).build();
            // address cache
            geocoder.latLngCache = CacheBuilder.newBuilder().maximumSize(cacheSize).build(new CacheLoader<LatLng, GeocodingResponse>() {
                @Override
                public GeocodingResponse load(LatLng latLng) {
                    try {
                        GeocodingResult[] results = GeocodingApi.reverseGeocode(geocoder.context, latLng).await();
                        return new GeocodingResponse(true, results);
                    } catch (Exception e) {
                        return null;
                    }
                }
            });
            geocoder.addressCache = CacheBuilder.newBuilder().maximumSize(cacheSize).build(new CacheLoader<String, GeocodingResponse>() {
                @Override
                public GeocodingResponse load(String address) {
                    try {
                        GeocodingResult[] results = GeocodingApi.geocode(geocoder.context, address).await();
                        return new GeocodingResponse(true, results);
                    } catch (Exception e) {
                        return null;
                    }
                }
            });
            geocoder.countryCache = CacheBuilder.newBuilder().maximumSize(cacheSize).build(new CacheLoader<String, GeocodingResponse>() {
                @Override
                public GeocodingResponse load(String country) {
                    try {
                        GeocodingResult[] results = GeocodingApi.geocode(geocoder.context, String.join(" ", country, "country")).await();
                        return new GeocodingResponse(true, results);
                    } catch (Exception e) {
                        return null;
                    }
                }
            });
            // return product
            return geocoder;
        }
    }

}
