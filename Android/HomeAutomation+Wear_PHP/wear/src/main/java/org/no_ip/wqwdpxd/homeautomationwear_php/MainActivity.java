package org.no_ip.wqwdpxd.homeautomationwear_php;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends WearableActivity {
    public int brightness1=0,brightness2=0,speed3=0;
    private static final long CONNECTION_TIME_OUT_MS = 2000;

    private GoogleApiClient client;
    private String nodeId;
    private SeekBar vsbLightD;

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    static String prefs = "dominik_polic_home_automation_prefs_WearOS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pref = getApplicationContext().getSharedPreferences(prefs, 0);
        editor = pref.edit();
        setAmbientEnabled();
        initApi();

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
                sendToast("node=light_dominik&action="+vsbLightD.getProgress());
            }
        });


        //Da tu...

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
        sendToast("node=gate2&action=open");
    }
    public void onGate2Close(View target) {
        sendToast("node=gate2&action=close");
    }
    public void onGate1ShortOpen(View target) {
        sendToast("node=gate1&action=pulse");
    }
    public void onDoorHouseLock(View target) {
        sendToast("node=door_house&action=lock");
    }
    public void onDoorHouseUnlock(View target) {
        sendToast("node=door_house&action=unlock");
    }



    private void initApi() {
        client = getGoogleApiClient(this);
        retrieveDeviceNode();
    }



    private GoogleApiClient getGoogleApiClient(Context context) {
        return new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build();
    }

    private void retrieveDeviceNode() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                client.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
                NodeApi.GetConnectedNodesResult result =
                        Wearable.NodeApi.getConnectedNodes(client).await();
                List<Node> nodes = result.getNodes();
                if (nodes.size() > 0) {
                    nodeId = nodes.get(0).getId();
                }
                client.disconnect();
            }
        }).start();
    }

    private void sendToast(final String poruka) {
        if (nodeId != null) {

            pref = getApplicationContext().getSharedPreferences(prefs, 0);
            editor = pref.edit();
            editor.putString("nodeId", nodeId);
            editor.apply();
            Log.d("DEBUG","settings-saved nodeId: "+ pref.getString("nodeId","ERROR"));

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d("DEBUG","nodeId: " + nodeId);
                    client.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
                    Wearable.MessageApi.sendMessage(client, nodeId, poruka, null);
                    client.disconnect();

                }
            }).start();
        }
    }

}
