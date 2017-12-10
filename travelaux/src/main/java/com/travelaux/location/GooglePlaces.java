package com.travelaux.location;

import android.util.Log;

import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpParser;
import com.google.api.client.json.jackson.JacksonFactory;

import org.apache.http.client.HttpResponseException;

/**
 * The class that is responsible for making API calls to the Google Places server and
 * parsing its JSON responses
 */
@SuppressWarnings("deprecation")
public class GooglePlaces {

    //Create instance of the HTTP transport.
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    // Google API Key
    private static final String API_KEY = "AIzaSyCQdNgPtfBx2ROY-5nH5rXhhiisiStywC8";

    // Google Places serach url's
    private static final String PLACES_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/search/json?";
    private static final String PLACES_DETAILS_URL = "https://maps.googleapis.com/maps/api/place/details/json?";

    //The current position
    private double _latitude;
    private double _longitude;
    private double _radius;


    /**
     * Searching places
     *
     * @param latitude  - latitude of place
     * @param longitude - longitude of place
     * @param radius    - radius of searchable area
     * @param types     - type of place to search
     * @param keywords  - name of place to search
     * @return list of places
     */
    public PlacesList search(double latitude, double longitude, double radius, String keyword)
            throws Exception {

        this._latitude = latitude;
        this._longitude = longitude;
        this._radius = radius;

        try {

            //Create the HTTP request
            HttpRequestFactory httpRequestFactory = createRequestFactory(HTTP_TRANSPORT);
            HttpRequest request = httpRequestFactory.buildGetRequest(new GenericUrl(PLACES_SEARCH_URL));
            request.getUrl().put("key", API_KEY);
            request.getUrl().put("location", _latitude + "," + _longitude);
            request.getUrl().put("radius", _radius); // in meters
            request.getUrl().put("sensor", "false");
            request.getUrl().put("keyword", keyword);

            //Parse the response and instantize it as a PlacesList class
            PlacesList list = request.execute().parseAs(PlacesList.class);

            // Check log cat for places response status
            Log.d("Places Status", "" + list.status);

            //Return the parsed list
            return list;

        } catch (HttpResponseException e) {
            Log.e("Error:", e.getMessage());
            return null;
        }

    }

    /**
     * Searching single place full details
     *
     * @param refrence - reference id of place, which you will get in the search api request
     */
    public PlaceDetails getPlaceDetails(String reference) throws Exception {
        try {

            //Build the HTTP request
            HttpRequestFactory httpRequestFactory = createRequestFactory(HTTP_TRANSPORT);
            HttpRequest request = httpRequestFactory.buildGetRequest(new GenericUrl(PLACES_DETAILS_URL));
            request.getUrl().put("key", API_KEY);
            request.getUrl().put("reference", reference);
            request.getUrl().put("sensor", "false");

            //Parse and return the place as a PlaceDetails class
            PlaceDetails place = request.execute().parseAs(PlaceDetails.class);
            return place;

        } catch (HttpResponseException e) {
            Log.e("Error in Perform Details", e.getMessage());
            throw e;
        }
    }

    /**
     * Creating http request Factory
     */
    public static HttpRequestFactory createRequestFactory(final HttpTransport transport) {
        return transport.createRequestFactory(request -> {

            //Add the headers
            GoogleHeaders headers = new GoogleHeaders();
            headers.setApplicationName("TravelAux");

            //Create the parser and add it to the request
            request.setHeaders(headers);
            JsonHttpParser parser = new JsonHttpParser(new JacksonFactory());
            request.addParser(parser);
        });
    }
}