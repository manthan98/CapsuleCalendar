package com.example.manthan.capsulecalendar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class homeScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
    }

    public void cameraClick(View v){
        Intent i = new Intent(this, OcrCaptureActivity.class);
        startActivity(i);
    }

}
