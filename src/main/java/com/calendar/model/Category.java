package com.calendar.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entita reprezentujúca kategóriu udalostí v kalendári.
 *
 * <p>Kategória slúži na organizáciu a filtrovanie udalostí podľa typu
 * (napr. "Práca", "Osobné", "Škola"). Každá kategória má priradenú farbu
 * pre lepšiu vizuálnu identifikáciu v kalendári.</p>
 *
 * <p>Kategória je vždy priradená k jednému používateľovi (User) a používateľ
 * môže mať viacero kategórií.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "category")
public class Category {

    /**
     * Unikátny identifikátor kategórie (primárny kľúč).
     * Automaticky generovaný pri vytvorení novej kategórie.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CategoryID")
    private Integer id;

    /**
     * Názov kategórie.
     * Povinné pole, musí mať dĺžku 1-100 znakov.
     * Napríklad: "Práca", "Osobné", "Škola", "Šport"
     */
    @NotBlank(message = "Category name is required")
    @Size(min = 1, max = 100, message = "Category name must be between 1 and 100 characters")
    @Column(name = "Name", nullable = false)
    private String name;

    /**
     * Farba kategórie v hexadecimálnom formáte.
     * Používa sa na vizuálne odlíšenie udalostí v kalendári.
     * Maximálna dĺžka 20 znakov (napr. "#FF5733").
     */
    @Size(max = 20, message = "Color must not exceed 20 characters")
    @Column(name = "Color")
    private String color;

    /**
     * Voliteľný popis kategórie.
     * Môže obsahovať dodatočné informácie o účele kategórie.
     * Maximálna dĺžka 255 znakov.
     */
    @Size(max = 255, message = "Description must not exceed 255 characters")
    @Column(name = "Description")
    private String description;

    /**
     * Vlastník kategórie (používateľ).
     * Každá kategória patrí práve jednému používateľovi.
     * Vzťah Many-to-One (viac kategórií môže patriť jednému používateľovi).
     */
    @ManyToOne
    @JoinColumn(name = "UserID")
    private User user;

    // Gettery
    public Integer getId() {return id;}
    public String getName() {return name;}
    public String getColor() {return color;}
    public String getDescription() {return description;}
    public User getUser() {return user;}

    // Settery
    public void setId(Integer id) {this.id = id;}
    public void setName(String name) {this.name = name;}
    public void setColor(String color) {this.color = color;}
    public void setDescription(String description) {this.description = description;}
    public void setUser(User user) {this.user = user;}
}