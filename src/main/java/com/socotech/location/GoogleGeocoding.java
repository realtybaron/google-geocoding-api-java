package com.socotech.location;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

/**
 * Created by IntelliJ IDEA. User: marc Date: Jan 14, 2011 Time: 6:13:33 AM
 */
public class GoogleGeocoding {

    private GeoApiContext context;
    private LoadingCache<String, GeocodingResponse> addressCache;

    /**
     * Use free-text address to determine geo location
     *
     * @param address free-text address
     * @return geometry
     * @throws IOException in case of failure
     */
    public GeocodingResponse findByAddress(String address) throws IOException {
        try {
            return addressCache.get(address);
        } catch (ExecutionException e) {
            logger.debug(e.getMessage());
            throw new IOException(e);
        }
    }

    /**
     * Use geo-coordinates to determine nearest address
     *
     * @param lat latitude
     * @param lng longitude
     * @return address
     * @throws IOException in case of failure
     */
    public GeocodingResponse findByGeometry(BigDecimal lat, BigDecimal lng) throws Exception {
        lat = lat.setScale(6, RoundingMode.HALF_EVEN);
        lng = lng.setScale(6, RoundingMode.HALF_EVEN);
        LatLng latLng = new LatLng(lat.doubleValue(), lng.doubleValue());
        GeocodingResult[] results = GeocodingApi.reverseGeocode(context, latLng).await();
        return new GeocodingResponse(results);
    }

    /**
     * Build internal address cache
     */
    private void buildCache(int size) {
        addressCache = CacheBuilder.newBuilder().maximumSize(size).build(new CacheLoader<String, GeocodingResponse>() {
            @Override
            public GeocodingResponse load(String address) {
                try {
                    GeocodingResult[] results = GeocodingApi.geocode(context, address).await();
                    return new GeocodingResponse(results);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    return null;
                }
            }
        });
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
            geocoder.buildCache(cacheSize);
            // return product
            return geocoder;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(GoogleGeocoding.class);
}
