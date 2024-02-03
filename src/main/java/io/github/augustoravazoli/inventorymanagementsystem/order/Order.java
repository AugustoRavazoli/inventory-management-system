package io.github.augustoravazoli.inventorymanagementsystem.order;

import io.github.augustoravazoli.inventorymanagementsystem.customer.Customer;
import io.github.augustoravazoli.inventorymanagementsystem.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.validator.constraints.UniqueElements;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Entity
@Table(name = "\"order\"")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDate date;

    @NotNull
    @ManyToOne(optional = false)
    private Customer customer;

    @UniqueElements
    @NotEmpty
    @OrderBy("index")
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @NotNull
    @ManyToOne(optional = false)
    private User owner;

    public Order() {}

    public Order(Long id, OrderStatus status, LocalDate date, Customer customer, List<OrderItem> items, User owner) {
        this.id = id;
        this.status = status;
        this.date = date;
        this.customer = customer;
        setItems(items);
        this.owner = owner;
    }

    public Order(OrderStatus status, Customer customer, List<OrderItem> items) {
        this(null, status, null, customer, items, null);
    }

    public Long getId() {
        return id;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDate getDate() {
        return date;
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
        IntStream.range(0, items.size()).forEach(i -> {
            items.get(i).setOrder(this);
            items.get(i).setIndex(i);
        });
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

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public OrderForm toForm() {
        return new OrderForm(
                status,
                customer.getId(),
                items.stream().map(OrderItem::toForm).toList()
        );
    }

}
