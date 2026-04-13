package com.inventory.dao;

import com.inventory.db.DatabaseConnection;
import com.inventory.model.NonPerishableProduct;
import com.inventory.model.PerishableProduct;
import com.inventory.model.Product;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete implementation of GenericDAO for the Product type.
 * Handles all SQL operations: INSERT, SELECT, UPDATE, DELETE.
 * Demonstrates GENERICS in action — implements GenericDAO<Product>.
 * Uses the factory pattern in mapResultSet() to return the correct subclass.
 */
public class ProductDAO implements GenericDAO<Product> {

    // -------------------------
    // INSERT — Add a new product
    // -------------------------
    /**
     * Inserts a new product into the database.
     * Uses a PreparedStatement to prevent SQL injection.
     *
     * @param product the Product object to insert
     * @throws SQLException if a database error occurs
     */
    @Override
    public void insert(Product product) throws SQLException {
        String sql = """
            INSERT INTO products (name, category, quantity, price, expiry_date)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, product.getName());
            // Default to Non-Perishable if category is null
            stmt.setString(2,
                    product.getCategory() != null ? product.getCategory() : "Non-Perishable");
            stmt.setInt(3, product.getQuantity());
            stmt.setDouble(4, product.getPrice());
            // Expiry date is always optional
            if (product.getExpiryDate() != null) {
                stmt.setDate(5, Date.valueOf(product.getExpiryDate()));
            } else {
                stmt.setNull(5, Types.DATE);
            }

            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) product.setId(keys.getInt(1));

            System.out.println("[DAO] Inserted: " + product.getName());
        }
    }

    // -------------------------
    // SELECT ALL — Retrieve all products
    // -------------------------
    /**
     * Fetches all products from the database.
     * Uses mapResultSet() to convert each row into the correct Product subclass.
     *
     * @return List<Product> containing all products (Perishable + Non-Perishable)
     * @throws SQLException if a database error occurs
     */
    @Override
    public List<Product> getAll() throws SQLException {
        String sql = "SELECT * FROM products ORDER BY id";
        List<Product> products = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                products.add(mapResultSet(rs)); // Convert row → Product object
            }
        }

        System.out.println("[DAO] Retrieved " + products.size() + " products.");
        return products;
    }

    // -------------------------
    // SELECT BY ID — Retrieve a single product
    // -------------------------
    /**
     * Fetches a single product by its primary key.
     *
     * @param id the product ID to look up
     * @return the matching Product, or null if not found
     * @throws SQLException if a database error occurs
     */
    @Override
    public Product getById(int id) throws SQLException {
        String sql = "SELECT * FROM products WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSet(rs);
            }
        }
        return null; // No product found with this ID
    }

    // -------------------------
    // UPDATE — Modify an existing product
    // -------------------------
    /**
     * Updates all fields of an existing product record.
     * Matches the record using the product's ID.
     *
     * @param product the Product with updated values
     * @throws SQLException if a database error occurs
     */
    @Override
    public void update(Product product) throws SQLException {
        String sql = """
                UPDATE products
                SET name = ?, category = ?, quantity = ?, price = ?, expiry_date = ?
                WHERE id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, product.getName());
            stmt.setString(2, product.getCategory());
            stmt.setInt(3, product.getQuantity());
            stmt.setDouble(4, product.getPrice());

            if (product.getExpiryDate() != null) {
                stmt.setDate(5, Date.valueOf(product.getExpiryDate()));
            } else {
                stmt.setNull(5, Types.DATE);
            }

            stmt.setInt(6, product.getId());
            stmt.executeUpdate();

            System.out.println("[DAO] Product updated: ID " + product.getId());
        }
    }

    // -------------------------
// UPDATE STOCK ONLY — Partial update for quantity
// -------------------------
    /**
     * Updates only the quantity field of a product.
     * Used by the "Update Stock" feature in the UI.
     *
     * Validation applied:
     * - productId must be a positive integer
     * - newQuantity must be zero or greater (negative stock is not allowed)
     * - product with the given ID must exist in the database
     * - update must affect exactly one row (confirms the record was found and changed)
     *
     * @param productId   the ID of the product to update (must be > 0)
     * @param newQuantity the new stock quantity (must be >= 0)
     * @throws IllegalArgumentException if productId or newQuantity are invalid
     * @throws SQLException             if the product is not found or a DB error occurs
     */
    public void updateStock(int productId, int newQuantity) throws SQLException {

        // ---- Input Validation ----

        // productId must be a positive integer — IDs generated by SERIAL start at 1
        if (productId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid product ID: " + productId + ". ID must be a positive integer."
            );
        }

        // Stock quantity cannot be negative — zero is valid (out of stock)
        if (newQuantity < 0) {
            throw new IllegalArgumentException(
                    "Invalid quantity: " + newQuantity + ". Stock quantity cannot be negative."
            );
        }

        // Optional upper bound — prevents unrealistically large quantities
        // Adjust MAX_STOCK_QUANTITY to match your business rules
        final int MAX_STOCK_QUANTITY = 100_000;
        if (newQuantity > MAX_STOCK_QUANTITY) {
            throw new IllegalArgumentException(
                    "Quantity " + newQuantity + " exceeds the maximum allowed stock of " + MAX_STOCK_QUANTITY + "."
            );
        }

        // ---- Existence Check — confirm product exists before updating ----
        if (!productExists(productId)) {
            throw new SQLException(
                    "Update failed: No product found with ID " + productId + "."
            );
        }

        // ---- Perform the Update ----
        String sql = "UPDATE products SET quantity = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, newQuantity);
            stmt.setInt(2, productId);

            int rowsAffected = stmt.executeUpdate();

            // Sanity check — executeUpdate should return 1 for a single row update
            if (rowsAffected == 0) {
                throw new SQLException(
                        "Update had no effect. Product ID " + productId + " may no longer exist."
                );
            }

            if (rowsAffected > 1) {
                // Should never happen with a PRIMARY KEY constraint, but guard anyway
                throw new SQLException(
                        "Unexpected update: " + rowsAffected + " rows affected for product ID " + productId + "."
                );
            }

            System.out.println("[DAO] Stock updated for product ID: " + productId +
                    " → new quantity: " + newQuantity);
        }
    }

// -------------------------
// HELPER — Check if a product exists by ID
// -------------------------
    /**
     * Queries the database to confirm a product with the given ID exists.
     * Used as a pre-update existence check to provide clearer error messages.
     *
     * @param productId the product ID to look up
     * @return true if a matching record exists, false otherwise
     * @throws SQLException if a database error occurs
     */
    private boolean productExists(int productId) throws SQLException {
        String sql = "SELECT 1 FROM products WHERE id = ? LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // true if at least one row was returned
        }
    }

    // -------------------------
    // DELETE — Remove a product
    // -------------------------
    /**
     * Deletes a product record from the database.
     *
     * @param id the ID of the product to delete
     * @throws SQLException if a database error occurs
     */
    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM products WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

            System.out.println("[DAO] Product deleted: ID " + id);
        }
    }

    // -------------------------
    // SEARCH — Filter products by name
    // -------------------------
    /**
     * Searches for products whose name contains the given keyword.
     * Uses SQL ILIKE for case-insensitive matching (PostgreSQL specific).
     *
     * @param keyword the search term
     * @return List of matching products
     * @throws SQLException if a database error occurs
     */
    public List<Product> searchByName(String keyword) throws SQLException {
        String sql = "SELECT * FROM products WHERE name ILIKE ? ORDER BY name";
        List<Product> results = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                results.add(mapResultSet(rs));
            }
        }
        return results;
    }

    // -------------------------
    // HELPER — Map ResultSet row → correct Product subclass
    // -------------------------
    /**
     * Converts a single ResultSet row into the appropriate Product subclass.
     * Acts as a simple factory — returns PerishableProduct or NonPerishableProduct
     * based on the category column value.
     *
     * @param rs the current ResultSet row
     * @return a Product subclass instance
     * @throws SQLException if a column cannot be read
     */
    private Product mapResultSet(ResultSet rs) throws SQLException {
        int    id       = rs.getInt("id");
        String name     = rs.getString("name");
        String category = rs.getString("category");
        int    quantity = rs.getInt("quantity");
        double price    = rs.getDouble("price");

        // getDate() returns null for non-perishable items — convert safely
        Date sqlDate    = rs.getDate("expiry_date");
        LocalDate expiryDate = (sqlDate != null) ? sqlDate.toLocalDate() : null;

        // Factory logic — return the correct subclass based on category
        if ("Perishable".equalsIgnoreCase(category)) {
            return new PerishableProduct(id, name, quantity, price, expiryDate);
        } else {
            return new NonPerishableProduct(id, name, quantity, price);
        }
    }
}