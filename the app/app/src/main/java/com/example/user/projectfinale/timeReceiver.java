package com.example.user.projectfinale;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

/**
 * Created by User on 16/04/2018.
 */

public class timeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //this notification is triggered when an alarm set in the Calories class is triggered
        NotificationManager notificationManager =(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent repeating_intent = new Intent(context, Calories.class);
        repeating_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,100,repeating_intent,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "calories");
        builder.setContentIntent(pendingIntent);
        builder.setSmallIcon(R.drawable.ic_ff); //set icon to custome design
        builder.setContentTitle("Calorie Reminder!"); //notification title
        builder.setContentText("It has been a while since you have updated your calories."); //notification content
        builder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 }); //set vibration
        builder.setAutoCancel(true);
        initChannels(context); //initialising channel if api is > 26
        notificationManager.notify(100,builder.build());

    }
    public void initChannels(Context context) {
        //method is only used for notification if api is above 26
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("calories", "Channel calories", NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("for calories");
        notificationManager.createNotificationChannel(channel);
    }





}
