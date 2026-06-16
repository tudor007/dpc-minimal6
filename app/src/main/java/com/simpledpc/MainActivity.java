package com.simpledpc;

import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private PolicyManager pm;

    // UI
    private TextView tvStatus;
    private Button btnLock;
    private Button btnWipe;
    private CheckBox cbCamera;
    private CheckBox cbFactoryReset;
    private CheckBox cbUsb;
    private SeekBar sbPasswordLength;
    private TextView tvPasswordLength;
    private SeekBar sbTimeout;
    private TextView tvTimeout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pm = PolicyManager.get(this);

        bindViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStatus();
        refreshControls();
    }

    // ─── View binding ─────────────────────────────────────────────────────────

    private void bindViews() {
        tvStatus         = findViewById(R.id.tvStatus);
        btnLock          = findViewById(R.id.btnLock);
        btnWipe          = findViewById(R.id.btnWipe);
        cbCamera         = findViewById(R.id.cbCamera);
        cbFactoryReset   = findViewById(R.id.cbFactoryReset);
        cbUsb            = findViewById(R.id.cbUsb);
        sbPasswordLength = findViewById(R.id.sbPasswordLength);
        tvPasswordLength = findViewById(R.id.tvPasswordLength);
        sbTimeout        = findViewById(R.id.sbTimeout);
        tvTimeout        = findViewById(R.id.tvTimeout);
    }

    // ─── Listeners ────────────────────────────────────────────────────────────

    private void setupListeners() {
        btnLock.setOnClickListener(v -> {
            pm.lockNow();
            toast("Screen locked");
        });

        btnWipe.setOnClickListener(v -> confirmWipe());

        cbCamera.setOnCheckedChangeListener((b, checked) -> {
            pm.setCameraDisabled(checked);
            toast("Camera " + (checked ? "disabled" : "enabled"));
        });

        cbFactoryReset.setOnCheckedChangeListener((b, checked) -> {
            pm.setFactoryResetDisabled(checked);
            toast("Factory reset " + (checked ? "blocked" : "allowed"));
        });

        cbUsb.setOnCheckedChangeListener((b, checked) -> {
            pm.setUsbFileTransferDisabled(checked);
            toast("USB file transfer " + (checked ? "blocked" : "allowed"));
        });

        sbPasswordLength.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int progress, boolean fromUser) {
                int len = progress + 4; // minimum displayed: 4
                tvPasswordLength.setText("Min password length: " + len);
                if (fromUser) pm.setPasswordMinLength(len);
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });

        sbTimeout.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private final long[] OPTIONS_MS = {
                30_000, 60_000, 120_000, 300_000, 600_000, 0 /* never */
            };
            private final String[] LABELS = {
                "30 sec", "1 min", "2 min", "5 min", "10 min", "Never"
            };
            @Override public void onProgressChanged(SeekBar s, int progress, boolean fromUser) {
                int idx = Math.min(progress, OPTIONS_MS.length - 1);
                tvTimeout.setText("Screen timeout: " + LABELS[idx]);
                if (fromUser) pm.setMaxTimeToLock(OPTIONS_MS[idx]);
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });
    }

    // ─── Status refresh ───────────────────────────────────────────────────────

    private void refreshStatus() {
        boolean isAdmin = pm.isAdminActive();
        boolean isDO    = pm.isDeviceOwner();

        String status = "Admin: " + (isAdmin ? "✓" : "✗")
                + "   Device Owner: " + (isDO ? "✓" : "✗");
        tvStatus.setText(status);

        // Disable controls if not admin
        btnLock.setEnabled(isAdmin);
        btnWipe.setEnabled(isAdmin);
        cbCamera.setEnabled(isAdmin);
        cbFactoryReset.setEnabled(isDO);
        cbUsb.setEnabled(isDO);
        sbPasswordLength.setEnabled(isAdmin);
        sbTimeout.setEnabled(isAdmin);

        if (!isAdmin) {
            tvStatus.append("\n\nTo activate, run:\nadb shell dpm set-device-owner com.simpledpc/.AdminReceiver");
        }
    }

    private void refreshControls() {
        // Suppress listener firing while setting programmatically
        cbCamera.setOnCheckedChangeListener(null);
        cbCamera.setChecked(pm.isCameraDisabled());
        cbCamera.setOnCheckedChangeListener((b, checked) -> {
            pm.setCameraDisabled(checked);
            toast("Camera " + (checked ? "disabled" : "enabled"));
        });

        int pwLen = Math.max(pm.getPasswordMinLength() - 4, 0);
        sbPasswordLength.setProgress(pwLen);
        tvPasswordLength.setText("Min password length: " + (pwLen + 4));
    }

    // ─── Wipe confirmation ────────────────────────────────────────────────────

    private void confirmWipe() {
        new AlertDialog.Builder(this)
                .setTitle("⚠ Factory Reset Device")
                .setMessage("This will erase ALL data on the device permanently. Are you absolutely sure?")
                .setPositiveButton("WIPE", (d, w) -> {
                    // Second confirmation
                    new AlertDialog.Builder(this)
                            .setTitle("Final Confirmation")
                            .setMessage("There is no undo. Proceed?")
                            .setPositiveButton("YES, WIPE NOW", (d2, w2) -> pm.wipeDevice())
                            .setNegativeButton("Cancel", null)
                            .show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
