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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * The class that is responsible for making API calls to the FourSquare server and
 * parsing its JSON responses
 */
@SuppressWarnings("deprecation")
public class FourSquare {

    //Create instance of the HTTP transport.
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    // FourSquare OAUTH Key for accessing reviews (not uploading)
    private static String DEF_OAUTH_KEY = "OL1DCU2LOVSYH4RIX3NTWKWJFWUJZTWE3QOIU41C0HNGG1MD";

    // Foursquare search url's
    private static final String PLACES_SEARCH_URL = "https://api.foursquare.com/v2/venues/search?";
    private static final String PLACES_DETAILS_URL = "https://api.foursquare.com/v2/venues/";
    private static final String NAME_URL = "https://api.foursquare.com/v2/users/self?";
    private static final String PLACES_SUBMIT_URL = "https://api.foursquare.com/v2/tips/add?";

    //Current latitude and longitude
    private double _latitude;
    private double _longitude;

    /**
     * Searching places
     *
     * @param latitude - latitude of place
     * @param radius   - radius of searchable area
     * @param types    - type of place to search
     * @param keywords - name of place to search
     * @return list of places
     * @params longitude - longitude of place
     */
    public Venue search(double latitude, double longitude, String keyword)
            throws Exception {

        //Hold the ID of the place
        this._latitude = latitude;
        this._longitude = longitude;

        //The ID of the place
        String id;

        //Search the list of places and retrieve the first one
        try {

            //Build the HTTP request
            HttpRequestFactory httpRequestFactory = createRequestFactory(HTTP_TRANSPORT);
            HttpRequest request = httpRequestFactory.buildGetRequest(new GenericUrl(PLACES_SEARCH_URL));
            request.getUrl().put("ll", _latitude + "," + _longitude);
            request.getUrl().put("limit", 1);
            request.getUrl().put("query", keyword);
            request.getUrl().put("oauth_token", DEF_OAUTH_KEY);
            request.getUrl().put("v", "20130110");

            //Parse the result
            VenueList list = request.execute().parseAs(VenueList.class);

            // Check log cat for places response status
            Log.d("Places Status", "" + list.meta.code);

            //Check if the place is found. If not, return empty handed
            if (list.response.venues != null) {
                id = list.response.venues[0].id;
            } else {
                return null;
            }

        } catch (HttpResponseException e) {
            Log.e("Error:", e.getMessage());
            return null;
        }

        //Search the single place full details
        try {

            //Build the HTTP request
            HttpRequestFactory httpRequestFactory = createRequestFactory(HTTP_TRANSPORT);
            HttpRequest request = httpRequestFactory.buildGetRequest(new GenericUrl(PLACES_DETAILS_URL + id + "?"));
            request.getUrl().put("oauth_token", DEF_OAUTH_KEY);
            request.getUrl().put("v", "20130110");

            //Parse the result
            Venue venue = request.execute().parseAs(Venue.class);
            return venue;

        } catch (HttpResponseException e) {
            Log.e("Error in Perform Details", e.getMessage());
            throw e;
        }
    }

    /**
     * Obtaining the logged-in user name for verification purposes
     *
     * @param token - the OAUTH token of the current user
     * @return String - the first and last names of the current user
     */
    public String getName(String token) {

        String name;

        //Create a query string for obtaining the user name
        try {

            //Build the HTTP request
            HttpRequestFactory httpRequestFactory = createRequestFactory(HTTP_TRANSPORT);
            HttpRequest request = httpRequestFactory.buildGetRequest(new GenericUrl(NAME_URL));
            request.getUrl().put("oauth_token", token);
            request.getUrl().put("v", "20130110");

            //Parse the retrieved JSON values using JSON objects (no classes needed)
            JSONObject json = new JSONObject(request.execute().parseAsString());

            //Halt if the code result is not 200
            if (json.getJSONObject("meta").getInt("code") != 200) {
                return null;
            }

            //Use a JSONObject to parse
            JSONObject user = json.getJSONObject("response").getJSONObject("user");
            name = user.getString("firstName") + " " + user.getString("lastName");

            // Check log cat for places response status
            Log.d("Logged-in name: ", name);

            //Return the generated name
            return name;

        } catch (HttpResponseException e) {
            Log.e("Error:", e.getMessage());
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * The function for submitting the review
     *
     * @param id    - The id of the place
     * @param token - The OAUTH token of the current user (from which name
     *              to send the review)
     * @param text  - The actual text of the review
     * @return the success of the review posting (true/false)
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public boolean submit(String id, String token, String text) throws InstantiationException, IllegalAccessException {

        //Create a query string for sending in the review
        //The review text will automatically be converted into a
        //URI-friendly format, inserting %-based octets where applicable
        //adhering to the UTF-8 standard.
        try {

            HttpRequestFactory httpRequestFactory = createRequestFactory(HTTP_TRANSPORT);
            HttpRequest request = httpRequestFactory.buildPostRequest(new GenericUrl(PLACES_SUBMIT_URL), null);
            request.getUrl().put("venueId", id);
            request.getUrl().put("text", text);
            request.getUrl().put("oauth_token", token);
            request.getUrl().put("v", "20130110");

            //Parse the retrieved JSON values using JSON objects (no classes needed)
            JSONObject json = new JSONObject(request.execute().parseAsString());

            if (json.getJSONObject("meta").getInt("code") != 200) {
                System.out.println("Error! Did not post, the code is " + json.getJSONObject("meta").getInt("code"));
                return false;
            }

            //Return the generated name
            return true;

        } catch (HttpResponseException e) {
            Log.e("Error:", e.getMessage());
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Creating http request Factory
     */
    public static HttpRequestFactory createRequestFactory(final HttpTransport transport) {

        return transport.createRequestFactory(request -> {

            //Add the headers (doesn't matter, but Google ones will do)
            GoogleHeaders headers = new GoogleHeaders();
            request.setHeaders(headers);

//Create the parser and add it to the request
            JsonHttpParser parser = new JsonHttpParser(new JacksonFactory());
            request.addParser(parser);
        });
    }
}
