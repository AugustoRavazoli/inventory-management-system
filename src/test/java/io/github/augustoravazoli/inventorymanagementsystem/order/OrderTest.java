package io.github.augustoravazoli.inventorymanagementsystem.order;

import io.github.augustoravazoli.inventorymanagementsystem.product.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class OrderTest {

    @Test
    void getAmount() {
        // given
        var order = new OrderBuilder()
                .item(2, new Product(null, null, null, "20"))
                .item(4, new Product(null, null, null, "10"))
                .build();
        // when
        var amount = order.getAmount();
        // then
        assertThat(amount).isEqualTo(new BigDecimal("80"));
    }

    @Test
    void getQuantity() {
        // given
        var order = new OrderBuilder()
                .item(2, null)
                .item(4, null)
                .build();
        // when
        var quantity = order.getQuantity();
        // then
        assertThat(quantity).isEqualTo(6);
    }

}
