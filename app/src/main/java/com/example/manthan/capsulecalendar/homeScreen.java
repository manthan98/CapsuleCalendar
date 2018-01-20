package com.example.manthan.capsulecalendar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Date;

public class homeScreen extends AppCompatActivity {

    String JWToken = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        JWToken = getIntent().getStringExtra("credential");
    }

    public void cameraClick(View v){
        Intent i = new Intent(this, OcrCaptureActivity.class);
        i.putExtra("credential", JWToken);
        startActivity(i);
    }

    public void calendarClick(View v){
        Calendar cal = new GregorianCalendar();
        cal.setTime(new Date());
        long time = cal.getTime().getTime();
        Uri.Builder builder =
                CalendarContract.CONTENT_URI.buildUpon();
        builder.appendPath("time");
        builder.appendPath(Long.toString(time));
        Intent intent =
                new Intent(Intent.ACTION_VIEW, builder.build());
        startActivity(intent);

    }

}
