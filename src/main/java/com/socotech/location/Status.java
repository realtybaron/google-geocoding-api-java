package com.socotech.location;

/**
 * Created by IntelliJ IDEA. User: marc Date: Jan 31, 2011 Time: 6:25:00 AM
 */
public enum Status {
  /**
   * indicates that no errors occurred; the address was successfully parsed and at least one geocode was returned
   */
  OK,
  /**
   * indicates that the geocode was successful but returned no results. This may occur if the geocode was passed a non-existent address or a latlng in a remote location
   */
  ZERO_RESULTS,
  /**
   * indicates that your request was denied, generally because of lack of a sensor parameter
   */
  REQUEST_DENIED,
  /**
   * generally indicates that the query (address or latlng) is missing
   */
  INVALID_REQUEST,
  /**
   * indicates that you are over your quota
   */
  OVER_QUERY_LIMIT
}
