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
public class Gate2Tile extends TileService {

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
        sendCommandFb("gate_2",(tile.getState()==Tile.STATE_ACTIVE)?"open":"close",user);

        //REDUNDANT updateStateListener();
    }

    public void sendCommandFb(String node, String action,String user){
        Log.d("SEND-COMMAND","Sending to node: "+node+", action: "+action);

        try {
            action = action.toString();
        }catch (NullPointerException e){
            Log.e("FIREBASE","Error converting \"action\"Object to String");
        }

        FirebaseDatabase myDb;
        DatabaseReference dbRef_remote;
        DatabaseReference dbRef_logs;

        myDb = FirebaseDatabase.getInstance();
        dbRef_remote = myDb.getReference("remote");
        dbRef_logs = myDb.getReference("logs");


        //Send execution command
        dbRef_remote.child(node).setValue(action);



        //Add log
        LogEntry logEntry = new LogEntry(action, node, user,"ANDROID_TILE");

        dbRef_logs.child(DateFormat.format("yyyy-MM-dd", new java.util.Date()).toString())
                .child(DateFormat.format("HH:mm:ss", new java.util.Date()).toString()).setValue(logEntry);

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


        updateStateListener();


        Tile tile = getQsTile();

        if(!networkConnected(this)) {
            tile.setState(Tile.STATE_UNAVAILABLE);
            tile.setLabel("No network");
            tile.setIcon(Icon.createWithResource(this,R.drawable.light_off));
            tile.updateTile();
        }

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


        FirebaseDatabase myDb;
        DatabaseReference dbRef_nodered;

        myDb = FirebaseDatabase.getInstance();
        dbRef_nodered = myDb.getReference("nodered/gate_2");
        dbRef_nodered.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Log.d("TILE-LISTENER","Data received: "+dataSnapshot.toString());

                try {
                    String currentState = (String) dataSnapshot.getValue().toString();
                    Log.d("TILE-LISTENER","currentState:"+currentState);
                    if(currentState.equals("close")){
                        Log.d("TILE-LISTENER","SET TO STATE_ACTIVE");
                        Tile tile = getQsTile();
                        tile.setState(Tile.STATE_ACTIVE);
                        tile.setLabel("Open gate");
                        tile.setIcon(Icon.createWithResource(getApplicationContext(),R.drawable.gate_open));
                        tile.updateTile();
                    }else{
                        Log.d("TILE-LISTENER","SET TO STATE_INACTIVE");
                        Tile tile = getQsTile();
                        tile.setState(Tile.STATE_INACTIVE);
                        tile.setLabel("Close gate");
                        tile.setIcon(Icon.createWithResource(getApplicationContext(),R.drawable.gate_close));
                        tile.updateTile();
                    }



                } catch (Exception e) {
                    Log.e("FIREBASE", "Exception, firebase format is wrong: " + e);
                }


            }


            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("FIREBASE", "Failed to read value.", error.toException());
            }
        });
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
