package com.example.user.projectfinale;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;

public class Calories extends AppCompatActivity {

    //declaring all variables used in class with a description for each

    String r1value; //radio button value for gender
    String r2value; //radio button value for activity level
    String spinnerValue; //spinner value holds fitness goal
    String heightValue; //holds value of height
    String weightValue; //holds value of weight
    String ageValue; //holds value of age
    String caloricGoal; //holds calculated number of calories a person should eat
    String caloriesLeft; //holds the number of calories a person has left to eat
    userDBhandler dbHandler; //handles database requests
    float height; //height value in float
    float weight; //weight value in float
    float age; //age value in float
    float BMR; //basal metabolic rate
    float MR; //holds the number of calories a person has left to eat
    TextView textGoal; //textview holding the caloric goal
    TextView textLeft; //textview holding the calories left
    TextView textConsumed; //textview holding the calories consumed
    TextView textBurned; //textview holding the calories burned from walking
    EditText calories; //user enters the number of calories they consume in this edittext
    Button addCalories; //button adds the number entered in the edittext, then triggers addcalories() function
    Button subtractCalories; //button subtracts the number entered in the edittext, then triggers addcalories() function
    String saddCalories; //stores the text in the addCalories button
    String ssubtractCalories; //stores the text in the subtract calories button
    float addedCalories; //stores the text from the calories edittext box
    float fCaloriesLeft; //float value of the calories left
    float fConsumed; //float value of the calories consumed
    float fCaloricGoal; //float value of the caloric goal
    float fcaloriesBurned = 0f; //float value of the calories burned from walking
    String caloriesBurned; //string value of the calories burned from walking
    SharedPreferences prefNotification; //initialises sharedpreferences for notification use
    SharedPreferences prefSteps; //initialises sharedpreferences for calculation of calories burned from walking
    int steps; //the number of steps extracted from shared preferences
    boolean notificationAllowed; //boolean holds value of whether notifications are allowed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calories);

        //assigning view elements to variables
        textGoal = (TextView) findViewById(R.id.textGoal);
        textLeft = (TextView) findViewById(R.id.textLeft);
        textConsumed = (TextView) findViewById(R.id.textConsumed);
        textBurned = (TextView) findViewById(R.id.textBurned);
        calories = (EditText) findViewById(R.id.editCalories);
        addCalories = (Button) findViewById(R.id.buttonAdd);
        subtractCalories= (Button) findViewById(R.id.buttonSubtract);

        prefNotification = getApplicationContext().getSharedPreferences("settings", MODE_PRIVATE);
        notificationAllowed = prefNotification.getBoolean("calorieNotification", true);
        //initialising the database handler variable to work with userDBhandler class
        dbHandler = new userDBhandler(this, null, null, 1);

        //this handler takes care of UI interaction within receiveThread if no data is gathered
        @SuppressLint("HandlerLeak") final Handler noDataHandler = new Handler(){
            @Override
            public void handleMessage(Message message){
                //if no user data has been entered then remove these views
                addCalories.setVisibility(View.GONE);
                subtractCalories.setVisibility(View.GONE);
                calories.setVisibility(View.GONE);
                textGoal.setVisibility(View.GONE);
                textLeft.setVisibility(View.GONE);
                textConsumed.setVisibility(View.GONE);
            }
        };

        //this handler updates the TextViews to match the content
        @SuppressLint("HandlerLeak") final Handler updateHandler = new Handler() {
            @Override
            public void handleMessage(Message message){
                //if the caloriesLeft is equal to null, then it will be set to equal the caloric goal
                //otherwise the updateCalories method will update the editText boxes to match the stored data
                if (caloriesLeft.equals("0.0")) {
                    caloriesLeft = caloricGoal;
                    fCaloriesLeft = Float.parseFloat(caloricGoal);
                    textLeft.setText(caloriesLeft);
                    textGoal.setText(caloricGoal);
                    textConsumed.setText("0");
                } else {
                    updateCalories(null);
                }
            }
        };
        Runnable receiveRun = new Runnable() {
            @Override
            public void run() {
                //function gets all of the user data
                getData(null);

                //checks to see if user has entered data (if age variable is more than 0 then yes)
                if (age > 0) {

                    //calculations method will use algorithms on the details gathered to determine the caloric intake
                    calculations();

                    //converting string stored in caloric file to float
                    fCaloriesLeft = Float.parseFloat(caloriesLeft);

                    //this handler updates the TextViews to match the content
                    updateHandler.sendEmptyMessage(0);


                } else {
                    //if no user data has been entered then remove these
                    noDataHandler.sendEmptyMessage(0);
                }
            }
        };
        Thread receiveThread = new Thread(receiveRun);
        receiveThread.start();

        //call function that approximately calculates calories burned
        caloriesBurned();
    }

    @Override
    public void onStop() {
        super.onStop();
        //if notifications are allowed then set alarm for notification.
        //this notifications launches 3 hours after the calories page is exited if it is not used in that time
        //NOTE: for demonstration purposes it's been set to 1 minute
        if (notificationAllowed == true) {
            //setting calendar 3 hours in the future
            Calendar calendar = Calendar.getInstance();
             /*int alarmTime = (calendar.get(Calendar.HOUR_OF_DAY)) + 3;
             calendar.set(Calendar.HOUR_OF_DAY,alarmTime);*/
            int alarmTime = (calendar.get(Calendar.MINUTE)) + 1;
            calendar.set(Calendar.MINUTE, alarmTime);

            //setting alarm which executes the code in the timeReceiver class
            Intent intent3 = new Intent(getApplicationContext(), timeReceiver.class);
            PendingIntent pendingIntent3 = PendingIntent.getBroadcast(getApplicationContext(), 100, intent3, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.cancel(pendingIntent3);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent3);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    public void getData(View view){
        //this class receives data from the user stored in the database




                //create string array that holds all of the values stored in the database
                String[] dbString = dbHandler.databaseToString();

                //extracting values from the database array into their own variable
                r1value = dbString[0];
                r2value = dbString[1];
                spinnerValue = dbString[2];
                heightValue = dbString[3];
                weightValue = dbString[4];
                ageValue = dbString[5];

                //if no numbers have been extracted from database, then don't attempt to convert floats
                if (heightValue.equals("")) {
                    height = 0.0f;
                    weight = 0.0f;
                    age = 0.0f;
                } else {
                    height = Integer.parseInt(heightValue);
                    weight = Integer.parseInt(weightValue);
                    age = Integer.parseInt(ageValue);
                }

                //extracts data stored on the calories file
                try {
                    String MessageC;
                    FileInputStream fileInputStreamC = openFileInput("caloric_file");
                    InputStreamReader inputStreamReaderC = new InputStreamReader(fileInputStreamC);
                    BufferedReader bufferedReaderC = new BufferedReader(inputStreamReaderC);
                    StringBuffer stringBufferC = new StringBuffer();
                    while ((MessageC = bufferedReaderC.readLine()) != null) {
                        stringBufferC.append(MessageC);

                    }
                    //caloriesLeft equals to the number stored in file
                    caloriesLeft = (stringBufferC.toString());

                    //if no data is extracted from class then send error
                } catch (FileNotFoundException e) {
                    caloriesLeft = "0.0";
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
    }

    public void calculations(){
        //this class does all the necessary calculations on the data gathered in the getData() function
        if (r1value.equals("male")){
            //calculate BMR for men
            BMR = 66f + (13.75f * weight) + (5f * height) -(6.8f * age);
            if (r2value.equals("none")) {
                MR = BMR*1.1f;
            }else if(r2value.equals("minimal")){
                MR = BMR*1.275f;
            }else if(r2value.equals("active")){
                MR = BMR*1.35f;
            }else{
                MR = BMR*1.525f;
            }
        }else{
            //calculate BMR for female
            BMR = 655f + (9.6f * weight) + (1.8f * height) -(4.7f * age);
            if (r2value.equals("none")) {
                MR = BMR*1.1f;
            }else if(r2value.equals("minimal")){
                MR = BMR*1.275f;
            }else if(r2value.equals("active")){
                MR = BMR*1.35f;
            }else {
                MR = BMR * 1.525f;
            }
        }
        //calculating fitness goal from MR
        Math.round(MR);
        if(spinnerValue.equals("fat loss")){
            caloricGoal = Float.toString(MR - 250);
        }else if(spinnerValue.equals("gain muscle")){
            caloricGoal = Float.toString(MR + 250);
        }else{
            caloricGoal = Float.toString(MR);
        }
    }

    public void updateCalories(View view){
        //this function is used to update the textviews

        //setting up textGoal
        textGoal.setText(caloricGoal);

        //setting up textConsumed
        fCaloricGoal =  Float.parseFloat(caloricGoal);
        fConsumed = fCaloricGoal -fCaloriesLeft;
        textConsumed.setText(Float.toString(fConsumed));

        //setting up textLeft
        caloriesLeft = Float.toString(fCaloriesLeft);
        textLeft.setText(caloriesLeft);

    }

    public void caloriesBurned(){
        //get shared preference value of steps taken by user
        prefSteps = getApplicationContext().getSharedPreferences("activityLoaded", MODE_PRIVATE);
        steps = prefSteps.getInt("realSteps", 20);

        //divide steps by 20 to get calories burned
        fcaloriesBurned = steps/20;

        //update textview
        caloriesBurned = Float.toString(fcaloriesBurned);
        textBurned.setText(caloriesBurned);
    }

    public void addCalories(View view){

        //check to see if data entered is valid
        //if the edittext box is empty then return
        if (calories.getText().toString().isEmpty()){
            return;
        }
        //if the editext value is a bad number then return
        addedCalories = Float.parseFloat(calories.getText().toString());
        if (addedCalories > 9999 || addedCalories < 1) {
            Toast.makeText(this, "you cant enter that number of calories!", Toast.LENGTH_SHORT).show();
            return;
        }

        //determine which button was pressed and subtract the caloriesLeft value with the user entered value
        //extract button text values
        saddCalories = addCalories.getText().toString();
        ssubtractCalories = subtractCalories.getText().toString();

        //do calculation based on what button was pressed
        Button b = (Button)view;
        String buttonText = b.getText().toString();
        if (buttonText.equals(saddCalories)){
            fCaloriesLeft = fCaloriesLeft -addedCalories;
        } else{
            fCaloriesLeft = fCaloriesLeft +addedCalories;
        }

        //update the editText boxes
        updateCalories(null);

        //write new caloriesLeft value to caloric_file
        try {
            FileOutputStream fileOutputStream = openFileOutput("caloric_file", MODE_PRIVATE);
            fileOutputStream.write(caloriesLeft.getBytes());
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //if user goes over calorie limit and notifications are allowed then push notification
        if (fCaloriesLeft < 0f && notificationAllowed == true) {
            Intent intent = new Intent(this, Calories.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
            int notification1Id = 56748;
            NotificationCompat.Builder builder1 = new NotificationCompat.Builder(this, "calories");
            builder1.setSmallIcon(R.drawable.ic_ff); //set icon to custome design
            builder1.setContentTitle("Watch your eating!"); //notification title
            builder1.setContentText("you have went over your caloric goal for today"); //notification content
            builder1.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 }); //set vibration
            builder1.setContentIntent(pendingIntent);
            builder1.setAutoCancel(true);
            builder1.setPriority(NotificationCompat.PRIORITY_LOW);
            initChannels(getApplicationContext()); //initialising channel if api is > 26
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(notification1Id, builder1.build());
        }

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

