package io.github.augustoravazoli.inventorymanagementsystem.order;

import io.github.augustoravazoli.inventorymanagementsystem.product.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
public class OrderItem {

    @NotNull
    @Min(1)
    @Column(nullable = false)
    private Integer quantity;

    @Id
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id")
    @OnDelete(action = OnDeleteAction.RESTRICT)
    private Product product;

    @Id
    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(nullable = false)
    private Integer index;

    public OrderItem() {}

    public OrderItem(Integer quantity, Product product, Order order) {
        this.quantity = quantity;
        this.product = product;
        this.order = order;
    }

    public OrderItem(Integer quantity, Product product) {
        this(quantity, product, null);
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

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public BigDecimal getAmount() {
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || this.getClass() != other.getClass()) return false;
        var that = (OrderItem) other;
        return Objects.equals(this.product.getId(), that.product.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(product.getId());
    }

    public OrderItemForm toForm() {
        return new OrderItemForm(quantity, product.getId());
    }

}
