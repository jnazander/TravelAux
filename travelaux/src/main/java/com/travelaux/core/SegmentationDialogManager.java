package com.travelaux.core;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.widget.EditText;

import com.travelaux.ocr.FeatureExtraction;

/**
 * The dialog manager that pops up when the threshold parameter is chosen to be changed
 */
public class SegmentationDialogManager {

    /**
     * Show the prompt dialog
     */
    public static void showDialog(Context context) {

        AlertDialog.Builder alert = new AlertDialog.Builder(context);

        //The prompt text
        alert.setTitle("Threshold amount");
        alert.setMessage("Please input the threshold amount to be used during logo segmentation");

        // Set an EditText view to get user input
        final EditText input = new EditText(context);

        //Set the input to only accept integers
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        String value = Double.toString(FeatureExtraction.getThreshold());
        input.setText(value.toCharArray(), 0, value.length());
        alert.setView(input);

        //Upon clicking OK, set the threshold value into the
        //feature extraction class
        alert.setPositiveButton("Ok", (dialog, whichButton) -> {

            double value1 = Double.valueOf(input.getText().toString());
            FeatureExtraction.setThreshold(value1);
        });

        //Upon clicking cancel, remove the prompt
        alert.setNegativeButton("Cancel", (dialog, whichButton) -> {
            // Canceled.
        });

        //Show the prompt
        alert.show();
    }
}
