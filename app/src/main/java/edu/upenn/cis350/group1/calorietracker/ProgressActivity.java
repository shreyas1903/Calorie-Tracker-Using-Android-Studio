package edu.upenn.cis350.group1.calorietracker;

import android.os.Bundle;

import android.widget.ProgressBar;
import android.widget.TextView;
import java.sql.Date;
import java.util.List;

/**.
 */
public class ProgressActivity extends CalorieTrackerActivity {

    private DatabaseHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        // adding up total values for each of the nutrients
        int calorieStatus = 0;
        int proteinStatus = 0;
        int sodiumStatus = 0;
        int carbsStatus = 0;

        db = new DatabaseHandler(getApplicationContext());
        List<Meal> meals = db.getAllMealsList(new Date(System.currentTimeMillis()));
        for(Meal meal: meals){
            calorieStatus += meal.getCalories();
            proteinStatus += meal.getProtein();
            sodiumStatus += meal.getSodium();
            carbsStatus += meal.getCarbs();

        }

        //sets the bar data for each bar, geting the progressbar
        //and edittext objects from xml
        createBar((ProgressBar) findViewById(R.id.calories_bar) ,
                  (TextView) findViewById(R.id.calories_text),
                   calorieStatus, "Calories");
        createBar((ProgressBar) findViewById(R.id.protein_bar),
                  (TextView) findViewById(R.id.protein_text),
                   proteinStatus, "Protein");
        createBar((ProgressBar) findViewById(R.id.sodium_bar),
                  (TextView) findViewById(R.id.sodium_text),
                   sodiumStatus, "Sodium");
        createBar((ProgressBar) findViewById(R.id.carbs_bar),
                  (TextView) findViewById(R.id.carbs_text),
                   carbsStatus, "Carbs");

    }

    /*
    params: progressbar to edit, textview to edit, progress thus far to use,
    type of nutrient bar relates to.
    This methods just uses progressbar methods to input the required data.
     */
    private void createBar(ProgressBar bar, TextView text, int progress, String type){
        bar.setMax(db.getSetting(type.toLowerCase()));
        bar.setProgress(progress);
        text.setText(type + " " +  progress+"/"+bar.getMax());
    }



}
