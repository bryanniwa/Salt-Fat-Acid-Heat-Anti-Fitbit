package ca.thebois.saltfatacidheat_anti_fitbit.Model;

import java.util.ArrayList;
import java.util.List;

public class MealManager {
    private final List<Meal> meals = new ArrayList<>();
    private int totalCalories = 0;

    private static MealManager instance;
    private MealManager() {

    }
    public static MealManager getInstance() {
        if (instance == null) {
            instance = new MealManager();
        }
        return instance;
    }

    public void addMeal(Meal meal) {
        meals.add(meal);
        totalCalories += meal.getCalories();
    }

    public int getTotalCalories() {
        return totalCalories;
    }

    public List<Meal> getMeals() {
        return meals;
    }
}
