package com.inventory.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Represents a perishable product (e.g., dairy, bread, fresh produce).
 * INHERITS from Product and OVERRIDES getProductInfo() — demonstrating POLYMORPHISM.
 */
public class PerishableProduct extends Product {

    // -------------------------
    // Constructor (without ID — for new inserts)
    // -------------------------
    public PerishableProduct(String name, int quantity, double price, LocalDate expiryDate) {
        super(name, "Perishable", quantity, price, expiryDate);
    }

    // -------------------------
    // Constructor (with ID — for database retrieval)
    // -------------------------
    public PerishableProduct(int id, String name, int quantity, double price, LocalDate expiryDate) {
        super(id, name, "Perishable", quantity, price, expiryDate);
    }

    // -------------------------
    // Overridden Method (Polymorphism)
    // -------------------------
    /**
     * Returns product info including days remaining until expiry.
     * Overrides the abstract method defined in Product.
     */
    @Override
    public String getProductInfo() {
        long daysUntilExpiry = ChronoUnit.DAYS.between(LocalDate.now(), getExpiryDate());

        String expiryStatus;
        if (daysUntilExpiry < 0) {
            expiryStatus = "EXPIRED";
        } else if (daysUntilExpiry == 0) {
            expiryStatus = "Expires TODAY";
        } else {
            expiryStatus = "Expires in " + daysUntilExpiry + " day(s) [" + getExpiryDate() + "]";
        }

        return String.format(
                "[Perishable] %s | Qty: %d | Price: KES %.2f | %s",
                getName(), getQuantity(), getPrice(), expiryStatus
        );
    }

    // -------------------------
    // Additional Perishable-Specific Method
    // -------------------------
    /**
     * Checks if the product is expired.
     * @return true if expiry date is before today
     */
    public boolean isExpired() {
        return getExpiryDate() != null && getExpiryDate().isBefore(LocalDate.now());
    }

    /**
     * Returns the number of days remaining before expiry.
     * Negative value means the product is already expired.
     */
    public long getDaysUntilExpiry() {
        if (getExpiryDate() == null) return Long.MAX_VALUE;
        return ChronoUnit.DAYS.between(LocalDate.now(), getExpiryDate());
    }
}