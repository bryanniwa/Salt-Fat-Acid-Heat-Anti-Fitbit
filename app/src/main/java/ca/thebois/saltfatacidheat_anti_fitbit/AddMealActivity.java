package ca.thebois.saltfatacidheat_anti_fitbit;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;

import ca.thebois.saltfatacidheat_anti_fitbit.Model.Meal;
import ca.thebois.saltfatacidheat_anti_fitbit.Model.MealManager;

// Array of options --> ArrayAdapter --> ListView
// List view: {views: items}


public class AddMealActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {
    private MealListAdapter adapter;
    private MealManager mealManager;
    private EditText mealNameBox;
    private EditText caloriesBox;
    private final Calendar calendar = Calendar.getInstance();
    private String time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_meal);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        mealManager = MealManager.getInstance();

        mealNameBox = findViewById(R.id.meal_name_input);
        caloriesBox = findViewById(R.id.calorie_input);

        setupAddMealButton();
        populateListView();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        time = hourOfDay + ":" + minute;
    }

    private void setupAddMealButton(){
        ImageButton addButton = findViewById(R.id.add_meal_button);
        addButton.setOnClickListener(listener -> {
            DialogFragment timePicker = new TimePickerFragment();
            timePicker.show(getSupportFragmentManager(), "time picker");
            String mealName = mealNameBox.getText().toString();
            String calories = caloriesBox.getText().toString();
            int cals = Integer.parseInt(calories);
            String currentDate = DateFormat.getDateInstance(DateFormat.SHORT).format(calendar.getTime());
            Meal newMeal = new Meal(mealName, cals, currentDate, time);
            mealManager.addMeal(newMeal);
            adapter.notifyDataSetChanged();
        });
    }

    private static class MealListAdapter extends ArrayAdapter<Meal> {
        private final Context context;
        private final List<Meal> meals;

        public MealListAdapter(Context context, List<Meal> meals) {
            super(context, 0, meals);
            this.context = context;
            this.meals = meals;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View listItem = convertView;
            if(listItem == null) {
                listItem = LayoutInflater.from(context).inflate(R.layout.meal, parent, false);
            }

            if (MealManager.getInstance().getMeals().size() > 0) {
                Meal currentMeal = meals.get(position);

                TextView index = listItem.findViewById(R.id.text_item_position);
                index.setText(Integer.toString(position + 1));

                TextView mealName = listItem.findViewById(R.id.text_meal_name);
                mealName.setText(currentMeal.getMealName());

                TextView calorieCount = listItem.findViewById(R.id.text_calorie_value);
                calorieCount.setText(Integer.toString(currentMeal.getCalories()));

                TextView textViewDate = listItem.findViewById(R.id.text_date);
                textViewDate.setText(currentMeal.getDate());

                TextView textViewTime = listItem.findViewById(R.id.text_time);
                textViewTime.setText(currentMeal.getTime());
            }
            return listItem;
        }
    }

    private void populateListView(){

        // Build Adapter
        adapter = new MealListAdapter(
                this, // Context for the activity
                mealManager.getMeals());  // Items to be displayed

        // Configure the list view
        ListView list = findViewById(R.id.mealComponents);
        list.setAdapter(adapter);
    }

    public static Intent makeLaunchIntent(Context context) {
        return new Intent(context, AddMealActivity.class);
    }
}