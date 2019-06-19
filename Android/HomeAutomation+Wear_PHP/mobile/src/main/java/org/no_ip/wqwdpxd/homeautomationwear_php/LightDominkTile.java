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
public class LightDominkTile extends TileService {

    private static String ORIGIN = "ANDROID-TILE-LIGHT";

    @Override
    public void onClick() {
        super.onClick();
        //Get tile instance for updating and reading data from it
        Tile tile = getQsTile();
        Log.d("TILE-DEBUG","State:"+tile.getState());
        //Get username from preferences
        SharedPreferences pref;
        String prefs = getResources().getString(R.string.prefs);
        pref = getApplicationContext().getSharedPreferences(prefs, 0);
        String user=pref.getString("username","Unknown");


        //Execute action based on current state and user
        if(user.equals("dominik"))
            ActionSender.sendCommandFb("light_dominik",(tile.getState()==Tile.STATE_ACTIVE)?"0":"255",user,this,ORIGIN);
        else if(user.equals("hrvoje"))
            ActionSender.sendCommandFb("light_hrvoje",(tile.getState()==Tile.STATE_ACTIVE)?"off":"on",user,this,ORIGIN);
        else
            ActionSender.sendCommandFb("light_livingroom_1",(tile.getState()==Tile.STATE_ACTIVE)?"off":"on",user,this,ORIGIN);

        //REDUNDANT updateStateListener();
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

        if(checkConditions(this))
            updateStateListener();

    }

    @Override
    public void onStopListening() {
        super.onStopListening();
    }




    public void updateStateListener(){
        //Get username from preferences
        SharedPreferences pref;
        String prefs = getResources().getString(R.string.prefs);
        pref = getApplicationContext().getSharedPreferences(prefs, 0);
        String user=pref.getString("username","Unknown");
        String selectedLight = "light_livingroom_1";
        if(user.equals("dominik"))
            selectedLight = "light_dominik";
        else if(user.equals("hrvoje"))
            selectedLight = "light_hrvoje";

        FirebaseDatabase myDb;
        DatabaseReference dbRef_nodered;

        myDb = FirebaseDatabase.getInstance();
        dbRef_nodered = myDb.getReference("nodered/"+selectedLight);
        dbRef_nodered.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Log.d("TILE-LISTENER", "Data received: " + dataSnapshot.toString());
                if(checkConditions(getApplicationContext())){
                    try {
                        String currentState = (String) dataSnapshot.getValue().toString();
                        Log.d("TILE-LISTENER", "currentState:" + currentState);
                        if (currentState.equals("on") || currentState.equals("255")) {
                            Log.d("TILE-LISTENER", "SET TO STATE_ACTIVE");
                            Tile tile = getQsTile();
                            tile.setState(Tile.STATE_ACTIVE);
                            tile.setLabel("Turn off");
                            tile.setIcon(Icon.createWithResource(getApplicationContext(), R.drawable.light_on));
                            tile.updateTile();
                        } else {
                            Log.d("TILE-LISTENER", "SET TO STATE_INACTIVE");
                            Tile tile = getQsTile();
                            tile.setState(Tile.STATE_INACTIVE);
                            tile.setLabel("Turn on");
                            tile.setIcon(Icon.createWithResource(getApplicationContext(), R.drawable.light_off));
                            tile.updateTile();
                        }


                    } catch (Exception e) {
                        Log.e("FIREBASE", "Exception, firebase format is wrong: " + e);
                    }
                }


            }


            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("FIREBASE", "Failed to read value.", error.toException());
            }
        });
    }

    public boolean checkConditions(Context context){
        if(!ActionSender.networkConnected(context)) {
            Tile tile = getQsTile();
            tile.setState(Tile.STATE_UNAVAILABLE);
            tile.setLabel("No network");
            tile.setIcon(Icon.createWithResource(context,R.drawable.light_off));
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
            tile.setIcon(Icon.createWithResource(context,R.drawable.light_off));
            tile.updateTile();
            return false;
        }

        return true;

    }





}
