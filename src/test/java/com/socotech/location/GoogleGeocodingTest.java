package com.socotech.location;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA. User: marc Date: Jan 14, 2011 Time: 6:49:12 AM
 */
public class GoogleGeocodingTest {
    @Test
    public void testAltLocality() throws Exception {
        Assert.assertEquals("Address is wrong", "2901 Dolan Road, Drummonds, TN 38023, United States", GoogleGeocoding.findByAddress("2901 dolan road drummonds tn").address().toString());
        Assert.assertEquals("Address is wrong", "6839 Minuteman Trail, Derby, NY 14047, United States", GoogleGeocoding.findByAddress("6839 minuteman derby ny").address().toString());
        Assert.assertEquals("Address is wrong", "1672 Digger Tree Court, Cool, CA 95614, United States", GoogleGeocoding.findByAddress("1672 digger tree ct cool ca").address().toString());
    }

    @Test
    public void testPreciseAddress() throws Exception {
        GeocodingResponse response = GoogleGeocoding.findByAddress("1316 Kenwood Avenue Austin, TX 78704");
        Geometry geometry = response.geometry();
        Assert.assertTrue("Address is imprecise", response.isPrecise());
        Assert.assertEquals("Latitude is invalid", 30.2463, geometry.location.latitude.floatValue(), 0.0001);
        Assert.assertEquals("Longitude is invalid", -97.7386, geometry.location.longitude.floatValue(), 0.0001);
    }

    @Test
    public void testImpreciseAddress() throws Exception {
        Geometry geometry = GoogleGeocoding.findByAddress("Kenwood Avenue Austin, TX 78704").geometry();
        Assert.assertEquals("Unable to geocode", LocationType.GEOMETRIC_CENTER.name(), geometry.locationType);
    }

    @Test
    public void testSubUnitAddress() throws Exception {
        GeocodingResponse response = GoogleGeocoding.findByAddress("505 West End Avenue Unit 11B New York, NY 10024");
        Assert.assertTrue("Sub Unit Address is imprecise", response.isPrecise("11B"));
        Assert.assertEquals("Unable to parse unit number", "11b", response.address().unitNumber);
    }

    @Test
    public void testReverseGeocoding() throws Exception {
        BigDecimal lat = BigDecimal.valueOf(30.2463500), lng = BigDecimal.valueOf(-97.7386490);
        Address address = GoogleGeocoding.findByGeometry(lat, lng).address();
        Assert.assertEquals("Unable to determine city", "Austin", address.city);
        Assert.assertEquals("Unable to determine country code", "US", address.countryCode);
        Assert.assertEquals("Unable to determine country", "United States", address.country);
        Assert.assertEquals("Unable to determine state name", "Texas", address.stateName);
        Assert.assertEquals("Unable to determine postal code", "78704", address.postalCode);
        Assert.assertEquals("Unable to determine street number", "1316", address.streetNumber);
        Assert.assertEquals("Unable to determine street name", "Kenwood Avenue", address.streetName);
        Assert.assertEquals("Unable to determine state abbreviation", "TX", address.stateAbbreviation);
        Assert.assertEquals("Unable to construct address line 1", "1316 Kenwood Avenue", address.line1());
        Assert.assertEquals("Unable to construct address line 2", "Austin, TX 78704", address.line2());
    }
}