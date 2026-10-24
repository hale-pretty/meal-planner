package ie.mealplanner.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "ingredient")
public class Ingredient extends CatalogEntity {
    @Column(nullable = false, length = 100)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 100)
    private String form;

    protected Ingredient() {
    }

    public Ingredient(String code, String name, String form) {
        this.code = code;
        this.name = name;
        this.form = form;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getForm() {
        return form;
    }
}
