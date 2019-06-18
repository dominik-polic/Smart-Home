package org.no_ip.wqwdpxd.homeautomationwear_php;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.InputStream;
import java.net.URL;

public class ImageActivity extends AppCompatActivity {


    private ImageView imageView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        //Remove notification
        NotificationManager notificationManager = (NotificationManager)  getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(0);


        imageView = findViewById(R.id.imageView);
        Button btnClose = findViewById(R.id.btnClose);
        Button btnSmallGate = findViewById(R.id.btnSmallGate);
        Button btnCarGate = findViewById(R.id.btnCarGate);


        btnCarGate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ActionSender.sendCommandFb("gate_2","open","notification",ImageActivity.this,"ANDROID-BELL");
            }
        });


        btnSmallGate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ActionSender.sendCommandFb("gate_1","pulse","notification",ImageActivity.this,"ANDROID-BELL");
            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });

        new DownloadImageTask((ImageView) findViewById(R.id.imageView)).execute("http://polichousecontrol.ddns.net:8001/snap.jpg");
    }




    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

}
