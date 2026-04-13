package com.inventory.model;

import java.time.LocalDate;

/**
 * Abstract base class representing a generic product.
 * Demonstrates ENCAPSULATION through private fields with getters/setters.
 * Declared abstract because a product must be either Perishable or Non-Perishable.
 */
public abstract class Product {

    // -------------------------
    // Private Fields (Encapsulation)
    // -------------------------
    private int id;
    private String name;
    private String category;
    private int quantity;
    private double price;
    private LocalDate expiryDate; // null for non-perishable products

    // -------------------------
    // Constructor (without ID — used when inserting new products)
    // -------------------------
    public Product(String name, String category, int quantity, double price, LocalDate expiryDate) {
        this.name       = name;
        this.category   = category;
        this.quantity   = quantity;
        this.price      = price;
        this.expiryDate = expiryDate;
    }

    // -------------------------
    // Constructor (with ID — used when retrieving from database)
    // -------------------------
    public Product(int id, String name, String category, int quantity, double price, LocalDate expiryDate) {
        this.id         = id;
        this.name       = name;
        this.category   = category;
        this.quantity   = quantity;
        this.price      = price;
        this.expiryDate = expiryDate;
    }

    // -------------------------
    // Abstract Method (Polymorphism — each subclass must implement this)
    // -------------------------
    /**
     * Returns a formatted string describing the product.
     * Each subclass overrides this to include type-specific details.
     */
    public abstract String getProductInfo();

    // -------------------------
    // Concrete Method (shared by all subclasses)
    // -------------------------
    /**
     * Checks whether the product is currently out of stock.
     * @return true if quantity is zero, false otherwise
     */
    public boolean isOutOfStock() {
        return this.quantity == 0;
    }

    /**
     * Restocks the product by adding the given amount to current quantity.
     * Demonstrates METHOD OVERLOADING (polymorphism) alongside the single-param version.
     * @param amount the quantity to add
     */
    public void updateStock(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Stock update amount cannot be negative.");
        }
        this.quantity += amount;
    }

    /**
     * Overloaded version — replaces the stock quantity entirely.
     * Demonstrates METHOD OVERLOADING.
     * @param newQuantity the new absolute quantity
     * @param replace     must be true to confirm replacement (prevents accidental calls)
     */
    public void updateStock(int newQuantity, boolean replace) {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }
        if (replace) {
            this.quantity = newQuantity;
        }
    }

    // -------------------------
    // Getters and Setters
    // -------------------------
    public int getId()                      { return id; }
    public void setId(int id)               { this.id = id; }

    public String getName()                 { return name; }
    public void setName(String name)        { this.name = name; }

    public String getCategory()             { return category; }
    public void setCategory(String cat)     { this.category = cat; }

    public int getQuantity()                { return quantity; }
    public void setQuantity(int quantity)   { this.quantity = quantity; }

    public double getPrice()                { return price; }
    public void setPrice(double price)      { this.price = price; }

    public LocalDate getExpiryDate()                    { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate)     { this.expiryDate = expiryDate; }

    // -------------------------
    // toString (for debugging/logging)
    // -------------------------
    @Override
    public String toString() {
        return String.format("Product[id=%d, name=%s, category=%s, qty=%d, price=%.2f]",
                id, name, category, quantity, price);
    }
}