package org.no_ip.wqwdpxd.homeautomationwear_php;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class MainActivity extends WearableActivity {

    private SeekBar vsbLightD;
    private FirebaseDatabase myDb;
    private DatabaseReference dbRef_nodered;
    private DatabaseReference dbRef_remote;
    private DatabaseReference dbRef_logs;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myDb = FirebaseDatabase.getInstance();
        dbRef_nodered = myDb.getReference("nodered");
        dbRef_remote = myDb.getReference("remote");
        dbRef_logs = myDb.getReference("logs");


        setAmbientEnabled();

        vsbLightD = findViewById(R.id.sbLight);


        vsbLightD.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {


            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sendCommandFb("light_dominik",vsbLightD.getProgress());
            }
        });


        //Da tu...

    }




    private void sendCommandFb(String node, Object action){
        String user = "wearos";

        try {
            action = action.toString();
        }catch (NullPointerException e){
            Log.e("FIREBASE","Error converting \"action\"Object to String");
        }




        //Send execution command
        dbRef_remote.child(node).setValue(action);

        //Add log
        LogEntry logEntry = new LogEntry(action, node, user);
        dbRef_logs.child(DateFormat.format("yyyy-MM-dd", new java.util.Date()).toString())
                .child(DateFormat.format("HH:mm:ss", new java.util.Date()).toString()).setValue(logEntry);
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        updateDisplay();
        super.onExitAmbient();
    }

    private void updateDisplay() {
        if (isAmbient()) {
            finish();
        }
    }




    public void onGate2Open(View target) {
        sendCommandFb("gate_2","open");
    }
    public void onGate2Close(View target) {
        sendCommandFb("gate_2","close");
    }
    public void onGate1ShortOpen(View target) {
        sendCommandFb("gate_1", "pulse");
    }
    public void onDoorHouseLock(View target) {
        sendCommandFb("door_house","lock");
    }
    public void onDoorHouseUnlock(View target) {
        sendCommandFb("door_house","unlock");
    }







}
