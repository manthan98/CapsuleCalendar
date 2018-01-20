package com.example.manthan.capsulecalendar;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class confirmation extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        String text = getIntent().getStringExtra("MedicationText");

        TextView medicationText= (TextView) findViewById(R.id.medicationText);
        medicationText.setText(text);
    }

}
