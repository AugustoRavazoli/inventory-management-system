package io.github.augustoravazoli.inventorymanagementsystem.product;

import io.github.augustoravazoli.inventorymanagementsystem.category.Category;
import io.github.augustoravazoli.inventorymanagementsystem.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "name", "owner_id" }))
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Category category;

    @Min(0)
    @Column(nullable = false)
    private Integer quantity = 0;

    @DecimalMin("0.01")
    @Column(nullable = false)
    private BigDecimal price = BigDecimal.ZERO;

    @ManyToOne(optional = false)
    private User owner;

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

    public Product(String name, Category category, Integer quantity, String price, User owner) {
        this(null, name, category, quantity, new BigDecimal(price));
        this.owner = owner;
    }

    public Product(Long id) {
        this.id = id;
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

    public void increaseQuantity(int amount) {
        this.quantity += amount;
    }

    public void decreaseQuantity(int amount) {
        this.quantity -= amount;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public ProductForm toForm() {
        var categoryId = category != null ? category.getId() : null;
        return new ProductForm(name, categoryId, quantity, price);
    }

}
