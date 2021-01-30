package com.example.user.projectfinale;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    //declaring all variables used in class with a description for each

    SharedPreferences pref; //initialising a sharedpreference object for activityLoaded
    SharedPreferences.Editor editor; //initialising editor for shared preferences
    boolean firstRun; //holds sharedpreference value determining if its the first time the application is run
    Calendar calendar = Calendar.getInstance(); //sets a calendar variable
    int resetHour = 23; //these variables hold the times the alarm will be triggered
    int resetMinute = 59;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //shared preferences are used to check if this is the first time the code has been run
        //if it is the first time then load the userInfo class for the user to enter details
        pref = getApplicationContext().getSharedPreferences("activityLoaded", MODE_PRIVATE);
        firstRun = pref.getBoolean("firstRun", false);
        if (firstRun==false){
            editor = pref.edit();
            editor.putBoolean("firstRun", true);
            editor.commit();
            Intent i = new Intent(this, userInfo.class);
            startActivity(i);
        }

        //setting up an alarm that triggers at the end of the day, this resets steps taken and calories consumed
        calendar.set(Calendar.MINUTE, resetMinute);
        calendar.set(Calendar.HOUR_OF_DAY, resetHour);
        Intent intent = new Intent(getApplicationContext(), resetDay.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 101, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    public void userInfo(View view){
        //runs userInfo activity
        Intent i = new Intent(this, userInfo.class);
        startActivity(i);
    }
    public void caloriesScreen(View view){
        //runs Calories activity
        Intent i = new Intent(this, Calories.class);
        startActivity(i);
    }
    public void SettingsScreen(View view){
        //runs Settings activity
        Intent i = new Intent(this, Settings.class);
        startActivity(i);
    }
    public void StepsScreen(View view){
        //runs stepCounter activity
        Intent i = new Intent(this, stepCounter.class);
        startActivity(i);
    }
}
