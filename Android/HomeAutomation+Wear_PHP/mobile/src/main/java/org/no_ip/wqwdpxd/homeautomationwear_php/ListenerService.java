package org.no_ip.wqwdpxd.homeautomationwear_php;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.DateFormat;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ListenerService extends WearableListenerService {

    String prefs;
    String user1;
    @Override
    public void onCreate(){
        super.onCreate();
        Log.d("WEAR","Starting listener service...");
        try {
            prefs = getString(R.string.prefs);
            user1 = getString(R.string.user1);
            Log.d("WEAR","Loading success");
        }catch(Exception e){
            Log.d("WEAR",e.getMessage());
        }

    }


    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        processData(messageEvent.getPath());
    }

    private void processData(String message) {


        final SharedPreferences pref = getApplicationContext().getSharedPreferences(prefs, 0); // 0 - for private mode

        String user = pref.getString("username", "Unknown");

        String node = message.split("&")[0].split("=")[1];
        String action = message.split("&")[1].split("=")[1];


        sendCommandFb(node,action,user);

    }

    public void sendCommandFb(String node, String action, String user){

        try {
            action = action.toString();
        }catch (NullPointerException e){
            Log.e("FIREBASE","Error converting \"action\"Object to String");
        }

        FirebaseDatabase myDb;
        DatabaseReference dbRef_remote;
        DatabaseReference dbRef_logs;
        DatabaseReference dbRef_nodered;

        myDb = FirebaseDatabase.getInstance();
        dbRef_nodered = myDb.getReference("nodered");
        dbRef_remote = myDb.getReference("remote");
        dbRef_logs = myDb.getReference("logs");


        //Send execution command
        dbRef_remote.child(node).setValue(action);



        //Add log
        LogEntry logEntry = new LogEntry(action, node, user,"ANDROID_WEAR_OS");

        dbRef_logs.child(DateFormat.format("yyyy-MM-dd", new java.util.Date()).toString())
                .child(DateFormat.format("hh:mm:ss", new java.util.Date()).toString()).setValue(logEntry);

    }

}
