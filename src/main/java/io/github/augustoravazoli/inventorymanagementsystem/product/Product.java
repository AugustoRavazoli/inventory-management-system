package io.github.augustoravazoli.inventorymanagementsystem.product;

import io.github.augustoravazoli.inventorymanagementsystem.category.Category;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;

@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Category category;

    @DecimalMin("0")
    @Column(nullable = false)
    private Integer quantity;

    @DecimalMin("0.01")
    @Column(nullable = false)
    private BigDecimal price;

    public Product() {}

    public Product(Long id, String name, Category category, Integer quantity, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.price = price;
    }

    public Product(String name, Category category, Integer quantity, String price) {
        this(null, name, category, quantity, new BigDecimal(price));
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public ProductForm toForm() {
        var categoryId = category != null ? category.getId() : null;
        return new ProductForm(name, categoryId, quantity, price);
    }

}
