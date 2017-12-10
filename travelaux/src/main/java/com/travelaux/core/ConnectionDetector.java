package com.travelaux.core;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * A 3rd party class for detecting whether the current internet connection
 * exists or not. This class is not created by the author of this report.
 * Courtesy of Ravi Tamada from the Android Hive
 * (http://www.androidhive.info/2012/07/android-detect-internet-connection-status/)
 */
public class ConnectionDetector {

    private Context _context;

    public ConnectionDetector(Context context) {
        this._context = context;
    }

    /**
     * Checking for all possible internet providers
     **/
    public boolean isConnectingToInternet() {
        ConnectivityManager connectivity = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (NetworkInfo anInfo : info)
                    if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }

        }
        return false;
    }
}