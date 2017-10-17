package org.no_ip.wqwdpxd.homeautomationwear_php;

/**
 * Created by Dominik on 16.11.2016..
 */
        import android.content.Context;
        import android.content.SharedPreferences;
        import android.net.ConnectivityManager;
        import android.net.NetworkInfo;
        import android.net.wifi.WifiInfo;
        import android.net.wifi.WifiManager;
        import android.widget.Toast;

        import com.google.android.gms.wearable.MessageEvent;
        import com.google.android.gms.wearable.WearableListenerService;

public class ListenerService extends WearableListenerService {

    public String serverIP;
    public String user;
    public String logUser;
    public String serverIP_AUTO;

    private String prefs = getString(R.string.prefs);
    private String localWiFi = getString(R.string.localWiFi);
    private String localIP = getString(R.string.local_IP);
    private String wanIP = getString(R.string.wan_IP);
    private String user1 = getString(R.string.user1);
    private String user2 = getString(R.string.user2);
    private String user3 = getString(R.string.user3);
    private String user4 = getString(R.string.user4);
    private String user5 = getString(R.string.user5);
    private String user6 = getString(R.string.user6);

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        showToast(messageEvent.getPath());
    }

    private void showToast(String message) {
       final SharedPreferences pref = getApplicationContext().getSharedPreferences(prefs, 0); // 0 - for private mode
        final  SharedPreferences.Editor editor = pref.edit();
        user=pref.getString("username", "Unknown");
        serverIP_AUTO=pref.getString("server_ip_auto","true");
        if(serverIP_AUTO.equals("true")){

            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String wifiname=wifiInfo.getSSID();
            if (("\""+localWiFi+"\"").equals(wifiname)) {
                serverIP = localIP;
            } else {
                serverIP=wanIP;
            }
            editor.putString("server_ip",serverIP);
            editor.commit();
        }else{

            serverIP = pref.getString("server_ip", localIP);
        }
        logUser=pref.getString("username", "unknown");




        if(logUser.equals(user1))
            new SendCommand(this).execute(message,user,serverIP,"ListenerService");

    }


}
