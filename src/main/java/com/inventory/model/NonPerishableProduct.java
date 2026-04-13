package com.inventory.model;

import java.time.LocalDate;

/**
 * Represents a non-perishable product (e.g., flour, soap, cooking oil).
 * INHERITS from Product and OVERRIDES getProductInfo() — demonstrating POLYMORPHISM.
 * No expiry date is applicable for this product type.
 */
public class NonPerishableProduct extends Product {

    // -------------------------
    // Constructor (without ID — for new inserts)
    // -------------------------
    public NonPerishableProduct(String name, int quantity, double price) {
        super(name, "Non-Perishable", quantity, price, null); // null = no expiry date
    }

    // -------------------------
    // Constructor (with ID — for database retrieval)
    // -------------------------
    public NonPerishableProduct(int id, String name, int quantity, double price) {
        super(id, name, "Non-Perishable", quantity, price, null);
    }

    // -------------------------
    // Overridden Method (Polymorphism)
    // -------------------------
    /**
     * Returns product info without expiry details since this product does not expire.
     * Overrides the abstract method defined in Product.
     */
    @Override
    public String getProductInfo() {
        return String.format(
                "[Non-Perishable] %s | Qty: %d | Price: KES %.2f | No expiry date",
                getName(), getQuantity(), getPrice()
        );
    }

    // -------------------------
    // Additional Non-Perishable Specific Method
    // -------------------------
    /**
     * Calculates the total stock value of this product.
     * Useful for inventory valuation reports.
     * @return total value = quantity × price
     */
    public double getTotalStockValue() {
        return getQuantity() * getPrice();
    }
}