package io.github.augustoravazoli.inventorymanagementsystem.order;

import io.github.augustoravazoli.inventorymanagementsystem.product.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

@Entity
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(1)
    @Column(nullable = false)
    private Integer quantity;

    @ManyToOne(optional = false)
    private Product product;

    public OrderItem() {}

    public OrderItem(Long id, Integer quantity, Product product) {
        this.id = id;
        this.quantity = quantity;
        this.product = product;
    }

    public OrderItem(Integer quantity, Product product) {
        this(null, quantity, product);
    }

    public Long getId() {
        return id;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public BigDecimal getAmount() {
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

}
