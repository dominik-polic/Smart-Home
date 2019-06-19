package org.no_ip.wqwdpxd.homeautomationwear_php;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.support.annotation.RequiresApi;
import android.text.format.DateFormat;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

@RequiresApi(api = Build.VERSION_CODES.N)
public class FilipTile extends TileService {

    private static String ORIGIN = "ANDROID-TILE-FILIP";

    @Override
    public void onClick() {
        super.onClick();

        Context context = this;
        String node = "garaza_filip";
        String action = "toggle";

        String prefs = context.getResources().getString(R.string.prefs);
        final SharedPreferences pref = context.getSharedPreferences(prefs, 0); // 0 - for private mode
        String user=pref.getString("username", "Unknown");

        if(user.equals("filip")) {
            if (!networkConnected(context))
                ActionSender.displayToast("No network", context);

            FirebaseDatabase myDb;
            DatabaseReference dbRef_remote;
            DatabaseReference dbRef_logs;

            myDb = FirebaseDatabase.getInstance();
            dbRef_remote = myDb.getReference("remote");
            dbRef_logs = myDb.getReference("logs");


            //Send execution command
            dbRef_remote.child(node).setValue(action);


            //Add log
            LogEntry logEntry = new LogEntry(action, node, user, ORIGIN);

            dbRef_logs.child(DateFormat.format("yyyy-MM-dd", new java.util.Date()).toString())
                    .child(DateFormat.format("HH:mm:ss", new java.util.Date()).toString()).setValue(logEntry);
        }
    }


    @Override
    public void onTileAdded() {
        super.onTileAdded();
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();

        if(checkConditions(this)) {
            Tile tile = getQsTile();
            tile.setState(Tile.STATE_INACTIVE);
            tile.setLabel("Toggle");
            tile.setIcon(Icon.createWithResource(getApplicationContext(), R.drawable.gate_close));
            tile.updateTile();
        }

    }

    @Override
    public void onStopListening() {
        super.onStopListening();
    }


    public boolean checkConditions(Context context){
        if(!networkConnected(context)) {
            Tile tile = getQsTile();
            tile.setState(Tile.STATE_UNAVAILABLE);
            tile.setLabel("No network");
            tile.setIcon(Icon.createWithResource(context,R.drawable.gate_close));
            tile.updateTile();
            return false;
        }

        SharedPreferences pref;
        String prefs = getResources().getString(R.string.prefs);
        pref = getApplicationContext().getSharedPreferences(prefs, 0);
        boolean loggedIn = pref.getBoolean("login", false);
        if(!loggedIn){
            Tile tile = getQsTile();
            tile.setState(Tile.STATE_UNAVAILABLE);
            tile.setLabel("Not logged in");
            tile.setIcon(Icon.createWithResource(context,R.drawable.gate_close));
            tile.updateTile();
            return false;
        }
        String user=pref.getString("username","Unknown");
        if(!user.equals("filip")){
            Tile tile = getQsTile();
            tile.setState(Tile.STATE_UNAVAILABLE);
            tile.setLabel("Wrong user");
            tile.setIcon(Icon.createWithResource(context,R.drawable.gate_close));
            tile.updateTile();
            return false;
        }

        return true;

    }



    public boolean networkConnected(Context context) {

        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = null;
        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
        }


        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }



}
