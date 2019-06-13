package org.no_ip.wqwdpxd.homeautomationwear_php;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ActionReceiver extends BroadcastReceiver {

    private static final long CONNECTION_TIME_OUT_MS = 2000;
    private GoogleApiClient client;
    private String nodeId = "00000000";
    private static final String clickBtnGateOpen = "W_B_GOPEN";
    private static final String clickBtnGateClose = "W_B_GCLOSE";
    private static final String clickBtnLightOn = "W_B_LON";
    private static final String clickBtnLightOff = "W_B_LOFF";
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    static String prefs = "dominik_polic_home_automation_prefs_WearOS";
    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        initApi(context);
        Log.d("WEAR_TILE","Intent received: " + intent.getAction());

        if(intent.getAction().equals(clickBtnGateClose)){
            sendToast("node=gate2&action=close");
        } else if(intent.getAction().equals(clickBtnGateOpen)){
            sendToast("node=gate2&action=open");
        } else if(intent.getAction().equals(clickBtnLightOff)){
            sendToast("node=light_dominik&action=0");
            Log.d("WEAR_TILE","light off");
        } else if(intent.getAction().equals(clickBtnLightOn)){
            Log.d("WEAR_TILE","light on");
            sendToast("node=light_dominik&action=255");
        }

    }



    private void initApi(Context context) {
        client = getGoogleApiClient(context);
        Log.d("DEBUG","client: "+client.toString());
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
        pref = context.getSharedPreferences(prefs, 0);
        nodeId = pref.getString("nodeId","Unknown");
        Log.d("DEBUG","client: "+nodeId);
        //while(nodeId==null||nodeId.equals(00000000));

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