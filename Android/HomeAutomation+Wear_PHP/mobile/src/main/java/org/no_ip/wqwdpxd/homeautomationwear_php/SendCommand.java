package org.no_ip.wqwdpxd.homeautomationwear_php;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;




public class SendCommand extends AsyncTask<String, String, String> {


    private String parentActivity="MainActivity";
    private String phpReturned;
    @SuppressLint("StaticFieldLeak")
    private MainActivity activity;
    SendCommand(MainActivity a) {
        this.activity = a;
    }

    SendCommand() {
    }

    protected String doInBackground(String... message) {

    parentActivity=message[3];
        String result="Executed";


        URL url = null;
        String urltext="http://"+message[2]+"/writer_mysql.php?"+message[0]+"&user="+message[1];
        String localIP = "192.168.1.20";
        try {

            url = new URL(urltext);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            result=e.toString();
        }
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) (url != null ? url.openConnection() : null);
            if(message[2].equals(localIP)) {
                if (urlConnection != null) {
                    urlConnection.setConnectTimeout(1000);
                }
            }else{
                if (urlConnection != null) {
                    urlConnection.setConnectTimeout(3000);
                }
            }
        } catch (java.net.SocketTimeoutException e) {
            result="ERR: Timeout on LAN";
        } catch (IOException e) {
            e.printStackTrace();
            result=e.toString();
        }
        try {
            BufferedReader in = null;
            if (urlConnection != null) {
                in = new BufferedReader(new InputStreamReader(
                        urlConnection.getInputStream()));
            }
            String inputLine;
            boolean first=true;
            if (in != null) {
                while ((inputLine = in.readLine()) != null)
                    if(first) {
                        phpReturned = inputLine;
                        first=false;
                    }
            }
            if(!phpReturned.equals("Success"))
                result="PHP Error";
            if (in != null) {
                in.close();
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


