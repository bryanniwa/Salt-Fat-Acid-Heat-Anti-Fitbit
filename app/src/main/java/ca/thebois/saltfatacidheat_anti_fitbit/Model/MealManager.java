package ca.thebois.saltfatacidheat_anti_fitbit.Model;

import java.util.ArrayList;
import java.util.List;

public class MealManager {
    private final List<Meal> meals = new ArrayList<>();
    private int totalCalories;

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
    }

    public List<Meal> getMeals() {
        return meals;
    }
}
