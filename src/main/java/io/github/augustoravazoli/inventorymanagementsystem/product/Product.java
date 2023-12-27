package io.github.augustoravazoli.inventorymanagementsystem.product;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @DecimalMin("0")
    @Column(nullable = false)
    private Integer quantity;

    @DecimalMin("0.01")
    @Column(nullable = false)
    private BigDecimal price;

    public Product() {}

    public Product(Long id, String name, Integer quantity, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    public Product(String name, Integer quantity, String price) {
        this(null, name, quantity, new BigDecimal(price));
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

}
