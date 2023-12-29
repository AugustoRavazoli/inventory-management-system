package io.github.augustoravazoli.inventorymanagementsystem.customer;

import jakarta.validation.constraints.NotBlank;

public class CustomerForm {

    @NotBlank
    private String name;

    @NotBlank
    private String address;

    @NotBlank
    private String phone;

    public CustomerForm() {}

    public CustomerForm(String name, String address, String phone) {
        this.name = name;
        this.address = address;
        this.phone = phone;
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

    public Customer toEntity() {
        return new Customer(name, address, phone);
    }

}
