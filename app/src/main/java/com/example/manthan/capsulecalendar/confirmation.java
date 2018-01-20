package com.example.manthan.capsulecalendar;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;

public class confirmation extends AppCompatActivity {

    private static final String TAG = "confirmationActivity";
    private  static Integer PER_DOSAGE = 0;
    private  static  Integer DOSES_PER_DAY = 0;
    private  static String DAYS_REPEAT = "0";
    private  static String NUM_PILLS = "0";

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
        if (index != -1) {
            perDosage = words[index + 1];
        }

        String[] numbers = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "ONE", "TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN", "EIGHT", "NINE", "TEN"};

        //if perday format includes word daily
        String per_day = "Could not find number of doses per day";
        int indexDaily = Arrays.asList(words).indexOf("DAILY");
        int indexDay = Arrays.asList(words).indexOf("DAY");
        if (indexDaily != -1) {
            if (words[indexDaily - 1].equals("TIMES")) {
                per_day = words[indexDaily - 2];
            } else if (Arrays.asList(numbers).indexOf(words[indexDaily - 1]) != -1) {
                per_day = words[indexDaily - 1];
            } else {
                per_day = "ONE";
            }
        } else if (indexDay != -1) {
            if (words[indexDay - 1].equals( "A" )|| words[indexDaily - 1].equals("PER") ) {
                per_day = words[indexDay - 2];
            } else if (words[indexDay - 1].equals("EACH") || words[indexDaily - 1].equals("EVERY") ) {
                per_day = "ONE";
            }
        }

    /*
        String refills = "Could not find number of doses per day";
        int indexNO = Arrays.asList(words).indexOf("NO");
        int indexRefill = Arrays.asList(words).indexOf("REFILL");
        int indexRefill2 = Arrays.asList(words).indexOf("REFILL:");
        if(indexNO != -1){
            if(words[indexNO + 1]=="REFILL"){
                refills = "NONE";
            }
        }
        else if(indexRefill2 != -1||indexRefill != -1){
            refills = words[index +1];
        }
*/
        TextView medicationText = (TextView) findViewById(R.id.medicationText);
        medicationText.setText(formattedText);

        EditText perDose = (EditText) findViewById(R.id.perDose);
        perDose.setText(perDosage, TextView.BufferType.EDITABLE);

        EditText perDay = (EditText) findViewById(R.id.perDay);
        perDay.setText(per_day, TextView.BufferType.EDITABLE);

        if(perDosage.equals("ONE")||perDosage.equals("1")){
            PER_DOSAGE = 1;
        }
        else if(perDosage.equals("TWO") || perDosage.equals("2")){
            PER_DOSAGE = 2;
        }
        else if(perDosage.equals("THREE") || perDosage.equals("3")){
            PER_DOSAGE = 3;

        }

        DOSES_PER_DAY = 1;
        if(per_day.equals("TWO") || per_day.equals("TWICE")){
            DOSES_PER_DAY = 2;
        }
        else if(per_day.equals("THREE") || per_day.equals("THRICE")){
            DOSES_PER_DAY = 3;
        }

        NUM_PILLS = perDosage;

        int count = 30/(DOSES_PER_DAY*(PER_DOSAGE));
        DAYS_REPEAT = String.valueOf(count);
    }

    public void syncCalendarClick(View v) {



        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Need write calendar permission", Toast.LENGTH_SHORT).show();
            return;
        }


        for(int i = 0; i<DOSES_PER_DAY; i++){
            if(i==0){
                Calendar beginTime = Calendar.getInstance();
                beginTime.set(Calendar.HOUR_OF_DAY, 9);
                beginTime.set(Calendar.MINUTE, 0);

                Long beginMilli = beginTime.getTimeInMillis();
                Long endTime = beginMilli + 600000;

                Intent intent = new Intent(Intent.ACTION_INSERT)
                        .setData(Events.CONTENT_URI)
                        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime)
                        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime)
                        .putExtra(Events.TITLE, "Medications reminder")
                        .putExtra(Events.RRULE,String.format("FREQ=DAILY;COUNT=%s; WKST=SU", DAYS_REPEAT))
                        .putExtra(Events.DESCRIPTION, String.format("Take %s tablets.", NUM_PILLS))
                        .putExtra(Events.EVENT_TIMEZONE, TimeZone.getDefault().getDisplayName());
                startActivity(intent);
            }
            else if(i==1){
                Calendar beginTime = Calendar.getInstance();
                beginTime.set(Calendar.HOUR_OF_DAY, 12);
                beginTime.set(Calendar.MINUTE, 0);

                Long beginMilli = beginTime.getTimeInMillis();
                Long endTime = beginMilli + 600000;

                Intent intent = new Intent(Intent.ACTION_INSERT)
                        .setData(Events.CONTENT_URI)
                        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime)
                        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime)
                        .putExtra(Events.TITLE, "Medications reminder")
                        .putExtra(Events.RRULE,String.format("FREQ=DAILY;COUNT=%s; WKST=SU", DAYS_REPEAT))
                        .putExtra(Events.DESCRIPTION, String.format("Take %s tablets.", PER_DOSAGE))
                        .putExtra(Events.EVENT_TIMEZONE, TimeZone.getDefault().getDisplayName());
                startActivity(intent);
            }
            else if(i==2){
                Calendar beginTime = Calendar.getInstance();
                beginTime.set(Calendar.HOUR_OF_DAY, 19);
                beginTime.set(Calendar.MINUTE, 0);

                Long beginMilli = beginTime.getTimeInMillis();
                Long endTime = beginMilli + 600000;

                Intent intent = new Intent(Intent.ACTION_INSERT)
                        .setData(Events.CONTENT_URI)
                        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime)
                        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime)
                        .putExtra(Events.TITLE, "Medications reminder")
                        .putExtra(Events.RRULE,String.format("FREQ=DAILY;COUNT=%s; WKST=SU", DAYS_REPEAT))
                        .putExtra(Events.DESCRIPTION, String.format("Take %s tablets.", PER_DOSAGE))
                        .putExtra(Events.EVENT_TIMEZONE, TimeZone.getDefault().getDisplayName());
                startActivity(intent);
            }

        }

    }

}
