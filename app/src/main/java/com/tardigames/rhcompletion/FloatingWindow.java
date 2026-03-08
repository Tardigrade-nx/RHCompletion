package com.tardigames.rhcompletion;

import android.app.ActivityOptions;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Display;
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

    public static final String EXTRA_DISPLAY_ID = "display_id";

    private WindowManager windowManager;
    private ViewGroup floatView;
    private int mDisplayId = Display.DEFAULT_DISPLAY;
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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setupWindow();
        return START_NOT_STICKY;
    }

    private void setupWindow() {
        // Resolve the target display from the saved preference
        DisplayManager displayManager = (DisplayManager) getSystemService(DISPLAY_SERVICE);
        Display display;
        if (Options.getPreferredScreen() == Options.SCREEN_SECONDARY) {
            Display[] displays = displayManager.getDisplays();
            display = (displays.length > 1) ? displays[1] : displayManager.getDisplay(Display.DEFAULT_DISPLAY);
        } else {
            // SCREEN_PRIMARY
            display = displayManager.getDisplay(Display.DEFAULT_DISPLAY);
        }
        mDisplayId = display.getDisplayId();

        // Create a context tied to the correct display so WindowManager
        // and LayoutInflater both target that screen
        Context displayContext = createDisplayContext(display);
        windowManager = (WindowManager) displayContext.getSystemService(WINDOW_SERVICE);

        // Inflate Layout using the display context
        LayoutInflater inflater = LayoutInflater.from(displayContext);
        floatView = (ViewGroup) inflater.inflate(R.layout.floating_layout, null);

        // Set Nickname
        TextView tvNickname = (TextView) floatView.findViewById(R.id.textView_Nickname);
        tvNickname.setText(Options.getNickname());

        // Set current date in local format
        String currentDate = DateFormat.getDateInstance().format(Calendar.getInstance().getTime());
        TextView tvDate = (TextView) floatView.findViewById(R.id.textView_Date);
        tvDate.setText(currentDate);

        // Callback for Close button
        ImageButton closeButton = (ImageButton) floatView.findViewById(R.id.imageButton_Close);
        closeButton.setOnClickListener(v -> close());

        // Callback for Edit button — open settings on the same display as the overlay
        ImageButton editButton = (ImageButton) floatView.findViewById(R.id.imageButton_edit);
        editButton.setOnClickListener(v -> {
            Options.setAutostart(false);
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ActivityOptions options = ActivityOptions.makeBasic().setLaunchDisplayId(mDisplayId);
            startActivity(intent, options.toBundle());
            close();
        });

        // Layout parameters
        int LAYOUT_TYPE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_TOAST;
        }

        // Compute floating window width using the target display's metrics
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        final int floatingWindowWidth = (int) (metrics.widthPixels * Options.getWidth() / 100.0f);
        WindowManager.LayoutParams floatWindowLayoutParam = new WindowManager.LayoutParams(
                floatingWindowWidth,
                (int) (floatingWindowWidth * 0.3f),
                LAYOUT_TYPE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        floatWindowLayoutParam.gravity = Gravity.TOP | Gravity.START;
        floatWindowLayoutParam.x = 0;
        floatWindowLayoutParam.y = 0;

        // Add the view to the Window Manager
        windowManager.addView(floatView, floatWindowLayoutParam);

        // Move the floating window on drag
        floatView.setOnTouchListener(new View.OnTouchListener() {
            final WindowManager.LayoutParams floatWindowLayoutUpdateParam = floatWindowLayoutParam;
            double x, y, px, py;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x = floatWindowLayoutUpdateParam.x;
                        y = floatWindowLayoutUpdateParam.y;
                        px = event.getRawX();
                        py = event.getRawY();
                        break;
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        close();
    }

    public void close() {
        stopSelf();
        if (floatView != null && floatView.isAttachedToWindow()) {
            windowManager.removeView(floatView);
        }
        isRunning = false;
    }
}
