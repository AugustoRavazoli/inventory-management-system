package io.github.augustoravazoli.inventorymanagementsystem.order;

import io.github.augustoravazoli.inventorymanagementsystem.customer.Customer;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "`order`")
public class Order {

    public enum Status {
        PAID, UNPAID
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(optional = false)
    private Customer customer;

    @NotEmpty
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn
    private List<OrderItem> items = new ArrayList<>();

    public Order() {}

    public Order(Long id, Status status, LocalDate date, Customer customer, List<OrderItem> items) {
        this.id = id;
        this.status = status;
        this.date = date;
        this.customer = customer;
        this.items = items;
    }

    public Order(Status status, Customer customer, List<OrderItem> items) {
        this(null, status, null, customer, items);
    }

    public Long getId() {
        return id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items.clear();
        this.items.addAll(items);
    }

    public int getQuantity() {
        return items.stream()
                .map(OrderItem::getQuantity)
                .reduce(Integer::sum)
                .orElse(0);
    }

    public BigDecimal getAmount() {
        return items.stream()
                .map(OrderItem::getAmount)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    public OrderForm toForm() {
        return new OrderForm(
                OrderForm.StatusForm.valueOf(status.name()),
                customer.getId(),
                items.stream().map(item -> new OrderItemForm(item.getQuantity(), item.getProduct().getId())).toList()
        );
    }

}
