package io.github.augustoravazoli.inventorymanagementsystem.dashboard;

import io.github.augustoravazoli.inventorymanagementsystem.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

@Entity
@Immutable
public class Dashboard {

    @Id
    private Long id;

    @Column(nullable = false)
    private Long totalCustomers;

    @Column(nullable = false)
    private Long totalCategories;

    @Column(nullable = false)
    private Long totalProducts;

    @Column(nullable = false)
    private Long totalUnpaidOrders;

    @Column(nullable = false)
    private Long totalPaidOrders;

    @Column(nullable = false)
    private BigDecimal totalSales;

    @OneToOne(optional = false)
    @MapsId
    @JoinColumn(name = "owner_id")
    private User owner;

    public Long getId() {
        return id;
    }

    public Long getTotalCustomers() {
        return totalCustomers;
    }

    public Long getTotalCategories() {
        return totalCategories;
    }

    public Long getTotalProducts() {
        return totalProducts;
    }

    public Long getTotalUnpaidOrders() {
        return totalUnpaidOrders;
    }

    public Long getTotalPaidOrders() {
        return totalPaidOrders;
    }

    public BigDecimal getTotalSales() {
        return totalSales;
    }

}
