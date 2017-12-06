package org.no_ip.wqwdpxd.homeautomationwear_php;

import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class ListenerService extends WearableListenerService {

    String prefs;
    String localWiFi;
    String localIP;
    String wanIP;
    String user1;
    @Override
    public void onCreate(){
        super.onCreate();
        Log.d("WEAR","Starting listener service...");
        try {
            prefs = getString(R.string.prefs);
            localWiFi = getString(R.string.localWiFi);
            localIP = getString(R.string.local_IP);
            wanIP = getString(R.string.wan_IP);
            user1 = getString(R.string.user1);
            Log.e("WEAR","Loading success");
        }catch(Exception e){
            Log.e("WEAR",e.getMessage());
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
        String serverIP_AUTO = pref.getString("server_ip_auto", "true");
        String serverIP;
        if(serverIP_AUTO.equals("true")){

            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = null;
            if (wifiManager != null) {
                wifiInfo = wifiManager.getConnectionInfo();
            }
            String wifiname= null;
            if (wifiInfo != null) {
                wifiname = wifiInfo.getSSID();
            }
            if (("\""+localWiFi+"\"").equals(wifiname)) {
                serverIP = localIP;
            } else {
                serverIP =wanIP;
            }
            editor.putString("server_ip", serverIP);
            editor.apply();
        }else{

            serverIP = pref.getString("server_ip", localIP);
        }
        String logUser = pref.getString("username", "unknown");




        if(logUser.equals(user1))
            new SendCommand().execute(message, user, serverIP,"ListenerService");

    }


}
