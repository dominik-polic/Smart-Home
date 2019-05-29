package org.no_ip.wqwdpxd.homeautomationwear_php;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;




public class SendCommand extends AsyncTask<String, String, String> {


    private String parentActivity="MainActivity";
    @SuppressLint("StaticFieldLeak")
    private MainActivity activity;
    SendCommand(MainActivity a) {
        this.activity = a;
    }

    SendCommand() {
    }

    protected String doInBackground(String... message) {

    parentActivity=message[2];
        String result="Executed";


        URL url = null;
        String urltext="http://polichousecontrol.ddns.net:1880/remote/command?"+message[0]+"&user="+message[1];
        try {

            url = new URL(urltext);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            result=e.toString();
        }
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) (url != null ? url.openConnection() : null);

                if (urlConnection != null) {
                    urlConnection.setConnectTimeout(1000);
                }



            InputStream in = null;
            InputStreamReader isw;
            if (urlConnection != null) {
                in = urlConnection.getInputStream();
                isw = new InputStreamReader(in);

                int data = isw.read();
                String response="";
                while (data != -1) {
                    char current = (char) data;
                    data = isw.read();
                    response+=current;
                }



                if (in != null) {
                    in.close();
                }
            }else{
                result = "PHP Error";
            }
        } catch (IOException e) {
            e.printStackTrace();
            result=e.toString();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
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


