package com.travelaux.location;

import com.google.api.client.util.Key;

import java.io.Serializable;

/**
 * A class used by the Google Places JSON parser to hold details
 * about the place's review
 * <p>
 * Implement this class from "Serializable"
 * So that you can pass this class Object to another using Intents
 * Otherwise you can't pass to another activity
 */
public class Review implements Serializable {

    private static final long serialVersionUID = 1L;

    @Key
    public String text;

    @Key
    public String author_name;

    @Key
    public int time;

}
