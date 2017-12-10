package com.travelaux.location;

import com.google.api.client.util.Key;

import java.io.Serializable;
import java.util.List;

/**
 * A class used by the Google Places JSON parser to hold details
 * about the retrieved list of places
 * <p>
 * Implement this class from "Serializable"
 * So that you can pass this class Object to another using Intents
 * Otherwise you can't pass to another actitivy
 */
public class PlacesList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Key
    public String status;

    @Key
    public List<Place> results;

}