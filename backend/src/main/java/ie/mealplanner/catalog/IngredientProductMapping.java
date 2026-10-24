package ie.mealplanner.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "ingredient_product_mapping")
public class IngredientProductMapping extends CatalogEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "reviewed_on", nullable = false)
    private LocalDate reviewedOn;

    protected IngredientProductMapping() {
    }

    public IngredientProductMapping(Ingredient ingredient, Product product, LocalDate reviewedOn) {
        this.ingredient = ingredient;
        this.product = product;
        this.reviewedOn = reviewedOn;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public Product getProduct() {
        return product;
    }

    public LocalDate getReviewedOn() {
        return reviewedOn;
    }
}
