package com.example.user.projectfinale;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class userInfo extends AppCompatActivity {

    //declaring all variables used in class with a description for each

    EditText editHeight; //user enters their height here
    EditText editWeight; //user enters their weight here
    EditText editAge; //user enters their age here
    Spinner mySpinner; //creating a spinner object for holding fitness goal
    userDBhandler dbHandler; //creating a dbhandler object to communicate with userDBhandler class
    String spinnerValue; //holds the spinner value
    String r1value; //holds the gender value of the first radio button
    String r2value; //holds the activity level value of the second radio button
    RadioGroup radiogroup1; //object groups the gender radio buttons
    RadioGroup radiogroup2; //object groups the activity level radio buttons
    RadioButton radiobutton1; //gets the selected radio button in the gender radio group
    RadioButton radiobutton2; //gets the selected radio button in the activity level radio group
    String stringHeight; //holds the string of editHeight
    String stringWeight; //holds the string of editWeight
    String stringAge; //holds the string of editAge
    SharedPreferences prefSteps; //the sharedpreference object holding the counter value
    SharedPreferences.Editor editor;
    RadioButton rmale;
    RadioButton rfemale;
    RadioButton rnone;
    RadioButton rminimal;
    RadioButton ractive;
    RadioButton rveryactive;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        //setting up spinner with array values stored in strings file
        mySpinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(userInfo.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.names));
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mySpinner.setAdapter(myAdapter);

        //assigning user input boxes to variables
        editHeight = (EditText) findViewById(R.id.editHeight);
        editWeight = (EditText) findViewById(R.id.editWeight);
        editAge = (EditText) findViewById(R.id.editAge);
        radiogroup1 = (RadioGroup)findViewById(R.id.radiogroup1);
        radiogroup2= (RadioGroup)findViewById(R.id.radiogroup2);

        //setting up radio buttons for input checking
        rmale=(RadioButton) findViewById(R.id.radioMale);
        rfemale=(RadioButton) findViewById(R.id.radioFemale);
        rnone=(RadioButton) findViewById(R.id.radioNone);
        rminimal=(RadioButton) findViewById(R.id.radioMinimal);
        ractive=(RadioButton) findViewById(R.id.radioActive);
        rveryactive=(RadioButton) findViewById(R.id.radioVeryActive);

        //creating a database handler variable
        dbHandler = new userDBhandler (this,null,null,1);

        //printing the values already stored in database
        printDatabase();


    }

    public void addButtonClicked(View view){
        //checks to see if all details have been entered
        if(editHeight.getText().toString().isEmpty()||editWeight.getText().toString().isEmpty()||editAge.getText().toString().isEmpty()|| rmale.isChecked()==false&&rfemale.isChecked()==false|| rnone.isChecked() == false && rminimal.isChecked()==false && ractive.isChecked()==false && rveryactive.isChecked() == false){
            Toast.makeText(getApplicationContext(), "not all details have been entered...",  Toast.LENGTH_SHORT).show();
            return;
        }

        //perform data storage in a runnable as a new thread called addThread
        Runnable addRun = new Runnable(){
            @Override
            public void run() {
                //delete previously stored data
                dbHandler.deleteDetails();

                //gets radio button values and convert to string
                radiobutton1=(RadioButton) findViewById(radiogroup1.getCheckedRadioButtonId());
                radiobutton2=(RadioButton) findViewById(radiogroup2.getCheckedRadioButtonId());
                r1value = ((String) radiobutton1.getText());
                r2value = ((String) radiobutton2.getText());

                //converts spinner and editText values to string
                spinnerValue = (mySpinner.getSelectedItem().toString());
                stringHeight = editHeight.getText().toString();
                stringWeight = editWeight.getText().toString();
                stringAge = editAge.getText().toString();

                //stores each piece of user entered data into the database in a certain order
                userDetails genderDetails = new userDetails(r1value);
                userDetails levelDetails = new userDetails(r2value);
                userDetails goalDetails = new userDetails(spinnerValue);
                userDetails heightDetails = new userDetails(stringHeight);
                userDetails weightDetails = new userDetails(stringWeight);
                userDetails ageDetails = new userDetails(stringAge);

                dbHandler.addDetails(genderDetails);
                dbHandler.addDetails(levelDetails);
                dbHandler.addDetails(goalDetails);
                dbHandler.addDetails(heightDetails);
                dbHandler.addDetails(weightDetails);
                dbHandler.addDetails(ageDetails);

                //reset the counter in stepCounter so that the next time it is opened it will start over
                //setting counter to 0 means that it will resave the initial steps in the database when stepCounter activity is launched
                prefSteps = getApplicationContext().getSharedPreferences("activityLoaded", MODE_PRIVATE);
                editor = prefSteps.edit();
                editor.putInt("counter", 0);
                editor.commit();

                //reset steps used to calculate how many calories are burned
                editor.putInt("realSteps", 0);
                editor.commit();
            }
        };
        Thread addThread = new Thread(addRun);
        addThread.start();


        Toast.makeText(getApplicationContext(), "info updated",  Toast.LENGTH_LONG).show();
    }

    public void printDatabase() {
        //create string array that holds all of the values stored in the database
        String[] dbString = dbHandler.databaseToString();

        //set radio button values to match database
        //for gender
        if (dbString[0].equals("male")){
            radiobutton1=(RadioButton) findViewById(R.id.radioMale);
            radiobutton1.setChecked(true);
        }else if (dbString[0].equals("")){
            // set nothing
        }else{
            radiobutton1=(RadioButton) findViewById(R.id.radioFemale);
            radiobutton1.setChecked(true);
        }
        //for activity level
        if (dbString[1].equals("none")){
            radiobutton2=(RadioButton) findViewById(R.id.radioNone);
            radiobutton2.setChecked(true);
        }else if(dbString[1].equals("minimal")){
            radiobutton2=(RadioButton) findViewById(R.id.radioMinimal);
            radiobutton2.setChecked(true);
        }else if(dbString[1].equals("active")){
            radiobutton2=(RadioButton) findViewById(R.id.radioActive);
            radiobutton2.setChecked(true);
        }
        else if(dbString[1].equals("")){
            // set nothing
        }else{
            radiobutton2=(RadioButton) findViewById(R.id.radioVeryActive);
            radiobutton2.setChecked(true);
        }


        //set spinner value to match database
        if (dbString[2].equals("fat loss")) {
            mySpinner.setSelection(0);
        }else if (dbString[2].equals("gain muscle")) {
            mySpinner.setSelection(1);
        } else {
            mySpinner.setSelection(2);
        }

        //set editText values to match database
        editHeight.setText(dbString[3]);
        editWeight.setText(dbString[4]);
        editAge.setText(dbString[5]);
    }
}
