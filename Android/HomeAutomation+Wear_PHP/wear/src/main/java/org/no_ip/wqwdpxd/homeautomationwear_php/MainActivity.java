package org.no_ip.wqwdpxd.homeautomationwear_php;

import android.content.Context;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
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

public class MainActivity extends WearableActivity implements AdapterView.OnItemSelectedListener {
    public int brightness1=0,brightness2=0,speed3=0;
    private static final long CONNECTION_TIME_OUT_MS = 2000;

    private GoogleApiClient client;
    private String nodeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setAmbientEnabled();
        initApi();
        Spinner spinner = findViewById(R.id.spinner);
        SeekBar vskbBrightnesMaster = findViewById(R.id.skbBrightnessMaster);
        SeekBar vskbBrightnessRGB = findViewById(R.id.skbBrightnessRGB);
        SeekBar vskbSpeedRGB = findViewById(R.id.skbSpeedRGB);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(MainActivity.this,
                R.array.modes_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(MainActivity.this);

        vskbBrightnesMaster.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {


            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                brightness1=progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sendToast("node=lightbrightness_dominik&action="+brightness1);
            }
        });


        vskbBrightnessRGB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {


            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                brightness2=progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sendToast("node=rgbbrightness_dominik&action="+brightness2);
            }
        });

        vskbSpeedRGB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {


            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                speed3=progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                speed3=speed3+27;
                if(speed3>127)speed3=127;
                sendToast("node=rgbspeed_dominik&action="+(127-speed3));
            }
        });




        //Da tu...

    }
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        sendToast("node=rgbmode_dominik&action="+parent.getSelectedItemPosition());
    }



    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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



    public void onLightsOn(View target) {
        sendToast("node=light_dominik&action=on");
    }
    public void onLightsOff(View target) {
        sendToast("node=light_dominik&action=off");
    }
    public void onGate2Open(View target) {
        sendToast("node=gate2&action=open");
    }
    public void onGate2Close(View target) {
        sendToast("node=gate2&action=closed");
    }
    public void onGate1ShortOpen(View target) {
        sendToast("node=gate1&action=pulse");
    }
    public void onRGBRemote(View target) {
        sendToast("node=rgboverride_dominik&action=true");
    }
    public void onDoorLock(View target) {
        sendToast("node=door_dominik&action=locked");
    }
    public void onDoorUnlock(View target) {
        sendToast("node=door_dominik&action=unlocked");
    }
    public void onDoorHouseLock(View target) {
        sendToast("node=door_main&action=locked");
    }
    public void onDoorHouseUnlock(View target) {
        sendToast("node=door_main&action=unlocked");
    }
    public void onBellRing(View target) {
        sendToast("node=bell&action=pulse");
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
            new Thread(new Runnable() {
                @Override
                public void run() {
                    client.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
                    Wearable.MessageApi.sendMessage(client, nodeId, poruka, null);
                    client.disconnect();

                }
            }).start();
        }
    }

}
