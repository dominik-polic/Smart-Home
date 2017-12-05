package org.no_ip.wqwdpxd.homeautomationwear_php;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.Timer;
import java.util.TimerTask;

import me.priyesh.chroma.ChromaDialog;
import me.priyesh.chroma.ColorMode;
import me.priyesh.chroma.ColorSelectListener;
import yuku.ambilwarna.AmbilWarnaDialog;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    //Navigation Drawer
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;

    public int firstrgb=0;
    public String serverIP;
    public String serverIP_AUTO;
    public String user="Unknown";
    public int startingcolor=Color.BLACK;
    public int brightness1=0,brightness2=0,speed3=0;
    public boolean first=true;
    private Button vbtnLightsOn;
    private Button vbtnLightsOff;
    private Button vbtnGate2Open;
    private Button vbtnGate2Close;
    private Button vbtnTestConnection;
    private Button vbtnRGBRemote;
    private Button vbtnDoorLock;
    private Button vbtnDoorUnlock;
    private Button vbtnDoorHouseLock;
    private Button vbtnDoorHouseUnlock;
    private Button vbtnBellRing;
    private Button vbtnBrightnessRGBIncrease;
    private Button vbtnBrightnessRGBDecrease;
    private Button vbtnBrightnessMasterIncrease;
    private Button vbtnBrightnessMasterDecrease;
    private Button vbtnResetArduino;
    private Button vbtnStopServer;
    private Button vbtnGate1ShortOpen;
    private Button vbtnPickColor;
    private Spinner spinner;
    private SeekBar vskbBrightnessRGB;
    private SeekBar vskbBrightnesMaster;
    private SeekBar vskbSpeedRGB;
    SharedPreferences pref;
    SharedPreferences.Editor editor;


    //Staus values BEGIN-----------
    public boolean x_lightson=false;
    public boolean x_gate2locked=false;
    public boolean x_doorhouselocked=false;
    public int x_rgbmode=0;
    public boolean x_rgboverride=false;
    public int x_rgbspeed=0;
    public int x_rgbbrightness=0;
    public int x_redbrightness=0;
    public int x_greenbrightness=0;
    public int x_bluebrightness=0;
    public int x_masterlightbrightness=0;
    //Status values END------------


    private String prefs = getString(R.string.prefs);
    private String localWiFi = getString(R.string.localWiFi);
    private String localIP = getString(R.string.local_IP);
    private String wanIP = getString(R.string.wan_IP);

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Get the IP!


        pref = getApplicationContext().getSharedPreferences(prefs, 0); // 0 - for private mode
        editor = pref.edit();
        boolean loggedIn = pref.getBoolean("login", false);
        serverIP_AUTO=pref.getString("server_ip_auto","true");

        if(serverIP_AUTO.equals("true")){

            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String wifiname=wifiInfo.getSSID();
            if (("\""+localWiFi+"\"").equals(wifiname)) {
                serverIP = localIP;
            } else {
                serverIP=wanIP;
            }
            editor.putString("server_ip",serverIP);
            editor.commit();
        }else{

            serverIP = pref.getString("server_ip", localIP);
        }


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);










        vbtnLightsOn = (Button) findViewById(R.id.btnLightsOn);
        vbtnLightsOff = (Button) findViewById(R.id.btnLightsOff);
        vbtnGate2Open = (Button) findViewById(R.id.btnGate2Open);
        vbtnGate2Close = (Button) findViewById(R.id.btnGate2Close);
        vbtnTestConnection = (Button) findViewById(R.id.btnTestConnection);
        vbtnRGBRemote = (Button) findViewById(R.id.btnRGBRemote);
        vbtnDoorLock = (Button) findViewById(R.id.btnDoorLock);
        vbtnDoorUnlock = (Button) findViewById(R.id.btnDoorUnlock);
        vbtnDoorHouseLock = (Button) findViewById(R.id.btnDoorHouseLock);
        vbtnDoorHouseUnlock = (Button) findViewById(R.id.btnDoorHouseUnlock);
        vbtnBellRing = (Button) findViewById(R.id.btnBellRing);
        vbtnBrightnessRGBIncrease = (Button) findViewById(R.id.btnBrightnessRGBIncrease);
        vbtnBrightnessRGBDecrease = (Button) findViewById(R.id.btnBrightnessRGBDecrease);
        vbtnBrightnessMasterIncrease = (Button) findViewById(R.id.btnBrightnessMasterIncrease);
        vbtnBrightnessMasterDecrease = (Button) findViewById(R.id.btnBrightnessMasterDecrease);
        vbtnResetArduino = (Button) findViewById(R.id.btnResetArduino);
        vbtnStopServer = (Button) findViewById(R.id.btnStopServer);
        vbtnGate1ShortOpen = (Button) findViewById(R.id.btnGate1ShortOpen);
        vbtnPickColor = (Button) findViewById(R.id.btnPickColor);

        vskbBrightnesMaster = (SeekBar) findViewById(R.id.skbBrightnessMaster);
        vskbBrightnessRGB = (SeekBar) findViewById(R.id.skbBrightnessRGB);
        vskbSpeedRGB = (SeekBar) findViewById(R.id.skbSpeedRGB);

        spinner = (Spinner) findViewById(R.id.spinner);


        //Navigation Drawer
        mDrawerList = (ListView)findViewById(R.id.navList);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();


        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.setTitleTextColor(0xFFFFFFFF);
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorAppShade));



        if(!loggedIn) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }else{
            updateUser();
        }





        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.modes_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);




        vbtnPickColor.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                openDialog(false);


                /*
                new ChromaDialog.Builder()
                        .initialColor(startingcolor)
                        .colorMode(ColorMode.RGB) // There's also RGB, ARGB and HSV
                        .onColorSelected(new ColorSelectListener() {
                            @Override public void onColorSelected(int color) {
                                int r = Color.red(color)/2;
                                int g = Color.green(color)/2;
                                int b = Color.blue(color)/2;
                                sendCommand("rgb.update,"+r+","+g+","+b,user,serverIP);
                                vbtnPickColor.setBackgroundColor(color);
                                startingcolor=color;
                            }
                        })
                        .create()
                        .show(getSupportFragmentManager(), "ChromaDialog");*/
            }
        });



        vbtnLightsOn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sendCommand("node=light_dominik&action=on",user,serverIP);
            }
        });
        vbtnLightsOff.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sendCommand("node=light_dominik&action=off",user,serverIP);
            }
        });
        vbtnGate2Open.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sendCommand("node=gate2&action=open",user,serverIP);
            }
        });
        vbtnGate2Close.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sendCommand("node=gate2&action=closed",user,serverIP);
            }
        });
        vbtnTestConnection.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sendCommand("node=test_led&action=pulse",user,serverIP);
            }
        });
        vbtnRGBRemote.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sendCommand("node=rgboverride_dominik&action=true",user,serverIP);
            }
        });
        vbtnDoorLock.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sendCommand("node=door_dominik&action=locked",user,serverIP);
            }
        });
        vbtnDoorUnlock.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sendCommand("node=door_dominik&action=unlocked",user,serverIP);
            }
        });
        vbtnDoorHouseLock.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sendCommand("node=door_main&action=locked",user,serverIP);
            }
        });
        vbtnDoorHouseUnlock.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sendCommand("node=door_main&action=unlocked",user,serverIP);
            }
        });
        vbtnBellRing.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sendCommand("node=bell&action=pulse",user,serverIP);
            }
        });
        vbtnBrightnessRGBIncrease.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sendCommand("node=rgbbrightness_dominik&action="+(x_rgbbrightness+10),user,serverIP);
            }
        });
        vbtnBrightnessRGBDecrease.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sendCommand("node=rgbbrightness_dominik&action="+(x_rgbbrightness-10),user,serverIP);
            }
        });
        vbtnBrightnessMasterIncrease.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sendCommand("node=lightbrightness_dominik&action="+(x_masterlightbrightness+10),user,serverIP);
            }
        });
        vbtnBrightnessMasterDecrease.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sendCommand("node=lightbrightness_dominik&action="+(x_masterlightbrightness-10),user,serverIP);
            }
        });
        vbtnResetArduino.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                resetArduino();
            }
        });
        vbtnStopServer.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                stopServer();





            }
        });
        vbtnGate1ShortOpen.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sendCommand("node=gate1&action=pulse",user,serverIP);
            }
        });





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
                sendCommand("node=lightbrightness_dominik&action="+brightness1,user,serverIP);
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
                sendCommand("node=rgbbrightness_dominik&action="+brightness2,user,serverIP);
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
                sendCommand("node=rgbspeed_dominik&action="+(speed3),user,serverIP);
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        //Navigation Drawer
        addDrawerItems();
        setupDrawer();
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(position){
                    case 0:
                        mDrawerLayout.closeDrawers();
                        break;

                    case 1:
                        Intent intent = new Intent(MainActivity.this, LogsActivity.class);
                        startActivity(intent);

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


        getStatus();
        int update_frequency;
        if(serverIP.equals(localIP))
            update_frequency=200;
        else
            update_frequency=1000;
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                // run AsyncTask here.
                if(!Global.ReadLog_running)
                    getStatus();

            }
        },0, update_frequency);

        sendCommand("connect",user,serverIP);

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

    public void sendCommand(String send_text,String user, String lanwan){
        if(networkConnected()) {
            if(!Global.SendCommand_running) {
                Global.SendCommand_running=true;
                new SendCommand(this).execute(send_text, user, lanwan, "MainActivity");
            }else{
                displayToast("Already running, try later!");
            }
        }else{
            displayToast("No internet!");
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

    void openDialog(boolean supportsAlpha) {
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(MainActivity.this, startingcolor, supportsAlpha, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                MainActivity.this.startingcolor = color;
                int r = Color.red(color)/2;
                int g = Color.green(color)/2;
                int b = Color.blue(color)/2;
                sendCommand("node=rgbcolor_dominik&action="+r+":"+g+":"+b,user,serverIP);
            }

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
            }
        });
        dialog.show();
    }

    public void displayToast(String text2){
        Context context = getApplicationContext();
        CharSequence text = text2;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.menu_logout:
                updateLogin(false, "Not_important");
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
                getStatus();
                return true;

            case R.id.menu_LAN:
                serverIP = pref.getString("server_ip", localIP);
                if (serverIP.equals(localIP)) {
                    serverIP = wanIP;
                    item.setChecked(false);
                } else {
                    item.setChecked(true);
                    serverIP = localIP;
                }
                editor.putString("server_ip", serverIP);
                editor.commit();

                displayToast("New IP: " + serverIP);
                return true;

            case R.id.menu_LAN_AUTO:
                serverIP_AUTO = pref.getString("server_ip_auto", "true");
                if (serverIP_AUTO.equals("false")) {
                    item.setChecked(true);
                    serverIP_AUTO = "true";
                    editor.putString("server_ip_auto", "true");
                } else {
                    serverIP_AUTO = "false";
                    item.setChecked(false);
                    editor.putString("server_ip_auto", "false");
                }
                editor.commit();

                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.

                return super.onOptionsItemSelected(item);


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
    @Override
    public void onResume(){
        serverIP=pref.getString("server_ip",localIP);
        super.onResume();
        updateUser();
        updateValues();
    }

    public void updateUser(){
        final SharedPreferences pref = getApplicationContext().getSharedPreferences(prefs, 0); // 0 - for private mode
        user=pref.getString("username", "Unknown");
        if(user.equals("dominik")){
            vbtnLightsOff.setEnabled(true);
            vbtnLightsOn.setEnabled(true);
            vbtnGate2Open.setEnabled(true);
            vbtnGate2Close.setEnabled(true);
            vbtnTestConnection.setEnabled(true);
            vbtnRGBRemote.setEnabled(true);
            vbtnDoorLock.setEnabled(true);
            vbtnDoorUnlock.setEnabled(true);
            vbtnDoorHouseLock.setEnabled(true);
            vbtnDoorHouseUnlock.setEnabled(true);
            vbtnBellRing.setEnabled(true);
            vbtnBrightnessRGBIncrease.setEnabled(true);
            vbtnBrightnessRGBDecrease.setEnabled(true);
            vbtnBrightnessMasterIncrease.setEnabled(true);
            vbtnBrightnessMasterDecrease.setEnabled(true);
            vbtnResetArduino.setEnabled(true);
            vbtnStopServer.setEnabled(true);
            vbtnGate1ShortOpen.setEnabled(true);
            spinner.setEnabled(true);
            vbtnPickColor.setEnabled(true);
            vskbSpeedRGB.setEnabled(true);
            vskbBrightnessRGB.setEnabled(true);
            vskbBrightnesMaster.setEnabled(true);

        }else{
            vbtnGate2Open.setEnabled(true);
            vbtnGate2Close.setEnabled(true);
            vbtnLightsOff.setEnabled(false);
            vbtnLightsOn.setEnabled(false);
            vbtnTestConnection.setEnabled(false);
            vbtnRGBRemote.setEnabled(false);
            vbtnDoorLock.setEnabled(false);
            vbtnDoorUnlock.setEnabled(false);
            vbtnDoorHouseLock.setEnabled(true);
            vbtnDoorHouseUnlock.setEnabled(true);
            vbtnBellRing.setEnabled(true);
            vbtnBrightnessRGBIncrease.setEnabled(false);
            vbtnBrightnessRGBDecrease.setEnabled(false);
            vbtnBrightnessMasterIncrease.setEnabled(false);
            vbtnBrightnessMasterDecrease.setEnabled(false);
            vbtnResetArduino.setEnabled(false);
            vbtnStopServer.setEnabled(false);
            vbtnGate1ShortOpen.setEnabled(true);
            spinner.setEnabled(false);
            vbtnPickColor.setEnabled(false);
            vskbSpeedRGB.setEnabled(false);
            vskbBrightnessRGB.setEnabled(false);
            vskbBrightnesMaster.setEnabled(false);

        }

        updateValues();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        if(!first)
            sendCommand("node=rgbmode_dominik&action="+parent.getSelectedItemPosition(),user,serverIP);
        else first=false;
    }


    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void stopServer(){
        new AlertDialog.Builder(this)
                .setTitle("Warning")
                .setMessage("Are you sure you want to stop the server?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        sendCommand("node=reset&action=server",user,serverIP);
                        displayToast("SERVER STOPPED!");
                    }})
                .setNegativeButton(android.R.string.no, null).show();
    }

    public void resetArduino(){
        new AlertDialog.Builder(this)
                .setTitle("Warning")
                .setMessage("Are you sure you want to reset arduino?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        sendCommand("node=reset&action=arduino",user,serverIP);
                        displayToast("Arduino resetting...");
                    }})
                .setNegativeButton(android.R.string.no, null).show();
    }



    //Navigation Drawer
    private void addDrawerItems() {
        String[] osArray = { "Controls", "Logs", "Cameras"};
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



    public void getStatus(){
        if(networkConnected()) {
            if(!Global.ReadLog_running){
                Global.ReadLog_running=true;
                new ReadLog(this).execute("status",serverIP,user,"no_log","MainActivity");
            }
        }else{
            displayToast("No internet connection!");
        }

    }

    public void addStatusLine(String line){
        String[] command=line.split(":");
        switch(command[0]){
            case "light_dominik":
                x_lightson=(command[1].equals("on"));
                break;

            case "gate2":
                x_gate2locked=(command[1].equals("closed"));
                break;

            case "door_main":
                x_doorhouselocked=(command[1].equals("locked"));
                break;

            case "rgboverride_dominik":
                x_rgboverride=(command[1].equals("true"));
                break;

            case "rgbmode_domink":
                x_rgbmode=Integer.parseInt(command[1]);
                break;

            case "rgbspeed_dominik":
                x_rgbspeed=Integer.parseInt(command[1]);
                break;

            case "rgbbrightness_dominik":
                x_rgbbrightness=Integer.parseInt(command[1]);
                break;

            case "rgbcolor_dominik":
                x_redbrightness=Integer.parseInt(command[1]);
                x_greenbrightness=Integer.parseInt(command[2]);
                x_bluebrightness=Integer.parseInt(command[3]);
                break;


            case "lightbrightness_dominik":
                x_masterlightbrightness=Integer.parseInt(command[1]);
                break;

        }
    }

    public void updateValues(){

        vbtnGate2Close.setEnabled(!x_gate2locked);
        vbtnGate2Open.setEnabled(x_gate2locked);
        vbtnDoorHouseLock.setEnabled(!x_doorhouselocked);
        vbtnDoorHouseUnlock.setEnabled(x_doorhouselocked);

        if(user.equals("dominik")) {
            vskbBrightnesMaster.setProgress(x_masterlightbrightness);
            vskbBrightnessRGB.setProgress(x_rgbbrightness);
            vskbSpeedRGB.setProgress(x_rgbspeed);
            vbtnRGBRemote.setEnabled(x_rgboverride);
            vbtnLightsOff.setEnabled(x_lightson);
            vbtnLightsOn.setEnabled(!x_lightson);
            int rgb_temp=x_rgbmode;

            switch(rgb_temp){
                case 0:
                    x_rgbmode=1;
                    break;
                case 1:
                    x_rgbmode=2;
                    break;
                case 2:
                    x_rgbmode=3;
                    break;
                case 10:
                    x_rgbmode=0;
                    break;
                case 11:
                    x_rgbmode=4;
                    break;
                case 12:
                    x_rgbmode=5;
                    break;
            }

            if(firstrgb<1){
                spinner.setSelection(x_rgbmode);
                firstrgb++;
            }

            startingcolor=Color.rgb(x_redbrightness*2,x_greenbrightness*2,x_bluebrightness*2);
            vbtnPickColor.setBackgroundColor(startingcolor);
        }
    }



}