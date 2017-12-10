package com.travelaux.core;

import android.app.AlertDialog;
import android.content.Context;

public class AlertDialogManager {

    /**
     * Function to display a simple Alert Dialog
     *
     * @param context - application context
     * @param title   - alert dialog title
     * @param message - alert message
     */
    @SuppressWarnings("deprecation")
    public void showAlertDialog(Context context, String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        // Setting the dialog title
        alertDialog.setTitle(title);

        // Setting the dialog message
        alertDialog.setMessage(message);

        // Setting OK Button
        alertDialog.setButton("OK", (dialog, which) -> {
        });

        // Showing Alert Message
        alertDialog.show();
    }
}