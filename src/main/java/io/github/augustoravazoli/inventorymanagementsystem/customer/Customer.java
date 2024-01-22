package io.github.augustoravazoli.inventorymanagementsystem.customer;

import io.github.augustoravazoli.inventorymanagementsystem.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "name", "owner_id" }))
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false)
    private String address;

    @NotBlank
    @Column(nullable = false)
    private String phone;

    @NotNull
    @ManyToOne(optional = false)
    private User owner;

    public Customer() {}

    public Customer(Long id, String name, String address, String phone) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
    }

    public Customer(String name, String address, String phone) {
        this(null, name, address, phone);
    }

    public Customer(String name, String address, String phone, User owner) {
        this(null, name, address, phone);
        this.owner = owner;
    }

    public Customer(Long id) {
        this.id = id;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public CustomerForm toForm() {
        return new CustomerForm(name, address, phone);
    }

}
