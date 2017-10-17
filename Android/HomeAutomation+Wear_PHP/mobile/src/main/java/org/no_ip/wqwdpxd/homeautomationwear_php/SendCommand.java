package org.no_ip.wqwdpxd.homeautomationwear_php;

import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamCorruptedException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Dominik on 14.11.2016..
 */



public class SendCommand extends AsyncTask<String, String, String> {

   private String localIP = "192.168.1.20";


    public String parentActivity="MainActivity";
    public boolean retriable = false;
    public String porukica = "";
    public String korisnik = "";
    public String phpReturned;
    public MainActivity activity;
    public SendCommand(MainActivity a) {
        this.activity = a;
    }

    public ListenerService activity2;
    public SendCommand(ListenerService a) {
        this.activity2 = a;
    }

    protected String doInBackground(String... message) {

    parentActivity=message[3];
        String result="Executed";


        URL url = null;
        String urltext="http://"+message[2]+"/writer_mysql.php?"+message[0]+"&user="+message[1];
        if(message[2].equals(localIP))
            retriable=true;
        try {

            url = new URL(urltext);
            porukica=message[0];
            korisnik=message[1];
        } catch (MalformedURLException e) {
            e.printStackTrace();
            result=e.toString();
        }
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            if(message[2].equals(localIP))urlConnection.setConnectTimeout(1000);
            else urlConnection.setConnectTimeout(3000);
        } catch (java.net.SocketTimeoutException e) {
            result="ERR: Timeout on LAN";
        } catch (IOException e) {
            e.printStackTrace();
            result=e.toString();
        }
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    urlConnection.getInputStream()));
            String inputLine;
            boolean first=true;
            while ((inputLine = in.readLine()) != null)
                if(first) {
                    phpReturned = inputLine;
                    first=false;
                }
            if(!phpReturned.equals("Success"))
                result="PHP Error";
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            result=e.toString();
        } finally {
            urlConnection.disconnect();
        }

        return result;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        //activity.displayToast(phpReturned);//Text from php script
        Global.SendCommand_running=false;
        if (parentActivity.equals("MainActivity")) {
            if (!s.equals("Executed")) {
                activity.retryWan();
            } else if (!s.equals("Executed")) {
                activity.displayToast(s);
            }



        } /*else {

            if (parentActivity.equals("MainActivity")) {
                if (!s.equals("Executed")) {
                } else{ //if (!s.equals("Executed")) {
                    Toast.makeText(activity2, s, Toast.LENGTH_LONG).show();
                }
            }
        }*/
    }



}


