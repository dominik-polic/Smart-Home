package org.no_ip.wqwdpxd.homeautomationwear_php;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FCMReceiver extends FirebaseMessagingService {

    private static String TAG = "FCM_RECEIVER";
    private static String CH_BELL_ID = "NCH_BELL";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if(remoteMessage.getFrom().equals("/topics/bell")) {
            try {
                Log.d("DEBUG","TAG1");
                String title = remoteMessage.getData().get("title");
                String body = remoteMessage.getData().get("body");
                String icon = remoteMessage.getData().get("icon");
                String picture = remoteMessage.getData().get("picture");


                Intent intent = new Intent(this,ImageActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);
                Uri sounduri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                createNotificationChannel();

                final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                builder.setContentTitle(title);
                builder.setContentText(body);
                builder.setContentIntent(pendingIntent);
                builder.setSound(sounduri);
                builder.setSmallIcon(R.drawable.bell);
                builder.setChannelId(CH_BELL_ID);

                Log.d("DEBUG","TAG2");
                ImageRequest imageRequest = new ImageRequest(picture, new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        Log.d("DEBUG","TAG3");
                        builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(response));
                        NotificationManager notificationManager = (NotificationManager)  getSystemService(NOTIFICATION_SERVICE);
                        notificationManager.notify(0,builder.build());

                    }
                }, 0, 0, null, Bitmap.Config.RGB_565, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("DEBUG","TAG-ERR: "+error);
                    }
                });

                Log.d("DEBUG","TAG3");
            MySingleton.getmInstance(this).addToRequestQueue(imageRequest);

                Log.d("DEBUG","TAG4");
            } catch (Exception e) {
                Log.d("WRONG-NOTIFICATION", "error parsing: " + e);
            }

        }else{
            Log.d("UNKNOWN-NOTIFICATION",remoteMessage.getData() + ", from: "+remoteMessage.getFrom());
        }

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


    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Bell notifications";
            String description = "This notifies you when someone rings a bell";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CH_BELL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
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

