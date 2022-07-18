package edu.upenn.cis350.group1.calorietracker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// daily activity responsible for Daily view

public class DailyActivity extends CalorieTrackerActivity {
    private DatabaseHandler dbHandler; // database handler

    // keys for various maps
    private static final String KEY_MEAL_NAME = "mealName";
    private static final String KEY_MEAL_TYPE = "type";
    private static final String KEY_MEAL_CALORIES = "calories";
    private static final String KEY_MEAL_ID = "mealID";
    private static final int RESULT_OK = 400;
    private static final int ACTIVITY_DAILY = 1;


    // rigid meal type array inherited from Meal.java
    private static final String[] types = {"Breakfast", "Lunch", "Dinner", "Snack"};

    private Date date; // need this for AlertDialog
    private double value; // need this for AlertDialog

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily);

        // create database handler
        dbHandler = new DatabaseHandler(getApplicationContext());

        // update daily intakes of weight and water
        updateLabels();

        // update and expand Daily list view
        updateAndExpandListView();

        ExpandableListView view = (ExpandableListView) findViewById(R.id.daily_list);
        view.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                ExpandableListAdapter adapter = parent.getExpandableListAdapter();

                HashMap<String, String> data =
                        (HashMap) adapter.getChild(groupPosition, childPosition);

                int mealID = Integer.parseInt(data.get(KEY_MEAL_ID));

                Intent i = new Intent(DailyActivity.this, InputActivity.class);
                i.putExtra("EXISTING", true);
                i.putExtra("MEAL_ID", mealID);
                startActivityForResult(i, ACTIVITY_DAILY);
                return true;
            }
        });
    }

    // helper func to update daily intake labels
    private void updateLabels() {
        // get today's date
        Date date = new Date(System.currentTimeMillis());

        // get water and weight values
        double weight = dbHandler.getWeight(date);
        double water = dbHandler.getWater(date);

        // set weight label if necessary
        if (weight >= 0.0) {
            TextView weightLabel = (TextView) findViewById(R.id.weight_counter);
            String weightString = dbHandler.getWeight(date) + " lbs.";
            weightLabel.setText(weightString);
        }

        // set water label if necessary
        if (water >= 0.0) {
            TextView waterLabel = (TextView) findViewById(R.id.water_counter);
            String waterString = dbHandler.getWater(date) + " oz.";
            waterLabel.setText(waterString);
        }
    }

    // click handler for adding new meal from Daily Screen
    public void onMealButtonClick(View v) {
        Intent mealInputScreen = new Intent(DailyActivity.this, InputActivity.class);

        startActivityForResult(mealInputScreen, ACTIVITY_DAILY);
    }



    // fetch and prepare data for the listview
    private ExpandableListAdapter prepareListData() {
        // get current time
        Date date = new Date(System.currentTimeMillis());

        // get list of meals
        List<Meal> meals = dbHandler.getAllMealsList(date);

        // list of maps to hold categories of meals
        ArrayList<HashMap<String, String>> parentMapList = new ArrayList<>();

        // add a map entry for every parent to the above list
        for (int i = 0; i < types.length; i++) {
            HashMap<String, String> group = new HashMap<>();
            group.put(KEY_MEAL_TYPE, types[i]);
            parentMapList.add(group);
        }

        // parameters required by list adapter - shows value of key "type in mealtype_header
        int groupLayout = R.layout.meal_group;
        String[] groupFrom = new String[] { KEY_MEAL_TYPE };
        int[] groupTo = new int [] { R.id.mealtype_header };

        // create list of list of maps for child view's purposes
        ArrayList<ArrayList<HashMap<String, String>>> childListOfListOfMaps = new ArrayList<>();

        // create empty lists of maps for children of each category
        for (int i = 0; i < parentMapList.size(); i++) {
            ArrayList<HashMap<String, String>> children = new ArrayList<>();
            childListOfListOfMaps.add(children);
        }

        // add each individual meal as a child of appropriate parent
        for (int i = 0; i < meals.size(); i++) {
            Meal m = meals.get(i);
            int typeCode = m.getTypeCode();
            ArrayList<HashMap<String, String>> listOfMaps = childListOfListOfMaps.get(typeCode);

            HashMap<String, String> mealProperties = new HashMap<>();
            mealProperties.put(KEY_MEAL_NAME, m.getName());
            mealProperties.put(KEY_MEAL_CALORIES, String.valueOf(m.getCalories()));
            mealProperties.put(KEY_MEAL_ID, String.valueOf(m.getMealID()));

            listOfMaps.add(mealProperties);
        }

        // parameters required by list adapter
        int childLayout = R.layout.meal_item;
        String[] childFrom = new String[] { KEY_MEAL_NAME, KEY_MEAL_CALORIES };
        int[] childTo = new int [] { R.id.textview_meal_title, R.id.textview_meal_calories };

        // return the actual adapter
        return new SimpleExpandableListAdapter(DailyActivity.this, parentMapList,
                groupLayout, groupFrom, groupTo, childListOfListOfMaps, childLayout, childFrom,
                childTo);
    }

    // update data in list view and expand categories with data
    private void updateAndExpandListView() {
        // get adapter and view
        ExpandableListAdapter adapter = prepareListData();
        ExpandableListView view = (ExpandableListView) findViewById(R.id.daily_list);

        // show the actual view
        view.setAdapter(adapter);

        // expand categories that contain data
        for (int i = 0; i < adapter.getGroupCount(); i++) {
            if (adapter.getChildrenCount(i) > 0) view.expandGroup(i);
        }
    }

    // general button click handler, executes weight button click if false, water button if true
    private void onButtonClick(final boolean isWater) {
        date = new Date(System.currentTimeMillis());
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        // Set up the input box
        final EditText inputBox = new EditText(this);
        // set input type and hint
        inputBox.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);

        // set header and input box text depending on water or weight
        if (isWater) {
            // set up for water input
            dialog.setTitle("Water intake on " + date.toString());

            double waterIntake = dbHandler.getWater(date);
            if (waterIntake == -1) {
                inputBox.setHint(Double.toString(0.0));
            } else {
                inputBox.setText(Double.toString(waterIntake));
            }
        } else {
            // set up for weight input
            dialog.setTitle("Weight for " + date.toString());

            double weight = dbHandler.getWeight(date);
            if (weight == -1) {
                inputBox.setHint(Double.toString(0.0));
            } else {
                inputBox.setText(Double.toString(weight));
            }
        }

        dialog.setView(inputBox);

        // Set up the buttons
        dialog.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // save value as either a water or weight recording
                value = Double.parseDouble(inputBox.getText().toString());
                if (value >= 0.0) {
                    if (isWater) {
                        dbHandler.setWaterForDate(date, value);
                    } else {
                        dbHandler.setWeightForDate(date, value);
                    }
                    updateLabels();
                } else {
                    dialog.cancel();
                }
            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        dialog.show();
    }

    // click handler for water button
    public void onWaterButtonClick(View v) {
        onButtonClick(true);
    }

    // click handler for setting weight from Daily Screen
    public void onWeightButtonClick(View v) {
        onButtonClick(false);
    }

    // called when a new meal is input using InputActivity
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);

        // if launched InputActivity returned properly then update list
        if(requestCode == ACTIVITY_DAILY && resultCode == RESULT_OK){
            updateAndExpandListView();
        }
    }
}
