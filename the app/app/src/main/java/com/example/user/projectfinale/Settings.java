package com.example.user.projectfinale;

import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Settings extends AppCompatActivity {
    //declaring all variables used in class with a description for each

    Button button; //this button will reset the users details
    TextView textClick; //this textview will display after the button is clicked
    Switch calorieSwitch; //switch is used to set calorie notifications
    Switch stepSwitch; //switch is used to set steps notifications
    int i =0; //counter used to hold number of times reset button has been clicked
    SharedPreferences pref; //initialises an object for the settings shared preferences
    SharedPreferences.Editor editor; //an editor for the settings shared preferences
    boolean calorieSwitchState; //holds the switch state for the calories switch
    boolean stepSwitchState; //holds the switch state for the steps switch
    userDBhandler dbHandler; //handles database requests
    SharedPreferences prefSteps; //the sharedpreference object holding the counter value
    SharedPreferences.Editor editorSteps;//an editor for the steps shared preferences
    String caloriesLeft = "0.0"; //set the caloriesLeft equal to 0.0
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //set up view elements with variables
        textClick = (TextView) findViewById(R.id.textClick);
        button = (Button) findViewById(R.id.buttonDelete);
        calorieSwitch = (Switch)findViewById(R.id.calorieSwitch);
        stepSwitch = (Switch)findViewById(R.id.stepSwitch);

        //initialise shared preference object and editor
        pref = getApplicationContext().getSharedPreferences("settings", MODE_PRIVATE);
        editor = pref.edit();
        //sets sliders to the stored value
        calorieSwitchState = pref.getBoolean("calorieNotification", true);
        stepSwitchState = pref.getBoolean("stepNotification", true);
        calorieSwitch.setChecked(calorieSwitchState);
        stepSwitch.setChecked(stepSwitchState);

        //setting up the database handler variable with userDBhandler class
        dbHandler = new userDBhandler (Settings.this,null,null,1);

        //this listener will make sure the button is clicked twice in order for it to delete data
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i++;
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (i==1){
                            //clicked once

                            textClick.setText("click again to delete data");
                        }else if (i ==2){
                            //clicked twice

                            //perform data deletion in the thread deleteThread
                            Runnable deleteRun = new Runnable() {
                                @Override
                                public void run() {
                                    //delete details
                                    dbHandler.deleteDetails();

                                    //reset the counter in stepCounter so that the next time it is opened it will start over
                                    //setting counter to 0 means that it will resave the initial steps in the database when stepCounter activity is launched
                                    prefSteps = getApplicationContext().getSharedPreferences("activityLoaded", MODE_PRIVATE);
                                    editorSteps = prefSteps.edit();
                                    editorSteps.putInt("counter", 0);
                                    editorSteps.commit();

                                    //reset steps used to calculate how many calories are burned
                                    editorSteps.putInt("realSteps", 0);
                                    editorSteps.commit();

                                    //reset the number of calories left in caloric_file to 0.0,
                                    //next time Calories activity is launched the value will be set to the same as caloricGoal
                                    try {
                                        FileOutputStream fileOutputStream = getApplicationContext().openFileOutput("caloric_file", MODE_PRIVATE);
                                        fileOutputStream.write(caloriesLeft.getBytes());
                                        fileOutputStream.close();
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            };
                            Thread deleteThread = new Thread(deleteRun);
                            deleteThread.start();

                            //let user know that data has been deleted
                            textClick.setText("data deleted");
                        }
                    }
                }, 500);

            }
        });
        //on change listener for calorie switch
        calorieSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true){
                    //allow notifications
                    editor.putBoolean("calorieNotification", true);
                    editor.commit();
                }else{
                    //disallow notifications
                    editor.putBoolean("calorieNotification", false);
                    editor.commit();
                }
            }
        });
        //on change listener for step switch
        stepSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true){
                    //allow notifications
                    editor.putBoolean("stepNotification", true);
                    editor.commit();
                }else{
                    //disallow notifications
                    editor.putBoolean("stepNotification", false);
                    editor.commit();
                }
            }
        });
    }

}
