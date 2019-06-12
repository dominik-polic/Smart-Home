package org.no_ip.wqwdpxd.homeautomationwear_php;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
public class ReadLog extends AsyncTask<String, String, String> {

    private boolean isMainActivity=false;
    @SuppressLint("StaticFieldLeak")
    private MainActivity activityMain;
    ReadLog(MainActivity b) {
        this.activityMain = b;
    }

    protected String doInBackground(String... message) {
        boolean getLogs=(message[0].equals("true"));
        String result="Logs Loaded";
        String user=message[1];
        String logName=message[2];
        String phpReturned="Nothing";
        if(message[3].equals("MainActivity"))
            isMainActivity=true;

        URL url = null;
        String urltext;

            urltext = "http://polichousecontrol.ddns.net:1880/remote/status";

        if(message[0].equals("status")) {

            HttpURLConnection urlConnection = null;
            try {
                url = new URL(urltext);
                if (url != null) {
                   urlConnection = (HttpURLConnection) url.openConnection();
                }
                if (urlConnection != null) {


                    //Log.d("DEBUG-READLOG","DEBUG1");
                    urlConnection.setConnectTimeout(3000);

                }


                //Log.d("DEBUG-READLOG","DEBUG3");
                InputStream in = null;
                InputStreamReader isw;
                if (urlConnection != null) {
                    in = urlConnection.getInputStream();
                    isw = new InputStreamReader(in);

                    int data = isw.read();
                    String response = "";
                    while (data != -1) {
                        char current = (char) data;
                        data = isw.read();
                        response += current;
                    }
                    //Log.d("DEBUG-READLOG",response);

                    JSONObject reader = new JSONObject(response);
                    MainActivity.vswLight1State=reader.getString("light_livingroom_1").equals("on")?true:false;
                    MainActivity.vswLight2State=reader.getString("light_livingroom_2").equals("on")?true:false;
                    MainActivity.vswHouseDoorState=reader.getString("house_door_state").equals("unlock")?true:false;
                    MainActivity.vswGate2State=reader.getString("gate2_state").equals("open")?true:false;
                    MainActivity.vsbLightDState=Integer.parseInt(reader.getString("light_dominik"));
                    if (in != null) {
                        in.close();
                    }
                }else{
                    result = "PHP Error";
                }
            } catch (IOException e) {
                e.printStackTrace();
                result = e.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

        return result;
        }

            return null;
    }

    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Global.ReadLog_running=false;

    }

}
