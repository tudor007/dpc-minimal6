package com.simpledpc;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * The mandatory DeviceAdminReceiver for a DPC.
 * When this app is set as Device Owner (via adb dpm), this receiver
 * gains full device management authority — no GMS required.
 */
public class AdminReceiver extends DeviceAdminReceiver {

    private static final String TAG = "SimpleDPC.Admin";

    @Override
    public void onEnabled(Context context, Intent intent) {
        Log.i(TAG, "Device admin enabled");
        Toast.makeText(context, "SimpleDPC: Admin enabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        Log.i(TAG, "Device admin disabled");
        Toast.makeText(context, "SimpleDPC: Admin disabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProfileProvisioningComplete(Context context, Intent intent) {
        // Called when Device Owner provisioning finishes (e.g. via NFC, QR, or adb)
        Log.i(TAG, "Provisioning complete — Device Owner is active");
        PolicyManager.get(context).applyDefaultPolicies();
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        return "Removing SimpleDPC will clear all enforced device policies.";
    }

    @Override
    public void onPasswordFailed(Context context, Intent intent) {
        Log.w(TAG, "Password attempt failed");
    }

    @Override
    public void onPasswordSucceeded(Context context, Intent intent) {
        Log.i(TAG, "Password succeeded");
    }
}
