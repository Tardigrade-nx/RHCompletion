package com.tardigames.rhcompletion;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        // Stop the service if it's already running
        if (FloatingWindow.isRunning) {
            stopService(new Intent(MainActivity.this, FloatingWindow.class));
        }

        // Request 'display on top' permission if necessary
        if (Settings.canDrawOverlays(this)) {
            // Start service and stop main activity
            startService(new Intent(MainActivity.this, FloatingWindow.class));
            finish();
        } else {
            requestDisplayOnTopPermission();
        }
    }

    // Request 'Display on top' permission
    private void requestDisplayOnTopPermission() {
        // Display 'Permission needed' message
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission Needed");
        builder.setMessage("Please enable 'Display on top' from System Settings.");
        builder.setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, RESULT_OK);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}