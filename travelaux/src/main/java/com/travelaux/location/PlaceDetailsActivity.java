package com.travelaux.location;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.travelaux.core.AlertDialogManager;
import com.travelaux.core.CameraActivity;
import com.travelaux.core.ConnectionDetector;
import com.travelaux.core.GPSTracker;
import com.travelaux.core.R;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * The Place Details activity, which deals with displaying all details regarding
 * a particular place
 */
public class PlaceDetailsActivity extends Activity {
    // flag for Internet connection status
    Boolean isInternetPresent = false;

    // Connection detector class
    ConnectionDetector cd;

    // Alert Dialog Manager
    AlertDialogManager alert = new AlertDialogManager();

    // Google Places
    GooglePlaces googlePlaces;

    // FourSquare
    FourSquare fourSquare;

    // Place Details
    PlaceDetails placeDetails;

    // Progress dialog
    ProgressDialog pDialog;

    // Places List for Google Places
    PlacesList nearPlaces;

    // Venue for FourSquare
    Venue venue;

    // GPS Location
    GPSTracker gps;

    // Linear Layout to hold the places
    ListView lv;

    // List of all reviews
    ArrayList<HashMap<String, String>> reviewsList = new ArrayList<>();

    // KEY Strings
    public static String KEY_REFERENCE = "reference"; // id of the place for Google Images
    public static String KEY_FSID = "id"; // id of the place for FourSquare
    public static String KEY_NAME = "name"; // The fourSquare name of the place
    public static String KEY_REVIEWER = "author_name"; //The name of a review's author
    public static String KEY_DATE = "time"; //The date of a particular review
    public static String KEY_REVIEW = "text"; //The text of the review
    public static String KEY_VICINITY = "vicinity"; // Place area name

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_place);

        //Create the connection detection object
        cd = new ConnectionDetector(getApplicationContext());

        // Check if Internet present
        isInternetPresent = cd.isConnectingToInternet();
        if (!isInternetPresent) {
            // Internet Connection is not present
            alert.showAlertDialog(PlaceDetailsActivity.this, "Internet Connection Error",
                    "Please connect to working Internet connection");
            // stop executing code by return
            return;
        }

        // creating GPS Class object
        gps = new GPSTracker(this);

        // check if GPS location can get
        if (gps.canGetLocation()) {
            Log.d("Your Location", "latitude:" + gps.getLatitude() + ", longitude: " + gps.getLongitude());
        } else {
            // Can't get user's current location
            alert.showAlertDialog(PlaceDetailsActivity.this, "GPS Status",
                    "Couldn't get location information. Please enable GPS");
            // stop executing code by return
            return;
        }

        // Getting listview
        lv = findViewById(R.id.list_reviews);

        // Calling a Async Background thread to load the single place
        new LoadSinglePlaceDetails().execute();
    }

    /**
     * Background Async Task to Load Google places
     */
    class LoadSinglePlaceDetails extends AsyncTask<String, String, String> {

        //Aid in cancelling if the place is not found
        boolean isCancelled;

        /**
         * Before starting background thread, show the progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(PlaceDetailsActivity.this);
            pDialog.setMessage(Html.fromHtml("<b>TravelAux</b> is retrieving place details for " + getIntent().getStringExtra("name") + "..."));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting Profile JSON
         */
        protected String doInBackground(String... args) {
            // creating Places class object
            googlePlaces = new GooglePlaces();
            fourSquare = new FourSquare();

            //For Google Places
            try {
                // Radius in meters - increase this value if you don't find any places
                double radius = 10000; // 10000 meters

                //Get the location name as a keyword
                String keyword = getIntent().getStringExtra("name");

                // get nearest places
                nearPlaces = googlePlaces.search(gps.getLatitude(),
                        gps.getLongitude(), radius, keyword);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Get json response status
            String status = nearPlaces.status;

            // Check for all possible statuses
            if (status.equals("OK")) {
                // Successfully got places details
                if (nearPlaces.results != null) {
                    // Retrieve the reference of the first returned place
                    KEY_REFERENCE = nearPlaces.results.get(0).reference;
                }

                //Load the place details
                try {
                    placeDetails = googlePlaces.getPlaceDetails(KEY_REFERENCE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (status.equals("ZERO_RESULTS")) {
                //Zero results found
                //If any of those errors pop up, run the alert dialog
                //from the UI thread
                PlaceDetailsActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        alert.showAlertDialog(PlaceDetailsActivity.this, "Near Places",
                                "Sorry, no places found for '" + getIntent().getStringExtra("name") + "'. Try to change the types of places");
                    }
                });

                //Go back to the camera activity class again
                isCancelled = true;
                onBackPressed();
            } else if (status.equals("UNKNOWN_ERROR")) {
                PlaceDetailsActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        alert.showAlertDialog(PlaceDetailsActivity.this, "Places Error",
                                "Sorry unknown error occured.");
                    }
                });
            } else if (status.equals("OVER_QUERY_LIMIT")) {
                PlaceDetailsActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        alert.showAlertDialog(PlaceDetailsActivity.this, "Places Error",
                                "Sorry query limit to google places is reached");
                    }
                });
            } else if (status.equals("REQUEST_DENIED")) {
                PlaceDetailsActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        alert.showAlertDialog(PlaceDetailsActivity.this, "Places Error",
                                "Sorry error occured. Request is denied");
                    }
                });
            } else if (status.equals("INVALID_REQUEST")) {
                PlaceDetailsActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        alert.showAlertDialog(PlaceDetailsActivity.this, "Places Error",
                                "Sorry error occured. Invalid Request");
                    }
                });
            } else {
                PlaceDetailsActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        alert.showAlertDialog(PlaceDetailsActivity.this, "Places Error",
                                "Sorry error occured.");
                    }
                });
            }

            //For FourSquare
            try {
                //Get the location name as a keyword
                String keyword = getIntent().getStringExtra("name");

                // get nearest places
                venue = fourSquare.search(gps.getLatitude(),
                        gps.getLongitude(), keyword);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (venue != null) {
                // Retrieve the reference of the first returned place
                KEY_FSID = venue.response.venue.id;
                KEY_NAME = venue.response.venue.name;
            }
            return null;

        }

        /**
         * After completing the background task, dismiss the progress dialog
         **/
        protected void onPostExecute(String file_url) {

            //Don't run any of the following if the AsyncTask is cancelled
            if (isCancelled) return;

            // dismiss the dialog after getting all products
            try {
                pDialog.dismiss();
            } catch (Exception ignored) {
            }
            ;

            // updating UI from Background Thread
            runOnUiThread(() -> {
                /**
                 * Updating parsed Places into LISTVIEW
                 * */
                if (placeDetails != null) {
                    String status = placeDetails.status;

                    // Check for all possible status
                    switch (status) {
                        case "OK":
                            if (placeDetails.result != null) {

                                //Retrieve all individual parameters of the place
                                String name = placeDetails.result.name;
                                String description = placeDetails.result.types[0];
                                String address = placeDetails.result.formatted_address;
                                String phone = placeDetails.result.formatted_phone_number;
                                String latitude = Double.toString(placeDetails.result.geometry.location.lat);
                                String longitude = Double.toString(placeDetails.result.geometry.location.lng);

                                //Change underscores in description to spaces
                                description = description.replace("_", " ");

                                Log.d("Place ", name + address + phone + latitude + longitude);

                                // Check for null data from google
                                // Sometimes place details might be missing
                                name = name == null ? "Not present" : name; // if name is null display as "Not present"
                                address = address == null ? "Not present" : address;
                                description = description == null ? "Not present" : description;
                                phone = phone == null ? "Not present" : phone;
                                latitude = latitude == null ? "Not present" : latitude;
                                longitude = longitude == null ? "Not present" : longitude;

                                //Add a header for Google reviews
                                //unfortunately, Html.fromHtml doesnt support text sizes using font,
                                //so the only solution is to use h1 tags
                                HashMap<String, String> map = new HashMap<>();
                                map.put(KEY_REVIEW, "<b><h1>Google Places Reviews</h1></b>");
                                reviewsList.add(map);

                                //Start populating the list of reviews, if they exist
                                if (placeDetails.result.reviews != null) {
                                    for (Review r : placeDetails.result.reviews) {

                                        map = new HashMap<>();

                                        // Place the body of the review into the hashmap
                                        map.put(KEY_REVIEW, r.text);

                                        // Reviewer's name
                                        map.put(KEY_REVIEWER, r.author_name);

                                        //Format the date. Multiply by 1000, as the Date object takes values
                                        //in milliseconds while Google and Foursquare hold dates in seconds
                                        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
                                        Date date = new Date(r.time * 1000L);
                                        // Review date
                                        map.put(KEY_DATE, dateFormat.format(date));

                                        // adding HashMap to ArrayList
                                        reviewsList.add(map);
                                    }
                                    //If not, add a "no reviews" notification
                                } else {
                                    map = new HashMap<>();
                                    map.put(KEY_REVIEW, "(No reviews found)");

                                    // adding HashMap to ArrayList
                                    reviewsList.add(map);
                                }
                                //Add a header for FourSquare reviews
                                map = new HashMap<>();
                                map.put(KEY_REVIEW, "<b><h1>FourSquare Reviews</h1></b>");
                                reviewsList.add(map);

                                //continue populating the list with FourSquare reviews, if they exist
                                if (venue != null && venue.response.venue.tips.groups.length > 0 &&
                                        venue.response.venue.tips.groups[0].items.length > 0) {
                                    for (Tip t : venue.response.venue.tips.groups[0].items) {

                                        map = new HashMap<>();

                                        // Place the body of the review into the hashmap
                                        map.put(KEY_REVIEW, t.text);

                                        // Reviewer's name
                                        map.put(KEY_REVIEWER, t.user.firstName + " " + t.user.lastName);

                                        //Format the date
                                        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
                                        Date date = new Date(t.createdAt * 1000L);

                                        // Review date
                                        map.put(KEY_DATE, dateFormat.format(date));

                                        // adding HashMap to ArrayList
                                        reviewsList.add(map);
                                    }
                                    //If not, add a "no reviews" notification
                                } else {
                                    map = new HashMap<>();
                                    map.put(KEY_REVIEW, "(No reviews found)");

                                    // adding HashMap to ArrayList
                                    reviewsList.add(map);
                                }

                                //Populate the ratings from both sources
                                double rating = (placeDetails.result.rating + (venue.response.venue.rating / 2)) / 2;

                                //Create the list adapter
                                ListAdapter adapter = new SimpleAdapter(PlaceDetailsActivity.this, reviewsList,
                                        R.layout.list_item,
                                        new String[]{KEY_REVIEW, KEY_REVIEWER, KEY_DATE}, new int[]{
                                        R.id.review, R.id.reviewer_name, R.id.review_date}) {

                                    /**
                                     * Override the getView function of the adapter to allow
                                     * HTML code to be parsed within the Review TextView of each
                                     * ListView item. The existing value from HashMap will be taken and
                                     * parsed using Html.fromHtml method.
                                     */
                                    @SuppressWarnings("unchecked")
                                    @Override
                                    public View getView(int position, View convertView, ViewGroup parent) {
                                        View row;

                                        if (null == convertView) {
                                            row = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.list_item, null);
                                        } else {
                                            row = convertView;
                                        }

                                        //The review field will be converted from raw text to HTML
                                        //using Html.fromHtml. The other 2 fields will be copied
                                        //without conversion
                                        TextView tvr = row.findViewById(R.id.review);
                                        TextView tvn = row.findViewById(R.id.reviewer_name);
                                        TextView tvd = row.findViewById(R.id.review_date);
                                        HashMap<String, String> map = (HashMap<String, String>) getItem(position);

                                        tvr.setText(Html.fromHtml(map.get(KEY_REVIEW)));
                                        tvn.setText(map.get(KEY_REVIEWER));
                                        tvd.setText(map.get(KEY_DATE));

                                        return row;
                                    }
                                };

                                //Create the layout inflater
                                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                                //Add a header to the ListView
                                View header = inflater.inflate(R.layout.list_header, null, true);
                                lv.addHeaderView(header);

                                //Add a footer to the ListView
                                View footer = inflater.inflate(R.layout.list_footer, null, true);
                                lv.addFooterView(footer);

                                //Populate the user reviews
                                lv.setAdapter(adapter);

                                // Displaying all the details in the view
                                // single_place.xml
                                TextView lbl_name = findViewById(R.id.name);
                                TextView lbl_desc = findViewById(R.id.description);
                                TextView lbl_address = findViewById(R.id.address);
                                TextView lbl_phone = findViewById(R.id.phone);
                                TextView lbl_location = findViewById(R.id.location);
                                RatingBar rating_bar = findViewById(R.id.ratingBar);

                                //Set the labels to the activity
                                lbl_name.setText(name);
                                lbl_desc.setText(description);
                                lbl_address.setText(address);
                                lbl_phone.setText(Html.fromHtml("<b>Phone:</b> " + phone));
                                lbl_location.setText(Html.fromHtml("<b>Latitude:</b> " + latitude + ", <b>Longitude:</b> " + longitude));
                                rating_bar.setRating((float) rating);
                            }
                            break;
                        case "ZERO_RESULTS":
                            alert.showAlertDialog(PlaceDetailsActivity.this, "Near Places",
                                    "Sorry no place found.");
                            break;
                        case "UNKNOWN_ERROR":
                            alert.showAlertDialog(PlaceDetailsActivity.this, "Places Error",
                                    "Sorry unknown error occured.");
                            break;
                        case "OVER_QUERY_LIMIT":
                            alert.showAlertDialog(PlaceDetailsActivity.this, "Places Error",
                                    "Sorry query limit to google places is reached");
                            break;
                        case "REQUEST_DENIED":
                            alert.showAlertDialog(PlaceDetailsActivity.this, "Places Error",
                                    "Sorry error occured. Request is denied");
                            break;
                        case "INVALID_REQUEST":
                            alert.showAlertDialog(PlaceDetailsActivity.this, "Places Error",
                                    "Sorry error occured. Invalid Request");
                            break;
                        default:
                            alert.showAlertDialog(PlaceDetailsActivity.this, "Places Error",
                                    "Sorry error occured.");
                            break;
                    }
                }

                /**
                 * Handle the "Create your own review" button. upon clicking,
                 * it will redirect to the final activity, ReviewSubmitActivity
                 */
                Button reviewButton = findViewById(R.id.btn_add);
                reviewButton.setOnClickListener(
                        v -> {

                            // Start a new intent
                            Intent intent = new Intent(PlaceDetailsActivity.this, ReviewSubmitActivity.class);

                            //Put the id and the name of the place into the intent
                            intent.putExtra("id", KEY_FSID);
                            intent.putExtra("name", KEY_NAME);

                            //Put the keyword into the intent (for returning purposes)
                            intent.putExtra("keyword", getIntent().getStringExtra("name"));

                            //Start the new activity and finish the current activity
                            //(so that this one will refresh when called again)
                            finish();
                            startActivity(intent);
                        }
                );

                /**
                 * Handle the "Back" button. upon clicking, it will simply
                 * finish the current activity and go to the previous one.
                 */
                Button backButton = findViewById(R.id.btn_back);
                backButton.setOnClickListener(v -> {
                    finish();
                    Intent intent = new Intent(PlaceDetailsActivity.this, CameraActivity.class);
                    startActivity(intent);
                });
            });
        }
    }

    /**
     * Catch the physical back key press on the Android device to not just
     * finish the current activity, but also invoke the previous activity
     * again
     */
    @Override
    public void onBackPressed() {
        finish();
        Intent intent = new Intent(PlaceDetailsActivity.this, CameraActivity.class);
        startActivity(intent);
    }
}
