package com.socotech.location;

/**
 * Created by IntelliJ IDEA. User: marc Date: Jan 31, 2011 Time: 6:06:27 AM
 */
public enum LocationType {
  /**
   * indicates that the returned result is a precise geocode for which we have location information accurate down to street address precision
   */
  ROOFTOP,
  /**
   * indicates that the returned result is approximate
   */
  APPROXIMATE,
  /**
   * indicates that the returned result is the geometric center of a result such as a polyline (for example, a street) or polygon (region)
   */
  GEOMETRIC_CENTER,
  /**
   * indicates that the returned result reflects an approximation (usually on a road) interpolated between two precise points (such as intersections). Interpolated results are generally returned when
   * rooftop geocodes are unavailable for a street address
   */
  RANGE_INTERPOLATED
}
