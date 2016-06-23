package com.socotech.location;

import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.gson.GsonFactory;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
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
    /**
     * Set key
     *
     * @param key server key
     */
    public static void setKey(String key) {
        GoogleGeocoding.key = key;
    }

    /**
     * Rebuild transport using a proxy
     *
     * @param proxy proxy server
     */
    public static void useProxy(String proxy) {
        NetHttpTransport.Builder builder = new NetHttpTransport.Builder();
        try {
            builder.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(Inet4Address.getByName(proxy), 80)));
        } catch (UnknownHostException e) {
            logger.warn(e.getMessage(), e);
        }
    }

    /**
     * Use free-text address to determine geo location
     *
     * @param address free-text address
     * @return geometry
     * @throws IOException in case of failure
     */
    public static GeocodingResponse findByAddress(String address) throws IOException {
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
    public static GeocodingResponse findByGeometry(BigDecimal lat, BigDecimal lng) throws IOException {
        lat = lat.setScale(6, RoundingMode.HALF_EVEN);
        lng = lng.setScale(6, RoundingMode.HALF_EVEN);
        try {
            GeocodingUrl url = GeocodingUrl.get(lat, lng);
            url.key = key;
            HttpRequest request = factory.buildGetRequest(url);
            return request.execute().parseAs(GeocodingResponse.class);
        } catch (HttpResponseException e) {
            logger.debug(e.getStatusMessage());
            throw e;
        }
    }

    /**
     * Key
     */
    private static String key;
    /**
     * Logger
     */
    private static Logger logger;
    /**
     * Gson factory
     */
    private static GsonFactory gsonFactory = new GsonFactory();
    /**
     * Request Factory
     */
    private static HttpRequestFactory factory;
    /**
     * cache used to reduce total requests to Google's Geocoding service
     */
    private static LoadingCache<String, GeocodingResponse> addressCache = CacheBuilder.newBuilder().maximumSize(100).build(new CacheLoader<String, GeocodingResponse>() {
        @Override
        public GeocodingResponse load(String address) throws Exception {
            try {
                GeocodingUrl url = GeocodingUrl.get(address);
                HttpRequest request = factory.buildGetRequest(url);
                return request.execute().parseAs(GeocodingResponse.class);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return null;
            }
        }
    });

    /**
     * Initialize class
     */
    static {
        // build logger
        logger = LoggerFactory.getLogger(GoogleGeocoding.class);
        // build transport
        HttpTransport transport = new NetHttpTransport();
        factory = transport.createRequestFactory(new HttpRequestInitializer() {
            public void initialize(HttpRequest request) {
                request.setParser(new JsonObjectParser(gsonFactory));
            }
        });
    }
}
