package com.socotech.timezone;

import java.util.Optional;
import java.util.TimeZone;

/**
 * Created by IntelliJ IDEA. User: marc Date: Jan 14, 2011 Time: 6:06:32 AM
 */
public class TimeZoneResponse {

    private final boolean success;
    private final TimeZone result;

    /**
     * Default constructor
     *
     * @param success request status
     * @param result  geocoding results
     */
    public TimeZoneResponse(boolean success, TimeZone result) {
        this.result = result;
        this.success = success;
    }

    /**
     * Result status
     *
     * @return true, if request was successful
     */
    public boolean isSuccess() {
        return success;
    }

    public Optional<TimeZone> getResult() {
        return Optional.ofNullable(result);
    }

    public static final TimeZoneResponse EMPTY = new TimeZoneResponse(false, null);

}
