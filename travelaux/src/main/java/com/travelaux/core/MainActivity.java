package com.travelaux.core;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.travelaux.ocr.FeatureExtraction;

/**
 * The main activity class, opens when the program is first started
 */
public class MainActivity extends Activity {

    /**
     * The method that is executed upon creation, currently does not
     * have any additional functionality
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * The method that is executed upon creation of the options menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    /**
     * Start the camera activity
     */
    public void start(View view) {

        //If checkbox is checked, turn on pre-processing display in the Camera Activity class
        CheckBox cbox = findViewById(R.id.devCheckBox);
        if (cbox.isChecked()) FeatureExtraction.setPreprocessing(true);

        //If camera hardware not found, pop up notification
        if (!checkCameraHardware(this))
            Toast.makeText(view.getContext(), "Your device does not seem to have a camera. TravelAux will still be able to retrieve your place details, but only by manual input.", 3).show();

        //Start the camera activity intent
        Intent intent = new Intent(MainActivity.this, CameraActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Check if this device has a camera
     */
    private boolean checkCameraHardware(Context context) {

        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

}