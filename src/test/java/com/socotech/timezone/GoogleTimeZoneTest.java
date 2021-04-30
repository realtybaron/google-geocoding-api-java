package com.socotech.timezone;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA. User: marc Date: Jan 14, 2011 Time: 6:49:12 AM
 */
public class GoogleTimeZoneTest {

    private GoogleTimeZoner googleTimeZoner;

    @Before
    public void onSetup() {
        this.googleTimeZoner = new GoogleTimeZoner.Builder().apiKey("AIzaSyCY82JPV99c78wbu-ddzPvpk2silESSiHk").build();
    }

    @Test
    public void testValid() {
        BigDecimal lat = BigDecimal.valueOf(30.2463621), lng = BigDecimal.valueOf(-97.7386784);
        TimeZoneResponse response = googleTimeZoner.findByLatLng(lat, lng);
        Assert.assertTrue("Request failed", response.isSuccess() && response.getResult().isPresent());
        Assert.assertEquals("Unable to determine time zone", "America/Chicago", response.getResult().get().getID());
    }
}