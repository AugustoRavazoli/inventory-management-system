package io.github.augustoravazoli.inventorymanagementsystem.order;

import io.github.augustoravazoli.inventorymanagementsystem.customer.Customer;
import io.github.augustoravazoli.inventorymanagementsystem.product.Product;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

class OrderBuilder {

    private Long id;
    private Order.Status status;
    private LocalDate date;
    private Customer customer;
    private final List<OrderItem> items = new ArrayList<>();

    public OrderBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public OrderBuilder status(Order.Status status) {
        this.status = status;
        return this;
    }

    public OrderBuilder date(LocalDate date) {
        this.date = date;
        return this;
    }

    public OrderBuilder customer(Customer customer) {
        this.customer = customer;
        return this;
    }

    public OrderBuilder item(int quantity, Product product) {
        this.items.add(new OrderItem(quantity, product));
        return this;
    }

    public Order build() {
        return new Order(id, status, date, customer, items);
    }

}
