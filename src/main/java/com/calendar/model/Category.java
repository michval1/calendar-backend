package com.calendar.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CategoryID")
    private Integer id;

    @NotBlank(message = "Category name is required")
    @Size(min = 1, max = 100, message = "Category name must be between 1 and 100 characters") // Matches: `Name` varchar(100)
    @Column(name = "Name", nullable = false)
    private String name;

    @Size(max = 20, message = "Color must not exceed 20 characters") // Matches: `Color` varchar(20)
    @Column(name = "Color")
    private String color;

    @Size(max = 255, message = "Description must not exceed 255 characters") // Matches: `Description` varchar(255)
    @Column(name = "Description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "UserID")
    private User user;

    public Integer getId() {return id;}
    public String getName() {return name;}
    public String getColor() {return color;}
    public String getDescription() {return description;}
    public User getUser() {return user;}


    public void setId(Integer id) {this.id = id;}
    public void setName(String name) {this.name = name;}
    public void setColor(String color) {this.color = color;}
    public void setDescription(String description) {this.description = description;}
    public void setUser(User user) {this.user = user;}
}