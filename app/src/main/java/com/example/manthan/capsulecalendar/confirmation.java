package com.example.manthan.capsulecalendar;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Arrays;

public class confirmation extends AppCompatActivity {

    private static final String TAG = "confirmationActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        String text = getIntent().getStringExtra("MedicationText");
        String formattedText = text.trim().replaceAll("\\s{2,}", " ");
        Log.d(TAG, formattedText);
        String[] words = formattedText.split("\\s+");

        int index = Arrays.asList(words).indexOf("TAKE");
        String perDosage = "Could not find number of tablets per dose";
        if(index != -1){
            perDosage= words[index + 1];
        }

        String[] numbers = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "ONE", "TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN", "EIGHT", "NINE", "TEN"};

        //if perday format includes word daily
        String per_day = "Could not find number of doses per day";
        int indexDaily = Arrays.asList(words).indexOf("DAILY");
        int indexDay = Arrays.asList(words).indexOf("DAY");
        if (indexDaily != -1){
            if(words[indexDaily - 1] == "TIMES"){
                per_day = words[indexDaily - 2];
            }
            else if(Arrays.asList(numbers).indexOf(words[indexDaily - 1]) != -1){
                per_day = words[indexDaily - 1];
            }
            else{
                per_day = "ONE";
            }
        }
        else if (indexDay != -1){
            if(words[indexDay - 1] == "A" || words[indexDaily - 1] == "PER"){
                per_day = words[indexDay - 2];
            }
            else if(words[indexDay - 1] == "EACH" || words[indexDaily - 1] == "EVERY"){
                per_day = "ONE";
            }
        }


        TextView medicationText= (TextView) findViewById(R.id.medicationText);
        medicationText.setText(formattedText);

        EditText perDose = (EditText) findViewById(R.id.perDose);
        perDose.setText(perDosage, TextView.BufferType.EDITABLE);

        EditText perDay = (EditText) findViewById(R.id.perDay);
        perDay.setText(per_day, TextView.BufferType.EDITABLE);


    }

}
