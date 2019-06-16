package org.no_ip.wqwdpxd.homeautomationwear_php;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
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
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import android.text.format.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Date;


public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 32501;

    //Navigation Drawer
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;

    private Handler mHandler;
    public String useWeb="Unknown";
    public String enableNotify="Unknown";
    public String user="Unknown";
    private Switch vswHouseDoor;
    private Switch vswGate2;
    private Switch vswLight1;
    private Switch vswLight2;
    private Button vbtnGate1;
    private SeekBar vsbLightD;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    private FirebaseDatabase myDb;
    private DatabaseReference dbRef_nodered;
    private DatabaseReference dbRef_remote;
    private DatabaseReference dbRef_logs;

    String prefs;



    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Connect to firebase
        myDb = FirebaseDatabase.getInstance();
        dbRef_nodered = myDb.getReference("nodered");
        dbRef_remote = myDb.getReference("remote");
        dbRef_logs = myDb.getReference("logs");
        dbRef_nodered.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                if (!networkConnected()) {
                    displayToast("No network");
                }
                try {

                    if(dataSnapshot.getKey().equals("nodered")) {
                        Log.d("DEBUG","Processing key: "+dataSnapshot.getKey());

                        String light_livingroom_1_fb = dataSnapshot.child("light_livingroom_1").getValue().toString();
                        //Log.d("DEBUG-TAG","Debug 1");
                        String light_livingroom_2_fb = dataSnapshot.child("light_livingroom_2").getValue().toString();
                        //Log.d("DEBUG-TAG","Debug 2");
                        String light_dominik_fb = dataSnapshot.child("light_dominik").getValue().toString();
                        //Log.d("DEBUG-TAG","Debug 3");
                        String gate_2_fb = dataSnapshot.child("gate_2").getValue().toString();
                        //Log.d("DEBUG-TAG","Debug 4");
                        String door_house_fb = dataSnapshot.child("door_house").getValue().toString();
                        //Log.d("DEBUG-TAG","Debug 5");

                        vswLight1.setChecked(light_livingroom_1_fb.equals("on"));
                        vswLight2.setChecked(light_livingroom_2_fb.equals("on"));
                        vswGate2.setChecked(gate_2_fb.equals("close"));
                        vswHouseDoor.setChecked(door_house_fb.equals("lock"));
                        vsbLightD.setProgress(Integer.parseInt(light_dominik_fb));
                    }

                } catch (Exception e) {
                    displayToast("ERROR: Firebase DB malfunction.");
                    Log.e("FIREBASE", "NullPointerException, firebase format is wrong: " + e);
                }


            }


            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("FIREBASE", "Failed to read value.", error.toException());
            }
        });



        prefs = getResources().getString(R.string.prefs);

        //Get the IP!


        pref = getApplicationContext().getSharedPreferences(prefs, 0); // 0 - for private mode
        editor = pref.edit();
        boolean temp_logout_once = pref.getBoolean("TEMP_LOGOUT_UPDATE_2019_6_15_a",false);
        boolean loggedIn = pref.getBoolean("login", false);
        if (!temp_logout_once){
            loggedIn = false;
            editor.putBoolean("login",false);
            editor.putBoolean("TEMP_LOGOUT_UPDATE_2019_6_15_a",true);
            editor.apply();
        }



        useWeb=pref.getString("use_web","false");
        enableNotify = pref.getString("enable_notify","true");





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
                sendCommandFb("door_house",(isChecked?"lock":"unlock"),user);

            }
        });

        vswGate2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                sendCommandFb("gate_2",(isChecked?"close":"open"),user);
            }
        });

        vswLight1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                sendCommandFb("light_livingroom_1",(isChecked?"on":"off"),user);
            }
        });

        vswLight2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                sendCommandFb("light_livingroom_2",(isChecked?"on":"off"),user);
            }
        });


        vbtnGate1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sendCommandFb("gate_1","open",user);
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
                sendCommandFb("light_dominik",vsbLightD.getProgress(),user);
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
                        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.LView");
                        if (launchIntent != null) {
                            startActivity(launchIntent);
                        }else{
                            mDrawerLayout.closeDrawers();
                            Intent intentLogs = new Intent(Intent.ACTION_VIEW);
                            intentLogs.setData(Uri.parse("market://details?id=com.LView"));
                            startActivity(intentLogs);
                        }

                        break;


                }

            }
        });









        mHandler = new Handler();


        if(enableNotify.equals("true")){
            updateNotificationSubscription(true);
        }else{
            updateNotificationSubscription(false);
        }

        if(useWeb.equals("true")){
            Intent intent = new Intent(MainActivity.this, LogsActivity.class);
            startActivity(intent);
        }
    }


    private void updateNotificationSubscription(boolean active){
        if(active)
            FirebaseMessaging.getInstance().subscribeToTopic("bell");
        else
            FirebaseMessaging.getInstance().unsubscribeFromTopic("bell");
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

    public void sendCommand(String send_text,String user){
        /*
        DatabaseReference myRef = myDb.getReference("test1");
        myRef.setValue(send_text);
        if(networkConnected()) {
            if(!Global.SendCommand_running) {
                Global.SendCommand_running=true;
                //Log.d("DEBUG-DOMI",send_text);
                new SendCommand(this).execute(send_text, user, "MainActivity");
            }
        }else{
            displayToast("No network");
        }
        */
        displayToast("This function is deprecated, you should not be seeing this!");
    }

    private void sendCommandFb(String node, Object action, String user){

        try {
            action = action.toString();
        }catch (NullPointerException e){
            Log.e("FIREBASE","Error converting \"action\"Object to String");
        }



        if(!networkConnected())
            displayToast("No network!");

        //Send execution command
        dbRef_remote.child(node).setValue(action);

        //Add log
        LogEntry logEntry = new LogEntry(action, node, user);
        dbRef_logs.child(DateFormat.format("yyyy-MM-dd", new java.util.Date()).toString())
                .child(DateFormat.format("HH:mm:ss", new java.util.Date()).toString()).setValue(logEntry);
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

            case R.id.menu_NOTIFY:
                enableNotify= pref.getString("enable_notify", "true");
                if (enableNotify.equals("true")) {
                    enableNotify="false";
                    item.setChecked(false);
                    updateNotificationSubscription(false);
                } else {
                    item.setChecked(true);
                    enableNotify="true";
                    updateNotificationSubscription(true);
                }
                editor.putString("enable_notify", enableNotify);
                editor.commit();
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
        MenuItem checkbox_NOTIFY = menu.findItem(R.id.menu_NOTIFY);

        if(enableNotify.equals("true")){
            checkbox_NOTIFY.setChecked(true);
        }else{
            checkbox_NOTIFY.setChecked(false);
        }

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






    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
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