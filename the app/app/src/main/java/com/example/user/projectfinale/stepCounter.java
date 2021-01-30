package com.example.user.projectfinale;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class stepCounter extends AppCompatActivity implements SensorEventListener {
    //declaring all variables used in class with a description for each

    TextView textRealSteps; //will be set to the number of steps taken since activity has first been run
    TextView startWalking; //will remind user to start walking in order to view steps taken
    TextView textGoal; //will display the daily goal number of steps
    EditText enteredGoal; //edittext which the user can enter their goal number of steps in
    SensorManager sensorManager; //a sensorManager object which will be used to detect steps
    int counter; //this counter is used to determine whether the application has been launched before
    boolean running = false; //this boolean is used to start and stop the step detecting
    userDBhandler dbHandler; //handles database requests
    SharedPreferences pref; //creating shared preference object to store counter, goal number of steps, and the steps taken since launched
    SharedPreferences.Editor editor; //an editor for the shared preference object
    int initialSteps = 0; //stores the previously recorded steps in step detector
    int currentSteps = 0; //stores the current number of steps recorded in step recorder
    int realSteps = 0; //stores the number of steps taken since the activity is launched the fist time
    int stepGoal = 0; //stores the goal number of steps

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_counter);

        //assigning view elements to variables
        textRealSteps = (TextView) findViewById(R.id.tvSteps);
        startWalking = (TextView) findViewById(R.id.textView);
        textGoal = (TextView) findViewById(R.id.textGoal);
        enteredGoal = (EditText) findViewById(R.id.editGoal);

        //sets up sensor manager object
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //initialising the database handler variable to work with userDBhandler class
        dbHandler = new userDBhandler(this, null, null, 1);

        //initialise shared preference object and editor
        pref = getApplicationContext().getSharedPreferences("activityLoaded", MODE_PRIVATE);
        editor = pref.edit();
        counter = pref.getInt("counter", 0); //if counter is equal to 0, then activity has not been loaded before
        stepGoal = pref.getInt("goalSteps", 5000); //default goal is 5000 if one hasn't been set


        //if stepGoal is a valid number then set it
        if(stepGoal>0){
            textGoal.setText("Step Goal: "+stepGoal);
        }
        //if the activity has been run before then get initialSteps value from database
        if (counter == 1){
            String[] dbString = dbHandler.databaseToStringSteps();

            //this if statement escapes a strange error i was getting after "delete data" button was pressed
            if(dbString[0] == ""){
                counter = 0;
                Toast.makeText(this,"the steps will reset until you enter your details!", Toast.LENGTH_LONG).show();
                return;
            }
            initialSteps = Math.round(Float.parseFloat(dbString[0]));
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
    }
    @Override
    public void onResume(){
        super.onResume();
        running = true;
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (countSensor != null){
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
        }else{
            Toast.makeText(this,"Sensor not found!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        //variable is saved in shared preferences on pause and used to detect calories burned
        editor.putInt("realSteps", realSteps);
        editor.commit();

        //used for debugging purposes
        /*running = false;
        if you unregister the hardware will stop detecting steps
        sensorManager.unregisterListener(this);*/
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //function triggers everytime a step has been taken
        //if it is the first time this activity has been loaded, then save the initial number of steps
        if (counter == 0) {
            saveSteps(event);
        }

        if (running){
            //if sensor running then calculate real steps from initial steps and current steps
            startWalking.setVisibility(TextView.INVISIBLE);
            currentSteps = Math.round(event.values[0]);
            realSteps = currentSteps - initialSteps;
            //display real steps
            textRealSteps.setText(String.valueOf(realSteps));
        }

        //if step goal is reached then trigger notification
        if (stepGoal == realSteps){
            launchNotification();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void saveSteps(SensorEvent event){

        //store initial steps in database
        userDetails addSteps = new userDetails(String.valueOf(event.values[0]));
        dbHandler.addSteps(addSteps);
        initialSteps = Math.round(event.values[0]);

        //set counter to 1 and store in shared preferences so this code isn't run again
        counter=1;
        editor.putInt("counter", 1);
        editor.commit();

    }
    public void saveGoal(View view){
        //if edittext is empty then return
        if (enteredGoal.getText().toString().isEmpty()){
            return;
        }

        //store goal steps in shared preferences and update on screen
        stepGoal = Integer.parseInt(enteredGoal.getText().toString());
        textGoal.setText("Set Goal: "+stepGoal);
        editor.putInt("goalSteps", stepGoal);
        editor.commit();
    }
    public void launchNotification(){
        //sets up and launches the notification when step goal is reached
        Intent intent = new Intent(this, stepCounter.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        int notification1Id = 56748;
        NotificationCompat.Builder builder1 = new NotificationCompat.Builder(this, "steps");
        builder1.setSmallIcon(R.drawable.ic_ff); //customised notification icon
        builder1.setContentTitle("Congratulations!"); //notification title
        builder1.setContentText("you have reached your goal number of steps."); //notification body text
        builder1.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 }); //set vibration
        builder1.setContentIntent(pendingIntent);
        builder1.setAutoCancel(true);
        builder1.setPriority(NotificationCompat.PRIORITY_LOW);
        initChannels(getApplicationContext()); //initialising channel if api is > 26
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notification1Id, builder1.build());
    }
    public void initChannels(Context context) {
        //method is only used for notification if api is above 26
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("steps", "Channel steps", NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("for steps");
        notificationManager.createNotificationChannel(channel);
    }
}
