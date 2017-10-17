package org.no_ip.wqwdpxd.homeautomationwear_php;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class LogsActivity extends AppCompatActivity {

    private String prefs = getString(R.string.prefs);
    private String localWiFi = getString(R.string.localWiFi);
    private String localIP = getString(R.string.local_IP);
    private String wanIP = getString(R.string.wan_IP);
    private String user1 = getString(R.string.user1);
    
    boolean logOpen=false;
    ArrayList<String> logList = new ArrayList<String>();
    private ListView mLogList;
    private ArrayAdapter<String> mLogAdapter;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    public String serverIP;
    public String serverIP_AUTO;
    public String user="Unknown";
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs);

        pref = getApplicationContext().getSharedPreferences(prefs, 0); // 0 - for private mode
        editor = pref.edit();
        boolean loggedIn = pref.getBoolean("login", false);
        if(!loggedIn) {
            displayToast("Please login first.");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        user=pref.getString("username","Unknown");
        serverIP=pref.getString("server_ip",localIP);
        serverIP_AUTO=pref.getString("server_ip_auto",localIP);
        mLogList = (ListView)findViewById(R.id.logList);
        mDrawerList = (ListView)findViewById(R.id.navList2);


        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.setTitleTextColor(0xFFFFFFFF);
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorAppLogsShade));


        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);

        getLogs();
        //addLogItems();

        addDrawerItems();
        setupDrawer();
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(position){
                    case 0:

                        finish();
                        break;

                    case 1:
                        mDrawerLayout.closeDrawers();
                        break;

                    case 2:
                        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.vMEyeSuper");
                        if (launchIntent != null) {
                            startActivity(launchIntent);
                        }else{
                            mDrawerLayout.closeDrawers();
                            Intent intentLogs = new Intent(Intent.ACTION_VIEW);
                            intentLogs.setData(Uri.parse("market://details?id=com.vMEyeSuper"));
                            startActivity(intentLogs);
                        }

                        break;


                }

            }
        });


        mLogList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mLogList.getItemAtPosition(position).equals("Reload Logs")){
                    logList.clear();
                    getLogs();
                }else if(!logOpen) {
                    String logname = "log_" + mLogList.getItemAtPosition(position) + ".txt";
                    //displayToast(logname);
                    getSupportActionBar().setTitle("Logs: " + mLogList.getItemAtPosition(position));
                    logList.clear();
                    getLog(logname);
                    logOpen = true;
                }
            }
        });


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    public void addLog(String log_name){
        logList.add(log_name);
    }

    public void addLogItems() {
if(logList.size()==0){
    logOpen=false;
    String[] logRetry ={"Reload Logs"};
    mLogAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, logRetry);
}else {
    String[] logArray = new String[logList.size()];
    for (int i = 0; i < logList.size(); i++) {
        logArray[i] = logList.get(i).toString();
    }

    String[] logNameArray = new String[logList.size()];
    for (int i = 0; i < logList.size(); i++) {
        logNameArray[i] = logArray[i].substring(0, logArray[i].length() - 4).substring(4);
    }

    if (!logOpen)
        mLogAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, logNameArray);
    else
        mLogAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, logArray);
}

    mLogList.setAdapter(mLogAdapter);

    }

    public void getLogs(){
        if(networkConnected()) {

            new ReadLog(this).execute("true",serverIP,user,"no_log","LogsActivity");

        }else{
            displayToast("No internet connection!");
        }

    }
    public void getLog(String log_name){
        if(networkConnected()) {

                new ReadLog(this).execute("false",serverIP,user,log_name,"LogsActivity");

        }else{
            displayToast("No internet connection!");
        }

    }

    public boolean networkConnected() {

        ConnectivityManager cm =
                (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();


        return isConnected;
    }
    public void displayToast(String text2){
        Context context = getApplicationContext();
        CharSequence text = text2;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    @Override
    public void onBackPressed() {
        if(logOpen) {
            logOpen=false;
            logList.clear();
            getSupportActionBar().setTitle("Logs");
            getLogs();
        }
    }

    private void addDrawerItems() {
        String[] osArray = { "Controls","Logs","Cameras" };
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray);
        mDrawerList.setAdapter(mAdapter);
    }

    private void setupDrawer() {

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //getSupportActionBar().setTitle("Navigation");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                //getSupportActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.menu_logout:
                updateLogin(false,"Not_important");
                return true;

            case R.id.menu_about:
                String versionName = null;
                try {
                    versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                displayToast("App version: " + versionName);
                return true;

            case R.id.menu_reload:
                displayToast("Only possible from Control screen!");
                return true;

            case R.id.menu_LAN:
                serverIP=pref.getString("server_ip",localIP);
                if(serverIP.equals(localIP)) {
                    serverIP = wanIP;
                    item.setChecked(false);
                }else {
                    item.setChecked(true);
                    serverIP = localIP;
                }
                editor.putString("server_ip",serverIP);
                editor.commit();

                displayToast("New IP: "+serverIP);
                return true;

            case R.id.menu_LAN_AUTO:
                serverIP_AUTO=pref.getString("server_ip_auto","true");
                if(serverIP_AUTO.equals("false")) {
                    item.setChecked(true);
                    serverIP_AUTO="true";
                    editor.putString("server_ip_auto","true");
                }else {
                    serverIP_AUTO="false";
                    item.setChecked(false);
                    editor.putString("server_ip_auto","false");
                }
                editor.commit();

                return true;

            case android.R.id.home:
                    finish();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.

                return super.onOptionsItemSelected(item);



        }

    }

    public void updateLogin(boolean success, String userid){

        SharedPreferences pref = getSharedPreferences(prefs, 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("login",success);
        editor.putString("username",userid);
        editor.commit();

        if(!success){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.overflow, menu);
        MenuItem checkbox_LAN = menu.findItem(R.id.menu_LAN);
        if(serverIP.equals(localIP))
            checkbox_LAN.setChecked(true);
        else
            checkbox_LAN.setChecked(false);

        MenuItem checkbox_LAN_AUTO = menu.findItem(R.id.menu_LAN_AUTO);

        if(serverIP_AUTO.equals("true"))
            checkbox_LAN_AUTO.setChecked(true);
        else
            checkbox_LAN_AUTO.setChecked(false);

        return true;
    }

    public void retryWan(){
        new AlertDialog.Builder(this)
                .setTitle("Server not found!")
                .setMessage("Server: "+serverIP+" not found. Would you like to swap IP address?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        if(serverIP.equals(localIP))
                            serverIP=wanIP;
                        else
                            serverIP=localIP;


                        editor.putString("server_ip",serverIP);
                        editor.commit();
                        displayToast("Changed to: "+serverIP);
                    }})
                .setNegativeButton(android.R.string.no, null).show();


    }

}
