package com.travelaux.location;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.travelaux.core.R;

/**
 * The class that handles creating a new review
 * and posting it to the FourSquare API server
 */
public class ReviewSubmitActivity extends Activity {

    //These values are used to register TravelAux's OAuth token for FourSquare
    public static final String CALLBACK_URL = "http://surrey.ac.uk";
    public static final String CLIENT_ID = "3CPZQPHED0DPMI1AOF451ZDU4NMCSZBWYCFFYIKCYBMPTRDR";

    //The authentication token variable
    String accessToken;

    //The id of the place
    String id;

    // Progress dialog
    ProgressDialog pDialog, nDialog;

    //The FourSquare object
    FourSquare fourSquare = new FourSquare();

    /**
     * The function that handles the creation of the activity
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_submit);

        /**
         * Handle the "Back" button. upon clicking, it will simply
         * finish the current activity and go to the previous one.
         * This is assigned at the start of onCreate method for simplicity
         * sake (the proceeding code will deal with the login webView and
         * label assignment)
         */
        Button backButton = findViewById(R.id.btn_back_sub);
        backButton.setOnClickListener(v -> {
            finish();
            Intent intent = new Intent(ReviewSubmitActivity.this, PlaceDetailsActivity.class);
            intent.putExtra("name", getIntent().getStringExtra("keyword"));
            startActivity(intent);
        });

        /**
         * Handle the "Submit Review" button. upon clicking,
         * it will invoke the FourSquare class, which will construct
         * the query string containing the text of the message to be posted.
         */
        Button reviewButton = findViewById(R.id.btn_add_sub);
        reviewButton.setOnClickListener(
                v -> {

                    //Retrieve the text of the review from the activity
                    EditText reviewText = findViewById(R.id.text_review_sub);

                    //if the text is not null, invoke the fourSquare submit function.
                    if (reviewText.getText().toString() != "") {

                        boolean submit = false;
                        try {
                            submit = fourSquare.submit(id, accessToken, reviewText.getText().toString());
                        } catch (InstantiationException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }

                        //If submission is successful, finish the current activity and start the
                        //previous one anew
                        if (submit) {
                            finish();
                            Intent intent = new Intent(ReviewSubmitActivity.this, PlaceDetailsActivity.class);
                            intent.putExtra("name", getIntent().getStringExtra("keyword"));
                            startActivity(intent);
                        }
                    }
                }
        );

        String url = "https://foursquare.com/oauth2/authenticate" +
                "?client_id=" + CLIENT_ID + "&response_type=token" +
                "&redirect_uri=" + CALLBACK_URL;

        // If authentication works, we'll get redirected to a url with a pattern like:
        // http://YOUR_REGISTERED_REDIRECT_URI/#access_token=ACCESS_TOKEN
        // We can override onPageStarted() in the web client and grab the token out.

        //Set the id variable
        id = getIntent().getStringExtra("id");

        //Set the name of the place in the activity
        TextView nameText = findViewById(R.id.name_sub);
        nameText.setText(getIntent().getStringExtra("name"));

        //Start the webview waiting process dialog
        pDialog = new ProgressDialog(ReviewSubmitActivity.this);
        pDialog.setMessage(Html.fromHtml("Please wait while you are connected to the FourSquare authentication server..."));
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();

        WebView webview = findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.requestFocus(View.FOCUS_DOWN);
        webview.setWebViewClient(new WebViewClient() {
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                String fragment = "#access_token=";
                int start = url.indexOf(fragment);
                if (start > -1) {

                    // You can use the accessToken for api calls now.
                    accessToken = url.substring(start + fragment.length(), url.length());

                    //Remove the waiting dialog (if it existed)
                    pDialog.dismiss();

                    Log.v("Log", "OAuth complete, token: [" + accessToken + "].");
                    //Toast.makeText(ReviewSubmitActivity.this, "Token: " + accessToken, Toast.LENGTH_SHORT).show();

                    //Remove the WebView and make way for the review submit layout
                    WebView webview = findViewById(R.id.webview);
                    ((RelativeLayout) webview.getParent()).removeView(webview);

                    //Start the name retrieval dialog
                    nDialog = new ProgressDialog(ReviewSubmitActivity.this);
                    nDialog.setMessage(Html.fromHtml("Your authentication is being validated. Please wait..."));
                    nDialog.setIndeterminate(false);
                    nDialog.setCancelable(false);
                    nDialog.show();

                    //Fill in the username variable into the logged-in TextView
                    String name = fourSquare.getName(accessToken);
                    if (name == null) name = "(unknown)";

                    TextView logged_in_as = findViewById(R.id.logged_in_as_sub);
                    logged_in_as.setText(Html.fromHtml("Logged in as: <b>" + name + "</b>"));

                    //Delete the waiting dialog
                    nDialog.dismiss();
                }
            }

            public void onPageFinished(WebView view, String url) {

                //Remove the waiting dialog
                pDialog.dismiss();
            }
        });
        webview.loadUrl(url);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_review_submit, menu);
        return true;
    }

    /**
     * Catch the physical back key press on the Android device to not just
     * finish the current activity, but also invoke the previous activity
     * again
     */
    @Override
    public void onBackPressed() {
        finish();
        Intent intent = new Intent(ReviewSubmitActivity.this, PlaceDetailsActivity.class);
        intent.putExtra("name", getIntent().getStringExtra("keyword"));
        startActivity(intent);
    }

}
