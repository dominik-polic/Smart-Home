package org.no_ip.wqwdpxd.homeautomationwear_php;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ActionReceiver extends BroadcastReceiver {

    private static final String clickBtnGateOpen = "W_B_GOPEN";
    private static final String clickBtnGateClose = "W_B_GCLOSE";
    private static final String clickBtnLightOn = "W_B_LON";
    private static final String clickBtnLightOff = "W_B_LOFF";
    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        Log.d("WEAR_TILE","Intent received: " + intent.getAction());

        if(intent.getAction().equals(clickBtnGateClose)){
            sendCommandFb("gate_2","close");
        } else if(intent.getAction().equals(clickBtnGateOpen)){
            sendCommandFb("gate_2","open");
        } else if(intent.getAction().equals(clickBtnLightOff)){
            sendCommandFb("light_dominik","0");
            Log.d("WEAR_TILE","light off");
        } else if(intent.getAction().equals(clickBtnLightOn)){
            Log.d("WEAR_TILE","light on");
            sendCommandFb("light_dominik","255");
        }

    }

    private void sendCommandFb(String node, Object action){
        String user = "wearos";

        try {
            action = action.toString();
        }catch (NullPointerException e){
            Log.e("FIREBASE","Error converting \"action\"Object to String");
        }

        DatabaseReference dbRef_nodered;
        DatabaseReference dbRef_remote;
        DatabaseReference dbRef_logs;
        FirebaseDatabase myDb;
        myDb = FirebaseDatabase.getInstance();
        dbRef_nodered = myDb.getReference("nodered");
        dbRef_remote = myDb.getReference("remote");
        dbRef_logs = myDb.getReference("logs");


        //Send execution command
        dbRef_remote.child(node).setValue(action);

        //Add log
        LogEntry logEntry = new LogEntry(action, node, user);
        dbRef_logs.child(DateFormat.format("yyyy-MM-dd", new java.util.Date()).toString())
                .child(DateFormat.format("HH:mm:ss", new java.util.Date()).toString()).setValue(logEntry);
    }


}