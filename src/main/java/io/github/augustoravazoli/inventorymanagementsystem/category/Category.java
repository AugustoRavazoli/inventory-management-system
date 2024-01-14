package io.github.augustoravazoli.inventorymanagementsystem.category;

import io.github.augustoravazoli.inventorymanagementsystem.user.User;
import jakarta.persistence.*;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "name", "owner_id" }))
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(optional = false)
    private User owner;

    public Category() {}

    public Category(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Category(Long id) {
        this(id, null);
    }

    public Category(String name) {
        this(null, name);
    }

    public Category(String name, User owner) {
        this(null, name);
        this.owner = owner;
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

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public CategoryForm toForm() {
        return new CategoryForm(name);
    }

}
