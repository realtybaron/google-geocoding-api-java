package com.socotech.location;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.gson.GsonFactory;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Created by IntelliJ IDEA. User: marc Date: Jan 14, 2011 Time: 6:13:33 AM
 */
public class GoogleGeocoding {
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
            logger.log(Level.SEVERE, e.getMessage(), e);
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
    public GeocodingResponse findByGeometry(BigDecimal lat, BigDecimal lng) throws IOException {
        lat = lat.setScale(6, RoundingMode.HALF_EVEN);
        lng = lng.setScale(6, RoundingMode.HALF_EVEN);
        try {
            GeocodingUrl url = GeocodingUrl.get(lat, lng);
            url.key = key;
            HttpRequest request = factory.buildGetRequest(url);
            return request.execute().parseAs(GeocodingResponse.class);
        } catch (HttpResponseException e) {
            logger.log(Level.INFO, e.getStatusMessage());
            throw e;
        }
    }

    /**
     * Build internal address cache
     */
    private void buildCache(int size) {
        addressCache = CacheBuilder.newBuilder().maximumSize(size).build(new CacheLoader<String, GeocodingResponse>() {
            @Override
            public GeocodingResponse load(String address) throws Exception {
                try {
                    GeocodingUrl url = GeocodingUrl.get(address);
                    HttpRequest request = factory.buildGetRequest(url);
                    return request.execute().parseAs(GeocodingResponse.class);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                    return null;
                }
            }
        });
    }

    /**
     * Key
     */
    private String key;
    /**
     * Logger
     */
    private Logger logger;
    /**
     * Request Factory
     */
    private HttpRequestFactory factory;
    /**
     * cache used to reduce total requests to Google's Geocoding service
     */
    private LoadingCache<String, GeocodingResponse> addressCache;

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
            // set key
            geocoder.key = apiKey;
            // build logger
            geocoder.logger = Logger.getLogger(GoogleGeocoding.class.getName());
            // build transport
            HttpTransport transport;
            if (appEngine) {
                transport = UrlFetchTransport.getDefaultInstance();
            } else {
                NetHttpTransport.Builder builder = new NetHttpTransport.Builder();
                if (proxy != null) {
                    builder.setProxy(proxy);
                }
                transport = builder.build();
            }
            // build factory
            geocoder.factory = transport.createRequestFactory(new HttpRequestInitializer() {
                public void initialize(HttpRequest request) {
                    request.setParser(new JsonObjectParser(new GsonFactory()));
                }
            });
            // address cache
            geocoder.buildCache(cacheSize);
            // return product
            return geocoder;
        }
    }
}
