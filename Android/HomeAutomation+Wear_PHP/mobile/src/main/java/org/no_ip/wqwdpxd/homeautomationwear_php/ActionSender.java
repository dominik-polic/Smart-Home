package org.no_ip.wqwdpxd.homeautomationwear_php;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ActionSender {

    public static void sendCommandFb(String node, Object action,String user, Context context, String origin){

        try {
            action = action.toString();
        }catch (NullPointerException e){
            Log.e("FIREBASE","Error converting \"action\"Object to String");
        }

        if(!networkConnected(context))
            displayToast("No network",context);

        FirebaseDatabase myDb;
        DatabaseReference dbRef_remote;
        DatabaseReference dbRef_logs;

        myDb = FirebaseDatabase.getInstance();
        dbRef_remote = myDb.getReference("remote");
        dbRef_logs = myDb.getReference("logs");


        //Send execution command
        dbRef_remote.child(node).setValue(action);



        //Add log
        LogEntry logEntry = new LogEntry(action, node, user, origin);

        dbRef_logs.child(DateFormat.format("yyyy-MM-dd", new java.util.Date()).toString())
                .child(DateFormat.format("HH:mm:ss", new java.util.Date()).toString()).setValue(logEntry);

    }


    public static boolean networkConnected(Context context) {

        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = null;
        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
        }


        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }



    public static void displayToast(String text2, Context context){
        try {
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text2, duration);
            toast.show();
        }catch (Exception e){
            Log.e("TOAST",e.getMessage());
        }
    }
}
