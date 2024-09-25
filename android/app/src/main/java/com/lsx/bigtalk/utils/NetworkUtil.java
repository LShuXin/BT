package com.lsx.bigtalk.utils;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

import java.util.Objects;


public class NetworkUtil {
    /** network - unavailable */
    public static final int NETWORK_NONE = 0;
    /** network - wifi */
    public static final int NETWORK_WIFI = 1;
    /** network - others */
    public static final int NETWORK_OTHERS = 2;

    public static int getNetWorkType(Application application) {
        if (!isNetWorkAvailable(application)) {
            return NetworkUtil.NETWORK_NONE;
        }

        ConnectivityManager connectivityManager = (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network nw = connectivityManager.getActiveNetwork();
        NetworkCapabilities actNw = connectivityManager.getNetworkCapabilities(nw);

        if (Objects.requireNonNull(actNw).hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            return NetworkUtil.NETWORK_WIFI;
        } else {
            return NetworkUtil.NETWORK_OTHERS;
        }
    }

    public static boolean isNetWorkAvailable(Application application) {
        ConnectivityManager connectivityManager = (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network nw = connectivityManager.getActiveNetwork();
        if (nw == null) {
            return false;
        };
        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(nw);
        return null != networkCapabilities
            &&
            (
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                ||
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                ||
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                ||
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH)
            );
    }

}
