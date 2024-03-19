package com.tardigames.rhcompletion;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.text.DateFormat;
import java.util.Calendar;

public class FloatingWindow extends Service {

    private WindowManager windowManager;
    private ViewGroup floatView;
    static public boolean isRunning = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;

        // Screen height and width
        DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        // Window Manager
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // Inflate Layout
        LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        floatView = (ViewGroup) inflater.inflate(R.layout.floating_layout, null);

        // Set Nickname
        TextView tvNickname = (TextView)floatView.findViewById(R.id.textView_Nickname);
        tvNickname.setText(Options.getNickname());

        // Set current date in local format
        String currentDate = DateFormat.getDateInstance().format(Calendar.getInstance().getTime());
        TextView tvDate = (TextView)floatView.findViewById(R.id.textView_Date);
        tvDate.setText(currentDate);

        // Callback for Close button
        ImageButton closeButton = (ImageButton)floatView.findViewById(R.id.imageButton_Close);
        closeButton.setOnClickListener(v -> close());

        // Callback for Edit button
        ImageButton editButton = (ImageButton)floatView.findViewById(R.id.imageButton_edit);
        editButton.setOnClickListener(v -> {
            // Disable autostart
            Options.setAutostart(false);
            // Start main activity
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            // Quit service
            close();
        });

        // Layout parameters
        int LAYOUT_TYPE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_TOAST;
        }
        WindowManager.LayoutParams floatWindowLayoutParam = new WindowManager.LayoutParams(
                (int) (width * 0.8f),
                (int) (width * 0.8f * 0.5f),
                LAYOUT_TYPE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        floatWindowLayoutParam.gravity = Gravity.TOP | Gravity.START;
        floatWindowLayoutParam.x = 4;
        floatWindowLayoutParam.y = 4;

        // Add the view to the Windows Manager
        windowManager.addView(floatView, floatWindowLayoutParam);

        // Move the floating windows
        floatView.setOnTouchListener(new View.OnTouchListener() {
            final WindowManager.LayoutParams floatWindowLayoutUpdateParam = floatWindowLayoutParam;
            double x, y, px, py;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    // Memorize x,y of touch
                    case MotionEvent.ACTION_DOWN:
                        x = floatWindowLayoutUpdateParam.x;
                        y = floatWindowLayoutUpdateParam.y;
                        px = event.getRawX();
                        py = event.getRawY();
                        break;
                    // Update x,y
                    case MotionEvent.ACTION_MOVE:
                        floatWindowLayoutUpdateParam.x = (int) ((x + event.getRawX()) - px);
                        floatWindowLayoutUpdateParam.y = (int) ((y + event.getRawY()) - py);
                        windowManager.updateViewLayout(floatView, floatWindowLayoutUpdateParam);
                        break;
                }
                return false;
            }
        });
    }

    // Called when stopService() is called in MainActivity
    @Override
    public void onDestroy() {
        super.onDestroy();
        close();
    }

    // Close floating window
    public void close() {
        // Stop service
        stopSelf();
        // Window is removed from the screen
        windowManager.removeView(floatView);
        isRunning = false;
    }
}
