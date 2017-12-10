package com.travelaux.location;

import com.google.api.client.util.Key;

import java.io.Serializable;

/**
 * A class used by the FourSquare JSON parser to hold details
 * about an individual venue (place)
 * <p>
 * Implement this class from "Serializable"
 * So that you can pass this class Object to another using Intents
 * Otherwise you can't pass to another actitivy
 */
public class Venue {

    @Key
    public Response response;

    public static class Response implements Serializable {

        private static final long serialVersionUID = 1L;
        @Key
        public Venues venue;

        public static class Venues implements Serializable {

            private static final long serialVersionUID = 1L;

            @Key
            public String id;

            @Key
            public String name;

            @Key
            public double rating;

            @Key
            public Tips tips;

            public static class Tips implements Serializable {

                private static final long serialVersionUID = 1L;
                @Key
                public Groups[] groups;

                public static class Groups implements Serializable {

                    private static final long serialVersionUID = 1L;
                    @Key
                    public Tip[] items;
                }
            }
        }
    }

    @Override
    public String toString() {
        return response.venue.name + " - " + response.venue.id;
    }
}
