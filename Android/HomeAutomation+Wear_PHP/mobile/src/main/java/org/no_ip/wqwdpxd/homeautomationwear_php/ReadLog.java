package org.no_ip.wqwdpxd.homeautomationwear_php;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Dominik on 23.11.2016..
 */

public class ReadLog extends AsyncTask<String, String, String> {

    boolean isMainActivity=false;
    public LogsActivity activityLogs;
    public ReadLog(LogsActivity a) {
        this.activityLogs = a;
    }
    public MainActivity activityMain;
    public ReadLog(MainActivity b) {
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
        String urltext="";
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
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(3000);
            } catch (java.net.SocketTimeoutException e) {
                result = "ERR: Timeout";
            } catch (IOException e) {
                e.printStackTrace();
                result = e.toString();
            }
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        urlConnection.getInputStream()));
                String inputLine;
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
                if(!phpReturned.equals("Success")&&getLogs)
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
