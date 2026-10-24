package ie.mealplanner.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "retailer")
public class Retailer extends CatalogEntity {
    @Column(nullable = false, length = 100)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "website_url", nullable = false, length = 1000)
    private String websiteUrl;

    protected Retailer() {
    }

    public Retailer(String code, String name, String websiteUrl) {
        this.code = code;
        this.name = name;
        this.websiteUrl = websiteUrl;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }
}
