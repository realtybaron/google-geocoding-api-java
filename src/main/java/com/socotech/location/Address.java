package com.socotech.location;

import com.google.common.base.Strings;

/**
 * Created by IntelliJ IDEA. User: marc Date: Jan 14, 2011 Time: 5:45:25 AM
 */
public class Address {
    public String city;
    public String country;
    public String stateName;
    public String postalCode;
    public String countryCode;
    public String streetName;
    public String unitNumber;
    public String streetNumber;
    public String stateAbbreviation;

    /**
     * Get line 1 of address
     *
     * @return line 1
     */
    public String line1() {
        StringBuilder builder = new StringBuilder();
        builder.append(streetNumber).append(' ').append(streetName);
        if (!Strings.isNullOrEmpty(unitNumber)) {
            builder.append(" #").append(unitNumber);
        }
        return builder.toString();
    }

    /**
     * Get line 2 of address
     *
     * @return line 2
     */
    public String line2() {
        return this.city + ", " + this.stateAbbreviation + ' ' + this.postalCode;
    }

    @Override
    public String toString() {
        return line1() + ", " + line2() + ", " + this.country;
    }
}
