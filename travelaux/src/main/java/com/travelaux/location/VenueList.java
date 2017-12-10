package com.travelaux.location;

import com.google.api.client.util.Key;

import java.io.Serializable;

/**
 * Implement this class from "Serializable"
 * So that you can pass this class Object to another using Intents
 * Otherwise you can't pass to another activity
 */
public class VenueList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Key
    public Meta meta;

    @Key
    public Response response;

    public static class Meta implements Serializable {

        private static final long serialVersionUID = 1L;
        @Key
        public int code;
    }

    public static class Response implements Serializable {

        private static final long serialVersionUID = 1L;
        @Key
        public Venues[] venues;

        public static class Venues implements Serializable {

            private static final long serialVersionUID = 1L;
            @Key
            public String id;
        }
    }

    @Override
    public String toString() {
        return String.valueOf(meta.code);
    }
}
