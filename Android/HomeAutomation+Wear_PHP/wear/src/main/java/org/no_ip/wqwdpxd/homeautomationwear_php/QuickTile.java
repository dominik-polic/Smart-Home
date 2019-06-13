package org.no_ip.wqwdpxd.homeautomationwear_php;


import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.clockwork.tiles.TileData;
import com.google.android.clockwork.tiles.TileProviderService;

public class QuickTile extends TileProviderService {

    private int id = 0;
    private static final String clickBtnGateOpen = "W_B_GOPEN";
    private static final String clickBtnGateClose = "W_B_GCLOSE";
    private static final String clickBtnLightOn = "W_B_LON";
    private static final String clickBtnLightOff = "W_B_LOFF";


    @Override
    public void onTileUpdate(int tileId) {
        if (tileId>-2) {
            Log.d("WEAR_TILE","Initialised with ID: " + tileId);
            id = tileId;

            RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.basic_tile);

            views.setOnClickPendingIntent(R.id.tileBtnGateClose, getPendingSelfIntent(this, clickBtnGateClose));
            views.setOnClickPendingIntent(R.id.tileBtnGateOpen, getPendingSelfIntent(this, clickBtnGateOpen));
            views.setOnClickPendingIntent(R.id.tileBtnLightOn, getPendingSelfIntent(this, clickBtnLightOn));
            views.setOnClickPendingIntent(R.id.tileBtnLightOff, getPendingSelfIntent(this, clickBtnLightOff));


            sendData(id, new TileData.Builder().setRemoteViews(views).build());
        }

    }



    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Log.d("WEAR_TILE","initialising intent....");
        Intent intent = new Intent(context, ActionReceiver.class);
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }




}
