package com.example.manthan.capsulecalendar;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;

import java.util.Arrays;

public class confirmation extends AppCompatActivity {

    private static final String TAG = "confirmationActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        String text = getIntent().getStringExtra("MedicationText");
        String credential = getIntent().getStringExtra("credential");
        Log.d("HEYYYYY", credential);
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


        //creating calendar event with data
        Event event = new Event()
                .setSummary("MEDICINE NAME")
                .setLocation("")
                .setDescription("Reminder to take your medicine.");

        DateTime startDateTime = new DateTime("2018-01-20T09:00:00-07:00");
        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone("Canada/Toronto");
        event.setStart(start);

        DateTime endDateTime = new DateTime("2018-01-20T17:00:00-07:00");
        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone("Canada/Toronto");
        event.setEnd(end);

        String[] recurrence = new String[] {"RRULE:FREQ=DAILY;COUNT=2"};

        event.setRecurrence(Arrays.asList(recurrence));
        EventReminder[] reminderOverrides = new EventReminder[] {
                new EventReminder().setMethod("popup").setMinutes(10),
        };
        Event.Reminders reminders = new Event.Reminders()
                .setUseDefault(false)
                .setOverrides(Arrays.asList(reminderOverrides));
        event.setReminders(reminders);

        String calendarId = "primary";
        //event = service.events().insert(calendarId, event).execute();


    }

}
