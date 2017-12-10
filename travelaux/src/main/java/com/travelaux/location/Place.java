package com.travelaux.location;

import com.google.api.client.util.Key;

import java.io.Serializable;
import java.util.List;

/**
 * A class used by the Google Places JSON parser to hold details
 * about an individual place
 * <p>
 * Implement this class from "Serializable"
 * So that you can pass this class Object to another using Intents
 * Otherwise you can't pass to another actitivy
 */
public class Place implements Serializable {

    private static final long serialVersionUID = 1L;

    @Key
    public String id;

    @Key
    public String name;

    @Key
    public String reference;

    @Key
    public Geometry geometry;

    @Key
    public String formatted_address;

    @Key
    public String formatted_phone_number;

    @Key
    public List<Review> reviews;

    @Key
    public double rating;

    @Key
    public String[] types;

    @Override
    public String toString() {
        return name + " - " + id + " - " + reference;
    }

    public static class Geometry implements Serializable {
        private static final long serialVersionUID = 1L;

        @Key
        public Location location;
    }

    public static class Location implements Serializable {
        private static final long serialVersionUID = 1L;

        @Key
        public double lat;

        @Key
        public double lng;
    }

}