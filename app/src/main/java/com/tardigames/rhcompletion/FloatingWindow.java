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
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
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

        // Set current date in local format
        String currentDate = DateFormat.getDateInstance().format(Calendar.getInstance().getTime());
        TextView tv = (TextView)floatView.findViewById(R.id.textView_Date);
        tv.setText(currentDate);

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
        floatWindowLayoutParam.gravity = Gravity.TOP | Gravity.LEFT;
        floatWindowLayoutParam.x = 0;
        floatWindowLayoutParam.y = 0;

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
        stopSelf();
        // Window is removed from the screen
        windowManager.removeView(floatView);
        isRunning = false;
    }
}
