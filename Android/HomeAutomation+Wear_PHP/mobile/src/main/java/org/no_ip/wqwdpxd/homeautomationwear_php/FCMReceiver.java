package org.no_ip.wqwdpxd.homeautomationwear_php;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FCMReceiver extends FirebaseMessagingService {

    private static String TAG = "FCM_RECEIVER";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());


        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());



        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Title: " + remoteMessage.getNotification().getTitle());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }





    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token){
        String prefs = getResources().getString(R.string.prefs);
        SharedPreferences pref = getApplicationContext().getSharedPreferences(prefs, 0); // 0 - for private mode
        String id=pref.getString("user_id", "0");
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("FCM_token", token);
        editor.apply();

        FirebaseDatabase myDb;
        DatabaseReference dbRef_tokens;

        myDb = FirebaseDatabase.getInstance();
        dbRef_tokens = myDb.getReference("users");


        //Send execution command
        if(!id.equals("0"))
            dbRef_tokens.child(id).child("FCM_token").setValue(token);

    }

}

