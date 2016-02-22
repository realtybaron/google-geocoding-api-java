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
import java.util.concurrent.ExecutionException;

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
  public static GeocodingResponse findByAddress(String address) throws IOException {
    try {
      return addressCache.get(address);
    } catch (ExecutionException e) {
      log.debug(e.getMessage());
      throw new IOException(e);
    }
  }

  /**
   * Use geocoordinates to determine nearest address
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
      HttpRequest request = factory.buildGetRequest(url);
      return request.execute().parseAs(GeocodingResponse.class);
    } catch (HttpResponseException e) {
      log.debug(e.getStatusMessage());
      throw e;
    }
  }

  /**
   * Logger
   */
  private static final Logger log;
  /**
   * Gson factory
   */
  private static final GsonFactory gsonFactory = new GsonFactory();
  /**
   * Request Factory
   */
  private static final HttpRequestFactory factory;
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
        log.error(e.getMessage(), e);
        return null;
      }
    }
  });

  /**
   * Initialize class
   */
  static {
    // build logger
    log = LoggerFactory.getLogger(GoogleGeocoding.class);
    // build transport
    HttpTransport transport = new NetHttpTransport();
    factory = transport.createRequestFactory(new HttpRequestInitializer() {
      @Override
      public void initialize(HttpRequest request) {
        request.setParser(new JsonObjectParser(gsonFactory));
      }
    });
  }
}
