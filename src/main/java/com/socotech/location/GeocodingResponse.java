package com.socotech.location;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.maps.model.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by IntelliJ IDEA. User: marc Date: Jan 14, 2011 Time: 6:06:32 AM
 */
public class GeocodingResponse {

    private boolean success;
    private Address address;
    private GeocodingResult[] results;

    /**
     * Default constructor
     *
     * @param success request status
     * @param results geocoding results
     */
    public GeocodingResponse(boolean success, GeocodingResult[] results) {
        this.success = success;
        this.results = results;
    }

    /**
     * Result status
     *
     * @return true, if request was successful
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Determine if address is precise
     *
     * @return true, if not a partial match
     */
    public boolean isPrecise() {
        if (this.results.length == 0) {
            return false;
        } else {
            Stream<GeocodingResult> stream = Arrays.stream(results);
            return stream.anyMatch((Predicate<GeocodingResult>) input -> !Collections.disjoint(Arrays.stream(input.types).collect(Collectors.toList()), PRECISE));
        }
    }

    /**
     * Get as geometry
     *
     * @return geometry
     */
    public Optional<Geometry> geometry() {
        Optional<Geometry> geometry = Optional.empty();
        Optional<AddressType> mostPrecise = Optional.empty();
        for (GeocodingResult result : results) {
            List<AddressType> types = Arrays.stream(result.types).collect(Collectors.toList());
            for (AddressType type : ORDER) {
                if (types.contains(type)) {
                    if (!mostPrecise.isPresent() || ORDER.indexOf(type) > ORDER.indexOf(mostPrecise.get())) {
                        geometry = Optional.of(result.geometry);
                        mostPrecise = Optional.of(type);
                    }
                }
            }
        }
        return geometry;
    }

    /**
     * Parse response into an Address
     *
     * @return address
     */
    public Optional<Address> address() {
        if (this.address == null) {
            Optional<AddressType> mostPrecise = Optional.empty();
            Optional<GeocodingResult> geocodingResult = Optional.empty();
            for (GeocodingResult result : results) {
                List<AddressType> types = Arrays.stream(result.types).collect(Collectors.toList());
                for (AddressType type : ORDER) {
                    if (types.contains(type)) {
                        if (!geocodingResult.isPresent() || ORDER.indexOf(type) > ORDER.indexOf(mostPrecise.get())) {
                            mostPrecise = Optional.of(type);
                            geocodingResult = Optional.of(result);
                        }
                    }
                }
            }
            if (geocodingResult.isPresent()) {
                this.address = new Address();
                for (AddressComponent component : geocodingResult.get().addressComponents) {
                    List<AddressComponentType> components = Lists.newArrayList(component.types);
                    if (components.contains(AddressComponentType.ROUTE)) {
                        this.address.streetName = component.longName;
                    } else if (components.contains(AddressComponentType.COUNTRY)) {
                        this.address.country = component.longName;
                        this.address.countryCode = component.shortName;
                    } else if (components.contains(AddressComponentType.SUBPREMISE)) {
                        this.address.unitNumber = component.longName;
                    } else if (components.contains(AddressComponentType.POSTAL_CODE)) {
                        this.address.postalCode = component.longName;
                    } else if (components.contains(AddressComponentType.STREET_NUMBER)) {
                        this.address.streetNumber = component.longName;
                    } else if (components.contains(AddressComponentType.NEIGHBORHOOD)) {
                        this.address.neighborhood = component.longName;
                    } else if (components.contains(AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_1)) {
                        this.address.stateName = component.longName;
                        this.address.stateAbbreviation = component.shortName;
                    } else if (components.contains(AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_2)) {
                        this.address.countyName = component.longName.replace(" County", "").trim();
                    }
                    // resolve city
                    if (Strings.isNullOrEmpty(this.address.city)) {
                        if (components.contains(AddressComponentType.LOCALITY)) {
                            this.address.city = component.longName;
                        } else if (components.contains(AddressComponentType.SUBLOCALITY)) {
                            this.address.city = component.longName;
                        } else if (components.contains(AddressComponentType.POSTAL_TOWN)) {
                            this.address.city = component.longName;
                        }
                    }
                }
            }
        }
        return Optional.ofNullable(this.address);
    }

    /**
     * Increasing order of precision
     */
    private static final List<AddressType> ORDER = Lists.newArrayList(
            AddressType.COUNTRY,
            AddressType.POLITICAL,
            AddressType.ADMINISTRATIVE_AREA_LEVEL_1,
            AddressType.POSTAL_CODE,
            AddressType.ADMINISTRATIVE_AREA_LEVEL_2,
            AddressType.SUBLOCALITY,
            AddressType.LOCALITY,
            AddressType.NEIGHBORHOOD,
            AddressType.ROUTE,
            AddressType.INTERSECTION,
            AddressType.STREET_ADDRESS,
            AddressType.SUBPREMISE,
            AddressType.PREMISE
    );

    public static final GeocodingResponse EMPTY = new GeocodingResponse(false, new GeocodingResult[0]);

    private static final Collection<AddressType> PRECISE = Lists.newArrayList(AddressType.PREMISE, AddressType.SUBPREMISE, AddressType.STREET_ADDRESS);
}
