package ie.mealplanner.suggestions;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "recipe")
public class RecipeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String code;
    @Column(nullable = false, length = 200)
    private String name;
    @Column(nullable = false, length = 1000)
    private String description;
    @Column(nullable = false, length = 30)
    private String cuisine;
    @Column(name = "meal_type", nullable = false, length = 30)
    private String mealType;
    @Column(nullable = false)
    private int servings;
    @Column(name = "cooking_time_minutes", nullable = false)
    private int cookingTimeMinutes;
    @Column(nullable = false)
    private boolean vegetarian;
    @Column(nullable = false)
    private boolean vegan;

    protected RecipeEntity() { }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCuisine() { return cuisine; }
    public String getMealType() { return mealType; }
    public int getServings() { return servings; }
    public int getCookingTimeMinutes() { return cookingTimeMinutes; }
    public boolean isVegetarian() { return vegetarian; }
    public boolean isVegan() { return vegan; }
}
