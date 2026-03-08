package com.tardigames.rhcompletion;

import android.app.AlertDialog;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    // Widgets
    private EditText m_editText_nickname;
    private TextView m_textView_width;
    private SeekBar m_seekBar_width;
    private Spinner m_spinner_screen;
    private Button m_button_ok;

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

        // Init
        Options.init(this);
        m_editText_nickname = findViewById(R.id.editText_Nickname);
        m_textView_width = findViewById(R.id.textView_width);
        m_seekBar_width = findViewById(R.id.seekBar_width);
        m_spinner_screen = findViewById(R.id.spinner_screen);
        m_button_ok = findViewById(R.id.button_OK);

        // Configure nickname widget
        m_editText_nickname.setText(Options.getNickname());

        // Configure width widget
        m_seekBar_width.setProgress(Options.getWidth());
        m_textView_width.setText(String.format(getString(R.string.textView_width), m_seekBar_width.getProgress()));
        m_seekBar_width.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (m_seekBar_width.getProgress() < 30)
                    m_seekBar_width.setProgress(30);
                m_textView_width.setText(String.format(getString(R.string.textView_width), m_seekBar_width.getProgress()));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Configure screen spinner — only show on multi-display devices
        DisplayManager displayManager = (DisplayManager) getSystemService(DISPLAY_SERVICE);
        if (displayManager.getDisplays().length > 1) {
            // Order must match Options.SCREEN_PRIMARY=0, Options.SCREEN_SECONDARY=1
            ArrayAdapter<CharSequence> screenAdapter = ArrayAdapter.createFromResource(
                    this, R.array.screen_options, android.R.layout.simple_spinner_item);
            screenAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            m_spinner_screen.setAdapter(screenAdapter);
            m_spinner_screen.setSelection(Options.getPreferredScreen());
        } else {
            // Single-display device: hide the screen selector entirely
            findViewById(R.id.textView_screen_label).setVisibility(View.GONE);
            m_spinner_screen.setVisibility(View.GONE);
            // Force primary so the fallback in FloatingWindow is always correct
            Options.setPreferredScreen(Options.SCREEN_PRIMARY);
        }

        // Configure OK button
        m_button_ok.setOnClickListener(v -> {
            Options.setNickname(String.valueOf(m_editText_nickname.getText()));
            Options.setWidth(m_seekBar_width.getProgress());
            // Only save screen preference if the spinner is visible
            if (m_spinner_screen.getVisibility() == View.VISIBLE) {
                Options.setPreferredScreen(m_spinner_screen.getSelectedItemPosition());
            }
            Options.setAutostart(true);
            checkStart();
        });

        // Stop the service if it's already running
        if (FloatingWindow.isRunning) {
            stopService(new Intent(MainActivity.this, FloatingWindow.class));
        }

        // Request permission if necessary
        if (!Settings.canDrawOverlays(this)) {
            requestAppearOnTopPermission();
        }

        // Autostart if configured
        if (Options.getAutostart()) {
            checkStart();
        }
    }

    // Start the floating window service if possible
    private void checkStart() {
        if (Settings.canDrawOverlays(this)) {
            startService(new Intent(MainActivity.this, FloatingWindow.class));
            finish();
        } else {
            requestAppearOnTopPermission();
        }
    }

    // Request 'Appear on top' permission
    private void requestAppearOnTopPermission() {
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