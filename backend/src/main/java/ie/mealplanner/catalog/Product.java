package ie.mealplanner.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "product")
public class Product extends CatalogEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "retailer_id", nullable = false)
    private Retailer retailer;

    @Column(nullable = false, length = 300)
    private String name;

    @Column(name = "product_url", nullable = false, length = 1000)
    private String productUrl;

    @Column(name = "pack_label", nullable = false, length = 200)
    private String packLabel;

    @Column(name = "pack_quantity", precision = 12, scale = 3)
    private BigDecimal packQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "pack_unit", length = 10)
    private PackUnit packUnit;

    @Column(name = "verified_on", nullable = false)
    private LocalDate verifiedOn;

    protected Product() {
    }

    public Product(Retailer retailer, String name, String productUrl, String packLabel, BigDecimal packQuantity, PackUnit packUnit, LocalDate verifiedOn) {
        this.retailer = retailer;
        this.name = name;
        this.productUrl = productUrl;
        this.packLabel = packLabel;
        this.packQuantity = packQuantity;
        this.packUnit = packUnit;
        this.verifiedOn = verifiedOn;
    }

    public Retailer getRetailer() {
        return retailer;
    }

    public String getName() {
        return name;
    }

    public String getProductUrl() {
        return productUrl;
    }

    public String getPackLabel() {
        return packLabel;
    }

    public BigDecimal getPackQuantity() {
        return packQuantity;
    }

    public PackUnit getPackUnit() {
        return packUnit;
    }

    public LocalDate getVerifiedOn() {
        return verifiedOn;
    }
}
