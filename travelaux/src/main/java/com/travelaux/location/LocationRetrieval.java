package com.travelaux.location;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.travelaux.core.AlertDialogManager;
import com.travelaux.core.ConnectionDetector;
import com.travelaux.core.GPSTracker;
import com.travelaux.core.R;

import java.util.ArrayList;
import java.util.HashMap;

public class LocationRetrieval extends Activity {

    // flag for Internet connection status
    Boolean isInternetPresent = false;

    // Connection detector class
    ConnectionDetector connectionDetector;

    // Alert Dialog Manager
    AlertDialogManager alertDialogManager = new AlertDialogManager();

    // Google Places
    GooglePlaces googlePlaces;

    // Places List
    PlacesList nearPlaces;

    // GPS Location
    GPSTracker gpsTracker;

    // Progress dialog
    ProgressDialog progressDialog;

    // Places Listview
    ListView placesListView;

    // ListItems data
    ArrayList<HashMap<String, String>> placesListItems = new ArrayList<HashMap<String, String>>();

    // KEY Strings
    public static String KEY_REFERENCE = "reference"; // id of the place
    public static String KEY_NAME = "name"; // name of the place

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_retrieval);

        connectionDetector = new ConnectionDetector(getApplicationContext());

        // Check if Internet present
        isInternetPresent = connectionDetector.isConnectingToInternet();
        if (!isInternetPresent) {
            // Internet Connection is not present
            alertDialogManager.showAlertDialog(LocationRetrieval.this, "Internet Connection Error",
                    "Please connect to working Internet connection");
            // stop executing code by return
            return;
        }

        // creating GPS Class object
        gpsTracker = new GPSTracker(this);

        // check if GPS location can get
        if (gpsTracker.canGetLocation()) {
            Log.d("Your Location", "latitude:" + gpsTracker.getLatitude() + ", longitude: " + gpsTracker.getLongitude());
        } else {
            // Can't get user's current location
            alertDialogManager.showAlertDialog(LocationRetrieval.this, "GPS Status",
                    "Couldn't get location information. Please enable GPS"
            );
            // stop executing code by return
            return;
        }

        // Getting listview
        placesListView = findViewById(R.id.list);

        // calling background Async task to load Google Places
        // After getting places from Google all the data is shown in listview
        new LoadPlaces().execute();

        /**
         * ListItem click event
         * On selecting a listitem SinglePlaceActivity is launched
         * */
        placesListView.setOnItemClickListener((parent, view, position, id) -> {
            // getting values from selected ListItem
            String reference = ((TextView) view.findViewById(R.id.reference)).getText().toString();

            // Starting new intent
            Intent in = new Intent(getApplicationContext(),
                    SinglePlaceActivity.class);

            // Sending place refrence id to single place activity
            // place refrence id used to get "Place full details"
            in.putExtra(KEY_REFERENCE, reference);
            startActivity(in);
        });
    }

    /**
     * Background Async Task to Load Google places
     */
    class LoadPlaces extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(LocationRetrieval.this);
            progressDialog.setMessage(Html.fromHtml("<b>Search</b><br/>Loading Places..."));
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        /**
         * getting Places JSON
         */
        protected String doInBackground(String... args) {
            // creating Places class object
            googlePlaces = new GooglePlaces();

            try {
                // Separeate your place types by PIPE symbol "|"
                // If you want all types places make it as null
                // Check list of types supported by google
                //
                //String types = ""; // Listing places only cafes, restaurants

                // Radius in meters - increase this value if you don't find any places
                double radius = 10000; // 10000 meters

                //Get the location name as a keyword
                String keyword = getIntent().getStringExtra("name");

                // get nearest places
                nearPlaces = googlePlaces.search(gpsTracker.getLatitude(),
                        gpsTracker.getLongitude(), radius, keyword);


            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * and show the data in UI
         * Always use runOnUiThread(new Runnable()) to update UI from background
         * thread, otherwise you will get error
         **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            progressDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(() -> {
                /**
                 * Updating parsed Places into LISTVIEW
                 * */
                // Get json response status
                String status = nearPlaces.status;

                // Check for all possible status
                switch (status) {
                    case "OK":
                        // Successfully got places details
                        if (nearPlaces.results != null) {
                            // loop through each place
                            for (Place p : nearPlaces.results) {
                                HashMap<String, String> map = new HashMap<String, String>();

                                // Place reference won't display in listview - it will be hidden
                                // Place reference is used to get "place full details"
                                map.put(KEY_REFERENCE, p.reference);

                                // Place name
                                map.put(KEY_NAME, p.name);

                                // adding HashMap to ArrayList
                                placesListItems.add(map);
                            }
                            // list adapter
                            ListAdapter adapter = new SimpleAdapter(LocationRetrieval.this, placesListItems,
                                    R.layout.list_item,
                                    new String[]{KEY_REFERENCE, KEY_NAME}, new int[]{
                                    R.id.reference, R.id.name});
                            // Adding data into listview
                            placesListView.setAdapter(adapter);
                        }
                        break;
                    case "ZERO_RESULTS":
                        // Zero results found
                        alertDialogManager.showAlertDialog(LocationRetrieval.this, "Near Places",
                                "Sorry no places found. Try to change the types of places"
                        );
                        break;
                    case "UNKNOWN_ERROR":
                        alertDialogManager.showAlertDialog(LocationRetrieval.this, "Places Error",
                                "Sorry unknown error occured."
                        );
                        break;
                    case "OVER_QUERY_LIMIT":
                        alertDialogManager.showAlertDialog(LocationRetrieval.this, "Places Error",
                                "Sorry query limit to google places is reached"
                        );
                        break;
                    case "REQUEST_DENIED":
                        alertDialogManager.showAlertDialog(LocationRetrieval.this, "Places Error",
                                "Sorry error occured. Request is denied"
                        );
                        break;
                    case "INVALID_REQUEST":
                        alertDialogManager.showAlertDialog(LocationRetrieval.this, "Places Error",
                                "Sorry error occured. Invalid Request"
                        );
                        break;
                    default:
                        alertDialogManager.showAlertDialog(LocationRetrieval.this, "Places Error",
                                "Sorry error occured."
                        );
                        break;
                }
            });

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

}
