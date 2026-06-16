package com.simpledpc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Re-applies DPC policies after device reboot.
 * Android persists most policy settings across reboots automatically via
 * DevicePolicyManager, but this receiver is the place to refresh any
 * runtime state or volatile settings.
 */
public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "SimpleDPC.Boot";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;

        Log.i(TAG, "Boot completed — refreshing DPC policies");

        PolicyManager pm = PolicyManager.get(context);
        if (pm.isAdminActive()) {
            // DevicePolicyManager already persists most settings.
            // Add any volatile/runtime policy re-application here if needed.
            Log.i(TAG, "Device admin active. Policies already enforced by OS.");
        } else {
            Log.w(TAG, "Device admin NOT active after boot.");
        }
    }
}
