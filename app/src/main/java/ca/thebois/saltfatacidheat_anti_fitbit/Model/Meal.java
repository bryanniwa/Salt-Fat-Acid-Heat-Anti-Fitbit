package ca.thebois.saltfatacidheat_anti_fitbit.Model;

import java.util.List;

public class Meal {
    private final String mealName;
    private final int calories;
    private final String date;
    private final String time;

    public Meal(String mealName, int calories, String date, String time) {
        this.mealName = mealName;
        this.calories = calories;
        this.date = date;
        this.time = time;
    }

    public String getMealName() {
        return mealName;
    }

    public int getCalories() {
        return calories;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }
}
