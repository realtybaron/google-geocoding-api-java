package com.socotech.location;

import com.google.maps.model.Geometry;
import com.google.maps.model.LocationType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Created by IntelliJ IDEA. User: marc Date: Jan 14, 2011 Time: 6:49:12 AM
 */
public class GoogleGeocodingTest {

    private GoogleGeocoding googleGeocoding;

    @Before
    public void onSetup() {
        this.googleGeocoding = new GoogleGeocoding.Builder().apiKey("AIzaSyCgk-CIEFxwdBbWxM3VVu0EDwBIEq4ZjpE").build();
    }

    @Test
    public void testAltLocality() throws Exception {
        Assert.assertEquals("Address is wrong", "2901 Dolan Road, Drummonds, TN 38023, United States", googleGeocoding.findByAddress("2901 dolan road drummonds tn").address().get().toString());
        Assert.assertEquals("Address is wrong", "6839 Minuteman Trail, Derby, NY 14047, United States", googleGeocoding.findByAddress("6839 minuteman derby ny").address().get().toString());
        Assert.assertEquals("Address is wrong", "1672 Digger Tree Court, Cool, CA 95614, United States", googleGeocoding.findByAddress("1672 digger tree ct cool ca").address().get().toString());
    }

    @Test
    public void testPreciseAddress() throws Exception {
        GeocodingResponse response = googleGeocoding.findByAddress("1316 Kenwood Avenue Austin, TX 78704");
        Optional<Geometry> geometry = response.geometry();
        Assert.assertTrue("Geometry is null", geometry.isPresent());
        Assert.assertTrue("Address is imprecise", response.isPrecise());
        Assert.assertEquals("Latitude is invalid", 30.2463, geometry.get().location.lat, 0.0001);
        Assert.assertEquals("Longitude is invalid", -97.7386, geometry.get().location.lng, 0.0001);
    }

    @Test
    public void testImpreciseAddress() throws Exception {
        Optional<Geometry> geometry = googleGeocoding.findByAddress("Kenwood Avenue Austin, TX 78704").geometry();
        Assert.assertTrue("Geometry is null", geometry.isPresent());
        Assert.assertEquals("Unable to geocode", LocationType.GEOMETRIC_CENTER.name(), geometry.get().locationType.name());
    }

    @Test
    public void testSubUnitAddress() throws Exception {
        GeocodingResponse response = googleGeocoding.findByAddress("505 West End Avenue Unit 11B New York, NY 10024");
        Assert.assertTrue("Sub Unit Address is imprecise", response.isPrecise());
        Assert.assertTrue("Address is null", response.address().isPresent());
        Assert.assertEquals("Unable to parse unit number", "11B", response.address().get().unitNumber);
    }

    @Test
    public void testReverseGeocoding() throws Exception {
        BigDecimal lat = BigDecimal.valueOf(30.2463621), lng = BigDecimal.valueOf(-97.7386784);
        Optional<Address> address = googleGeocoding.findByGeometry(lat, lng).address();
        Assert.assertTrue("Address is null", address.isPresent());
        Assert.assertEquals("Unable to determine city", "Austin", address.get().city);
        Assert.assertEquals("Unable to determine country code", "US", address.get().countryCode);
        Assert.assertEquals("Unable to determine country", "United States", address.get().country);
        Assert.assertEquals("Unable to determine state name", "Texas", address.get().stateName);
        Assert.assertEquals("Unable to determine postal code", "78704", address.get().postalCode);
        Assert.assertEquals("Unable to determine street number", "1316", address.get().streetNumber);
        Assert.assertEquals("Unable to determine street name", "Kenwood Avenue", address.get().streetName);
        Assert.assertEquals("Unable to determine state abbreviation", "TX", address.get().stateAbbreviation);
        Assert.assertEquals("Unable to construct address line 1", "1316 Kenwood Avenue", address.get().line1());
        Assert.assertEquals("Unable to construct address line 2", "Austin, TX 78704", address.get().line2());
    }
}