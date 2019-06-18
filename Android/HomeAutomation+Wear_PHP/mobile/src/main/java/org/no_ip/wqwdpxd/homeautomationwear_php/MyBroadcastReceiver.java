package org.no_ip.wqwdpxd.homeautomationwear_php;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Switch;

public class MyBroadcastReceiver extends BroadcastReceiver {
    private static final String ACTION_GATE1 = "MY_BROADCAST_RECEIVER_OPEN_GATE_1";
    private static final String ACTION_GATE2 = "MY_BROADCAST_RECEIVER_OPEN_GATE_2";
    private static String ORIGIN = "ANDROID-INTENT";

    @Override
    public void onReceive(Context context, Intent intent) {

        String prefs = context.getResources().getString(R.string.prefs);
        final SharedPreferences pref = context.getApplicationContext().getSharedPreferences(prefs, 0); // 0 - for private mode
        String user=pref.getString("username", "Unknown");
        NotificationManager notificationManager = (NotificationManager)  context.getSystemService(context.NOTIFICATION_SERVICE);

        switch(intent.getAction()){
            case ACTION_GATE1:
                Log.d("INTENT-RECEIVER","Opening gate 1");
                ActionSender.sendCommandFb("gate_1","pulse",user,context,ORIGIN+"-NOTIFICAION-BUTTON");
                //Remove the notification when done
                break;


            case ACTION_GATE2:
                Log.d("INTENT-RECEIVER","Opening gate 2");
                ActionSender.sendCommandFb("gate_2","open",user,context,ORIGIN+"-NOTIFICAION-BUTTON");
                //Remove the notification when done
                notificationManager.cancel(0);
                break;

            default:
                Log.d("BROADCAST-RECEIVER","Received unknown intent: "+intent.toString());
                break;


        }




    }
}
