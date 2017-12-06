package org.no_ip.wqwdpxd.homeautomationwear_php;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ReadLog extends AsyncTask<String, String, String> {

    private boolean isMainActivity=false;
    @SuppressLint("StaticFieldLeak")
    private LogsActivity activityLogs;
    ReadLog(LogsActivity a) {
        this.activityLogs = a;
    }
    @SuppressLint("StaticFieldLeak")
    private MainActivity activityMain;
    ReadLog(MainActivity b) {
        this.activityMain = b;
    }

    protected String doInBackground(String... message) {
        boolean getLogs=(message[0].equals("true"));
        String result="Logs Loaded";
        String link=message[1];
        String user=message[2];
        String logName=message[3];
        String phpReturned="Nothing";
        if(message[4].equals("MainActivity"))
            isMainActivity=true;

        URL url = null;
        String urltext;
        if(getLogs)
             urltext = "http://" + link + "/logs/list_all.php?user=" + user;
        else if(!isMainActivity)
            urltext = "http://" + link + "/logs/"+logName;
        else
            urltext = "http://" + link + "/current_status_mysql.php";


                    try {
                url = new URL(urltext);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                result = e.toString();
            }
            HttpURLConnection urlConnection = null;
            try {
                if (url != null) {
                    urlConnection = (HttpURLConnection) url.openConnection();
                }
                if (urlConnection != null) {
                    urlConnection.setConnectTimeout(3000);
                }
            } catch (java.net.SocketTimeoutException e) {
                result = "ERR: Timeout";
            } catch (IOException e) {
                e.printStackTrace();
                result = e.toString();
            }
            try {
                BufferedReader in = null;
                if (urlConnection != null) {
                    in = new BufferedReader(new InputStreamReader(
                            urlConnection.getInputStream()));
                }
                String inputLine;
                if (in != null) {
                    while ((inputLine = in.readLine()) != null){
                        if(inputLine.equals("-START-"))
                            result="Got Status";
                            if(inputLine.equals("Success")||inputLine.equals("-START-"))
                                phpReturned="Success";
                            else if(!isMainActivity)
                                activityLogs.addLog(inputLine);
                            else
                                activityMain.addStatusLine(inputLine);

                        }
                }
                if(!phpReturned.equals("Success")&&getLogs)
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

    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Global.ReadLog_running=false;
        if(!s.equals("Logs Loaded")&&!s.equals("Got Status"))
            if(!isMainActivity)
                activityLogs.retryWan();
        if(!isMainActivity)
            activityLogs.addLogItems();
        else {
            activityMain.updateValues();
        }
    }

}
