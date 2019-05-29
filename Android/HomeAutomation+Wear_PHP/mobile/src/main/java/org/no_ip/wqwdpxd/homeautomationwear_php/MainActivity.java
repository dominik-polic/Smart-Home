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
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
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
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 32501;

    //Navigation Drawer
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;

    private Handler mHandler;
    public String useWeb="Unknown";
    public String user="Unknown";
    public boolean first=true;
    private Switch vswHouseDoor;
    private Switch vswGate2;
    private Switch vswLight1;
    private Switch vswLight2;
    private Button vbtnGate1;
    private SeekBar vsbLightD;
    SharedPreferences pref;
    SharedPreferences.Editor editor;




    String prefs;
    static boolean vswLight1State;
    static boolean vswLight2State;
    static boolean vswGate2State;
    static boolean vswHouseDoorState;
    static int vsbLightDState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        prefs = getResources().getString(R.string.prefs);

        //Get the IP!


        pref = getApplicationContext().getSharedPreferences(prefs, 0); // 0 - for private mode
        editor = pref.edit();
        boolean loggedIn = pref.getBoolean("login", false);
        useWeb=pref.getString("use_web","false");






        vswHouseDoor = findViewById(R.id.swHouseDoor);
        vswGate2=findViewById(R.id.swGate2);
        vswLight1=findViewById(R.id.swLivingRoom1);
        vswLight2=findViewById(R.id.swLivingRoom2);
        vbtnGate1=findViewById(R.id.btnGate1ShortOpen);
        vsbLightD=findViewById(R.id.sbDominik);

        //Navigation Drawer
        mDrawerList = findViewById(R.id.navList);
        mDrawerLayout = findViewById(R.id.drawer_layout);


        Toolbar myToolbar = findViewById(R.id.my_toolbar);
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




        vswHouseDoor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                sendCommand("node=door_house&action="+(isChecked?"unlock":"lock"),user);
                vswHouseDoorState=isChecked;
            }
        });

        vswGate2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                sendCommand("node=gate2&action="+(isChecked?"open":"close"),user);
                vswGate2State=isChecked;
            }
        });

        vswLight1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                sendCommand("node=light_livingroom_1&action="+(isChecked?"on":"off"),user);
                vswLight1State=isChecked;
            }
        });

        vswLight2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                sendCommand("node=light_livingroom_2&action="+(isChecked?"on":"off"),user);
                vswLight2State=isChecked;
            }
        });


        vbtnGate1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sendCommand("node=gate1&action=open",user);
            }
        });





        vsbLightD.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {


            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sendCommand("node=light_dominik&action="+vsbLightD.getProgress(),user);
            }
        });


        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

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




        sendCommand("connect",user);
        mHandler = new Handler();
        startRepeatingTask();

        if(useWeb.equals("true")){
            Intent intent = new Intent(MainActivity.this, LogsActivity.class);
            startActivity(intent);
        }
    }

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                getStatus();
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHandler.postDelayed(mStatusChecker, 500);
            }
        }
    };

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

    public void sendCommand(String send_text,String user){
        if(networkConnected()) {
            if(!Global.SendCommand_running) {
                Global.SendCommand_running=true;
                Log.d("DEBUG-DOMI",send_text);
                new SendCommand(this).execute(send_text, user, "MainActivity");
            }
        }else{
            displayToast("No network");
        }

    }


    public boolean networkConnected() {

        ConnectivityManager cm =
                (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = null;
        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
        }


        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }



    public void displayToast(String text2){
        try {
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text2, duration);
            toast.show();
        }catch (Exception e){
            Log.e("TOAST",e.getMessage());
        }
    }

    public void updateLogin(){

        SharedPreferences pref = getSharedPreferences(prefs, 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("login", false);
        editor.putString("username", "Not_important");
        editor.apply();

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.menu_logout:
                updateLogin();
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



            case R.id.menu_WEB_DEFAULT:
                useWeb = pref.getString("use_web", "false");
                if (useWeb.equals("true")) {
                    useWeb="false";
                    item.setChecked(false);
                } else {
                    item.setChecked(true);
                    useWeb="true";
                }
                editor.putString("use_web", useWeb);
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
        MenuItem checkbox_WEB = menu.findItem(R.id.menu_WEB_DEFAULT);
        if(useWeb.equals("true"))
            checkbox_WEB.setChecked(true);
        else
            checkbox_WEB.setChecked(false);



        return true;
    }
    @Override
    public void onResume(){
        super.onResume();
        updateUser();
    }

    public void updateUser(){
        final SharedPreferences pref = getApplicationContext().getSharedPreferences(prefs, 0); // 0 - for private mode
        user=pref.getString("username", "Unknown");
        if(user.equals("dominik")){
            vswGate2.setEnabled(true);
            vswHouseDoor.setEnabled(true);
            vswLight2.setEnabled(true);
            vswLight1.setEnabled(true);
            vbtnGate1.setEnabled(true);
            vsbLightD.setEnabled(true);
        }else{
            vswGate2.setEnabled(true);
            vswHouseDoor.setEnabled(true);
            vswLight2.setEnabled(true);
            vswLight1.setEnabled(true);
            vbtnGate1.setEnabled(true);
            vsbLightD.setEnabled(false);

        }

    }





    //Navigation Drawer
    private void addDrawerItems() {
        String[] osArray = { "Controls", "Web Interface", "Cameras"};
        ArrayAdapter<String> mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, osArray);
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
        mDrawerLayout.addDrawerListener(mDrawerToggle);

    }



    public void getStatus(){
        updateValues();
        if(networkConnected()) {
            if(!Global.ReadLog_running){
                Global.ReadLog_running=true;
                new ReadLog(this).execute("status",user,"no_log","MainActivity");
            }
        }else{
            displayToast("No network");
        }


    }
    public void updateValues(){
        vswLight1.setChecked(vswLight1State);
        vswLight2.setChecked(vswLight2State);
        vswGate2.setChecked(vswGate2State);
        vswHouseDoor.setChecked(vswHouseDoorState);
        vsbLightD.setProgress(vsbLightDState);

    }




    public void updatePermissionNetwork() {
        Log.d("WIFI","updateNetworkPermission beginning");
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            Log.d("WIFI","Version is above 23");
            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                Log.d("WIFI","Permission is denied :'(");
                // Should we show an explanation?

                /*if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {*/

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                //}
            }else{
                Log.d("WIFI","Permission is already granted?");
            }


        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                    Log.d("WIFI","Wifi permission granted");

                } else {

                    Log.d("WIFI","Wifi permission denied");

                    editor.commit();
                }

            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

}