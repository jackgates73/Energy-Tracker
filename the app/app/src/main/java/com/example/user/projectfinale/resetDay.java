package com.example.user.projectfinale;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by User on 26/04/2018.
 */

public class resetDay extends BroadcastReceiver {

    SharedPreferences prefSteps; //the sharedpreference object holding the counter value
    SharedPreferences.Editor editor;
    String caloriesLeft = "0.0"; //set the caloriesLeft equal to 0.0

    @Override
    public void onReceive(Context context, Intent intent) {
        //reset the counter in stepCounter so that the next time it is opened it will start over
        //setting counter to 0 means that it will resave the initial steps in the database when stepCounter activity is launched
        prefSteps = context.getSharedPreferences("activityLoaded", MODE_PRIVATE);
        editor = prefSteps.edit();
        editor.putInt("counter", 0);
        editor.commit();
        Toast.makeText(context, "day reset", Toast.LENGTH_LONG).show();

        //reset steps used to calculate how many calories are burned
        editor.putInt("realSteps", 0);
        editor.commit();

        //reset the number of calories left in caloric_file to 0.0,
        //next time Calories activity is launched the value will be set to the same as caloricGoal
        try {
            FileOutputStream fileOutputStream = context.openFileOutput("caloric_file", MODE_PRIVATE);
            fileOutputStream.write(caloriesLeft.getBytes());
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}