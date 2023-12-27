package io.github.augustoravazoli.inventorymanagementsystem.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.NumberFormat;

import java.math.BigDecimal;

public class ProductForm {

    @NotBlank
    private String name;

    @DecimalMin("0")
    @NotNull
    private Integer quantity;

    @NumberFormat(style = NumberFormat.Style.CURRENCY, pattern = "#,###.00")
    @DecimalMin("0.01")
    @NotNull
    private BigDecimal price;

    public ProductForm() {}

    public ProductForm(String name, Integer quantity, BigDecimal price) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
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

    public Product toEntity() {
        return new Product(null, name, quantity, price);
    }

}
