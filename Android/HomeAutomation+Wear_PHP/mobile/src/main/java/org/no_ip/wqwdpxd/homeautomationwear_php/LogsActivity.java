package org.no_ip.wqwdpxd.homeautomationwear_php;

import android.annotation.SuppressLint;
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
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;

public class LogsActivity extends AppCompatActivity {

    String prefs;
    String localIP;
    String wanIP;


    SharedPreferences pref;
    SharedPreferences.Editor editor;
    private String user="Unknown";
    private String useWeb="Unknown";
    private String enableNotify="Unknown";
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private WebView webView;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs);

        webView= (WebView) findViewById(R.id.webview);

        prefs = getResources().getString(R.string.prefs);

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
        useWeb=pref.getString("use_web","false");
        enableNotify = pref.getString("enable_notify","true");

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.setTitleTextColor(0xFFFFFFFF);
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorAppLogsShade));

        mDrawerList = findViewById(R.id.navList2);
        mDrawerLayout = findViewById(R.id.drawer_layout);

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




        if(getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.loadUrl("http://polichousecontrol.ddns.net:1880/ui");
        //webView.loadUrl("https://google.com");

        if(enableNotify.equals("true")){
            updateNotificationSubscription(true);
        }else{
            updateNotificationSubscription(false);
        }
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




    public void displayToast(String text2){
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text2, duration);
        toast.show();
    }

    @Override
    public void onBackPressed() {
        if(webView.canGoBack())
            webView.goBack();

    }

    private void addDrawerItems() {
        String[] osArray = { "Controls","Web Interface","Cameras" };
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

    private void updateNotificationSubscription(boolean active){
        if(active)
            FirebaseMessaging.getInstance().subscribeToTopic("bell");
        else
            FirebaseMessaging.getInstance().unsubscribeFromTopic("bell");
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


            case android.R.id.home:
                    finish();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.

                return super.onOptionsItemSelected(item);



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



}