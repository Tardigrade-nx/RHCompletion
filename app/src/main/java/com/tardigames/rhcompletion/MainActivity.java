package com.tardigames.rhcompletion;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Init options
        Options.init(this);

        // Handle click on button OK
        Button buttonOk = findViewById(R.id.button_OK);
        buttonOk.setOnClickListener(v -> {
            // Save nickname
            EditText editText = (EditText) findViewById(R.id.editText_Nickname);
            Options.setNickname(String.valueOf(editText.getText()));
            // Set autostart
            Options.setAutostart(true);
            // Start
            checkStart();
        });

        // Set nickname
        EditText editText = (EditText) findViewById(R.id.editText_Nickname);
        editText.setText(Options.getNickname());

        // Stop the service if it's already running
        if (FloatingWindow.isRunning) {
            stopService(new Intent(MainActivity.this, FloatingWindow.class));
        }

        // Request permission if necessary
        if (! Settings.canDrawOverlays(this)) {
            requestAppearOnTopPermission();
        }

        // Autostart
        if (Options.getAutostart()) {
            checkStart();
        }
    }

    // Start the floating window service if possible
    private void checkStart() {
        if (Settings.canDrawOverlays(this)) {
            // Start service and stop main activity
            startService(new Intent(MainActivity.this, FloatingWindow.class));
            finish();
        } else {
            requestAppearOnTopPermission();
        }
    }

    // Request 'Appear on top' permission
    private void requestAppearOnTopPermission() {
        // Display 'Permission needed' message
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission Needed");
        builder.setMessage("Please enable 'Appear on top' from System Settings.");
        builder.setPositiveButton("Open Settings", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, RESULT_OK);
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}