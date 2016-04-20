package com.socotech.location;

import com.google.api.client.util.Key;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by IntelliJ IDEA. User: marc Date: Jan 14, 2011 Time: 6:06:32 AM
 */
public class GeocodingResponse {
    @Key
    public String status;
    @Key("results")
    public List<GeocodingResult> results = Lists.newArrayList();
    /**
     * Holds address components for subsequent retrieval
     */
    private Address address;

    /**
     * Determine if address is precise
     *
     * @return true, if not a partial match
     */
    @Deprecated
    public boolean isPrecise() {
        if (this.results.isEmpty()) {
            return false;
        } else {
            List<String> types = this.results.iterator().next().types;
            return types.contains(ResultType.street_address.name());
        }
    }

    /**
     * Determine if address is precise
     *
     * @param unit unit or apt number
     * @return true, if not a partial match
     */
    public boolean isPrecise(String unit) {
        if (this.results.isEmpty()) {
            return false;
        } else if (Strings.isNullOrEmpty(unit)) {
            List<String> types = this.results.iterator().next().types;
            return types.contains(ResultType.street_address.name());
        } else {
            List<String> types = this.results.iterator().next().types;
            return types.contains(ResultType.subpremise.name());
        }
    }

    /**
     * Get as geometry
     *
     * @return geometry
     */
    public Geometry geometry() {
        return this.results.iterator().next().geometry;
    }

    /**
     * Parse response into an Address
     *
     * @return address
     */
    public Address address() {
        if (this.address == null) {
            this.address = new Address();
            for (GeocodingResult result : this.results) {
                for (AddressComponent component : result.components) {
                    if (component.types.contains(ResultType.route.name())) {
                        this.address.streetName = component.longName;
                    } else if (component.types.contains(ResultType.country.name())) {
                        this.address.country = component.longName;
                        this.address.countryCode = component.shortName;
                    } else if (component.types.contains(ResultType.subpremise.name())) {
                        this.address.unitNumber = component.longName;
                    } else if (component.types.contains(ResultType.postal_code.name())) {
                        this.address.postalCode = component.longName;
                    } else if (component.types.contains(ResultType.street_number.name())) {
                        this.address.streetNumber = component.longName;
                    } else if (component.types.contains(ResultType.administrative_area_level_1.name())) {
                        this.address.stateName = component.longName;
                        this.address.stateAbbreviation = component.shortName;
                    }
                    // resolve city
                    if (Strings.isNullOrEmpty(this.address.city)) {
                        if (component.types.contains(ResultType.locality.name())) {
                            this.address.city = component.longName;
                        } else if (component.types.contains(ResultType.sublocality.name())) {
                            this.address.city = component.longName;
                        } else if (component.types.contains(ResultType.administrative_area_level_2.name())) {
                            this.address.city = component.longName;
                        }
                    }
                }
            }
        }
        return this.address;
    }
}
