package org.no_ip.wqwdpxd.homeautomationwear_php;

import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

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
        final SharedPreferences.Editor editor = pref.edit();
        String user = pref.getString("username", "Unknown");

        String logUser = pref.getString("username", "unknown");




        if(logUser.equals(user1))
            new SendCommand().execute(message, user,"ListenerService");

    }


}
