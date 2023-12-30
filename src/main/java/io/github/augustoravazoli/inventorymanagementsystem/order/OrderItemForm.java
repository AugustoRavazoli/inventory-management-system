package io.github.augustoravazoli.inventorymanagementsystem.order;

import io.github.augustoravazoli.inventorymanagementsystem.product.Product;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class OrderItemForm {

    @Min(1)
    @NotNull
    private Integer quantity;

    @NotNull
    private Long productId;

    public OrderItemForm() {}

    public OrderItemForm(Integer quantity, Long productId) {
        this.quantity = quantity;
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public OrderItem toEntity() {
        return new OrderItem(quantity, new Product(productId));
    }

}
