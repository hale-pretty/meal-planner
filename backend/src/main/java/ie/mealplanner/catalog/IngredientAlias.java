package ie.mealplanner.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "ingredient_alias")
public class IngredientAlias extends CatalogEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(nullable = false, length = 200)
    private String name;

    protected IngredientAlias() {
    }

    public IngredientAlias(Ingredient ingredient, String name) {
        this.ingredient = ingredient;
        this.name = name;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public String getName() {
        return name;
    }
}
