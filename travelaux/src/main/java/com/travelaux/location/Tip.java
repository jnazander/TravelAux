package com.travelaux.location;

import com.google.api.client.util.Key;

import java.io.Serializable;

/**
 * A class used by the FourSquare JSON parser to hold details
 * about the place's tips (reviws)
 * <p>
 * Implement this class from "Serializable"
 * So that you can pass this class Object to another using Intents
 * Otherwise you can't pass to another activity
 */
public class Tip {

    @Key
    public String text;

    @Key
    public User user;

    public static class User implements Serializable {

        private static final long serialVersionUID = 1L;

        @Key
        public String firstName;

        @Key
        public String lastName;
    }

    @Key
    public int createdAt;

}
