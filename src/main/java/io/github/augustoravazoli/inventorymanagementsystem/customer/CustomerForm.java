package io.github.augustoravazoli.inventorymanagementsystem.customer;

import com.opencsv.bean.CsvBindByPosition;
import jakarta.validation.constraints.NotBlank;

public class CustomerForm {

    @CsvBindByPosition(position = 0, required = true)
    @NotBlank
    private String name;

    @CsvBindByPosition(position = 1, required = true)
    @NotBlank
    private String address;

    @CsvBindByPosition(position = 2, required = true)
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
