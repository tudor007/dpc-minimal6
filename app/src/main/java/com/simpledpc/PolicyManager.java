package com.simpledpc;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.UserManager;
import android.util.Log;

/**
 * Wraps all DevicePolicyManager calls.
 * Pure AOSP — zero dependency on Google Play Services or GmsCore.
 */
public class PolicyManager {

    private static final String TAG = "SimpleDPC.Policy";

    private final DevicePolicyManager dpm;
    private final ComponentName adminComponent;
    private final Context context;

    private static PolicyManager instance;

    public static PolicyManager get(Context ctx) {
        if (instance == null) {
            instance = new PolicyManager(ctx.getApplicationContext());
        }
        return instance;
    }

    private PolicyManager(Context context) {
        this.context = context;
        this.dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        this.adminComponent = new ComponentName(context, AdminReceiver.class);
    }

    // ─── Status ──────────────────────────────────────────────────────────────

    public boolean isAdminActive() {
        return dpm.isAdminActive(adminComponent);
    }

    public boolean isDeviceOwner() {
        return dpm.isDeviceOwnerApp(context.getPackageName());
    }

    // ─── Default policies applied at provisioning ─────────────────────────

    public void applyDefaultPolicies() {
        Log.i(TAG, "Applying default policies");
        setPasswordMinLength(6);
        setCameraDisabled(false);  // camera on by default
    }

    // ─── Screen lock ─────────────────────────────────────────────────────────

    public void lockNow() {
        if (!isAdminActive()) { Log.w(TAG, "Not admin"); return; }
        Log.i(TAG, "Locking screen");
        dpm.lockNow();
    }

    // ─── Password policy ─────────────────────────────────────────────────────

    public void setPasswordMinLength(int length) {
        if (!isAdminActive()) { Log.w(TAG, "Not admin"); return; }
        Log.i(TAG, "Setting password min length: " + length);
        dpm.setPasswordMinimumLength(adminComponent, length);
    }

    public int getPasswordMinLength() {
        return dpm.getPasswordMinimumLength(adminComponent);
    }

    public void setPasswordQuality(int quality) {
        // quality constants: DevicePolicyManager.PASSWORD_QUALITY_*
        if (!isAdminActive()) return;
        dpm.setPasswordQuality(adminComponent, quality);
    }

    public void setMaxFailedPasswordsBeforeWipe(int max) {
        if (!isAdminActive()) return;
        Log.w(TAG, "Setting max failed passwords to wipe: " + max);
        dpm.setMaximumFailedPasswordsForWipe(adminComponent, max);
    }

    public int getMaxFailedPasswordsForWipe() {
        return dpm.getMaximumFailedPasswordsForWipe(adminComponent);
    }

    // ─── Screen timeout ───────────────────────────────────────────────────────

    public void setMaxTimeToLock(long milliseconds) {
        if (!isAdminActive()) return;
        Log.i(TAG, "Setting max time to lock: " + milliseconds + "ms");
        dpm.setMaximumTimeToLock(adminComponent, milliseconds);
    }

    // ─── Camera ──────────────────────────────────────────────────────────────

    public void setCameraDisabled(boolean disabled) {
        if (!isAdminActive()) return;
        Log.i(TAG, "Camera disabled: " + disabled);
        dpm.setCameraDisabled(adminComponent, disabled);
    }

    public boolean isCameraDisabled() {
        return dpm.getCameraDisabled(adminComponent);
    }

    // ─── Keyguard features ────────────────────────────────────────────────────

    public void disableKeyguardFeatures(int flags) {
        // flags: DevicePolicyManager.KEYGUARD_DISABLE_*
        if (!isAdminActive()) return;
        dpm.setKeyguardDisabledFeatures(adminComponent, flags);
    }

    // ─── User restrictions (Device Owner only) ────────────────────────────────

    public void addUserRestriction(String restriction) {
        if (!isDeviceOwner()) { Log.w(TAG, "Not Device Owner"); return; }
        Log.i(TAG, "Adding user restriction: " + restriction);
        dpm.addUserRestriction(adminComponent, restriction);
    }

    public void clearUserRestriction(String restriction) {
        if (!isDeviceOwner()) return;
        dpm.clearUserRestriction(adminComponent, restriction);
    }

    // Common restriction helpers

    public void setFactoryResetDisabled(boolean disabled) {
        if (disabled) {
            addUserRestriction(UserManager.DISALLOW_FACTORY_RESET);
        } else {
            clearUserRestriction(UserManager.DISALLOW_FACTORY_RESET);
        }
    }

    public void setUsbFileTransferDisabled(boolean disabled) {
        if (disabled) {
            addUserRestriction(UserManager.DISALLOW_USB_FILE_TRANSFER);
        } else {
            clearUserRestriction(UserManager.DISALLOW_USB_FILE_TRANSFER);
        }
    }

    public void setSettingsChangesDisabled(boolean disabled) {
        if (disabled) {
            addUserRestriction(UserManager.DISALLOW_CONFIG_WIFI);
        } else {
            clearUserRestriction(UserManager.DISALLOW_CONFIG_WIFI);
        }
    }

    // ─── Wipe ─────────────────────────────────────────────────────────────────

    /**
     * Factory reset the device. IRREVERSIBLE.
     * Only call after explicit user confirmation.
     */
    public void wipeDevice() {
        if (!isAdminActive()) return;
        Log.w(TAG, "WIPING DEVICE");
        dpm.wipeData(0);
    }
}
