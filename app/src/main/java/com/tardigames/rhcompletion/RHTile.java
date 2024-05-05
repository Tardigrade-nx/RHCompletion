package com.tardigames.rhcompletion;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

public class RHTile extends TileService {

    @Override
    public void onStartListening() {
        super.onStartListening();
        Tile tile = getQsTile();
        tile.setState(FloatingWindow.isRunning ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.updateTile();
    }

    @Override
    public void onClick() {
        super.onClick();
        Tile tile = getQsTile();
        if (FloatingWindow.isRunning) {
            // Close the floating window
            Intent intent = new Intent(this, FloatingWindow.class);
            stopService(intent);
            // Update tile
            tile.setState(Tile.STATE_INACTIVE);
        } else {
            // Start main activity
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                startActivityAndCollapse(pendingIntent);
            } else {
                startActivity(intent);
            }
            // Update tile
            tile.setState(Tile.STATE_ACTIVE);
        }
        tile.updateTile();
    }
}
