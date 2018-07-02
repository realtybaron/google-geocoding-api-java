package com.socotech.location;

import com.google.api.client.util.Key;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by IntelliJ IDEA. User: marc Date: Jan 14, 2011 Time: 6:06:32 AM
 */
public class GeocodingResponse {
    @Key("status")
    public String status;
    /**
     * Holds address components for subsequent retrieval
     */
    private Address address;
    /**
     * Holds geometry components for subsequent retrieval
     */
    private Geometry geometry;
    /**
     * Geocoding results
     */
    @Key("results")
    public List<GeocodingResult> results = Lists.newArrayList();

    /**
     * Determine if address is precise
     *
     * @return true, if not a partial match
     */
    @Deprecated
    public boolean isPrecise() {
        Optional<GeocodingResult> result = this.findResult();
        if (!result.isPresent()) {
            return false;
        } else {
            List<String> types = result.get().types;
            List<ResultType> typesAsEnums = types.stream().map(ResultType::valueOf).collect(Collectors.toList());
            return !Collections.disjoint(typesAsEnums, ResultType.PRECISE);
        }
    }

    /**
     * Determine if address is precise
     *
     * @param unit unit or apt number
     * @return true, if not a partial match
     */
    public boolean isPrecise(String unit) {
        Optional<GeocodingResult> result = this.findResult();
        if (!result.isPresent()) {
            return false;
        } else if (Strings.isNullOrEmpty(unit)) {
            List<String> types = result.get().types;
            return types.contains(ResultType.street_address.name());
        } else {
            List<String> types = result.get().types;
            return types.contains(ResultType.subpremise.name());
        }
    }

    /**
     * Get as geometry
     *
     * @return geometry
     */
    @Nullable
    public Geometry geometry() {
        if (this.geometry == null) {
            Optional<GeocodingResult> result = this.findResult();
            result.ifPresent(geocodingResult -> this.geometry = geocodingResult.geometry);
        }
        return this.geometry;
    }

    /**
     * Parse response into an Address
     *
     * @return address
     */
    @Nullable
    public Address address() {
        if (this.address == null) {
            Optional<GeocodingResult> result = this.findResult();
            result.ifPresent(geocodingResult -> this.address = new Address().copyFrom(geocodingResult));
        }
        return this.address;
    }

    private Optional<GeocodingResult> findResult() {
        Optional<GeocodingResult> rooftop = results.stream().filter(input -> input.geometry.locationType.equalsIgnoreCase("ROOFTOP")).findFirst();
        Optional<GeocodingResult> interpolated = results.stream().filter(input -> input.geometry.locationType.equalsIgnoreCase("RANGE_INTERPOLATED")).findFirst();
        Optional<GeocodingResult> geometricCenter = results.stream().filter(input -> input.geometry.locationType.equalsIgnoreCase("GEOMETRIC_CENTER")).findFirst();
        if (rooftop.isPresent()) {
            return rooftop;
        } else if (interpolated.isPresent()) {
            return interpolated;
        } else {
            return geometricCenter;
        }
    }
}
