package com.inventory.dao;

import com.inventory.model.NonPerishableProduct;
import com.inventory.model.PerishableProduct;
import com.inventory.model.Product;
import com.inventory.db.DatabaseConnection;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Quick test to verify the DAO layer works correctly
 * before building the JavaFX UI in Stage 4.
 * Run this as a plain Java main class (no JavaFX needed).
 */
public class DAOTest {
    public static void main(String[] args) {

        ProductDAO dao = new ProductDAO();

        // ---- Test 1: Database Connection ----
        System.out.println("=== Test 1: Database Connection ===");
        boolean connected = DatabaseConnection.testConnection();
        System.out.println("Connected: " + connected);

        // ---- Test 2: Insert a new product ----
        System.out.println("\n=== Test 2: Insert Product ===");
        try {
            PerishableProduct newProduct = new PerishableProduct(
                    "Test Butter", 25, 220.00, LocalDate.of(2025, 5, 15));
            dao.insert(newProduct);
            System.out.println("Inserted with ID: " + newProduct.getId());
        } catch (SQLException e) {
            System.err.println("Insert failed: " + e.getMessage());
        }

        // ---- Test 3: Retrieve all products ----
        System.out.println("\n=== Test 3: Get All Products ===");
        try {
            List<Product> products = dao.getAll();
            for (Product p : products) {
                System.out.println(p.getProductInfo());
            }
        } catch (SQLException e) {
            System.err.println("Retrieval failed: " + e.getMessage());
        }

        // ---- Test 4: Update stock ----
        System.out.println("\n=== Test 4: Update Stock ===");
        try {
            dao.updateStock(1, 999); // Update product ID 1 stock to 999
            Product updated = dao.getById(1);
            System.out.println("Updated quantity: " + updated.getQuantity());
        } catch (SQLException e) {
            System.err.println("Update failed: " + e.getMessage());
        }

        // ---- Test 5: Search by name ----
        System.out.println("\n=== Test 5: Search by Name ===");
        try {
            List<Product> results = dao.searchByName("milk");
            results.forEach(p -> System.out.println(p.getProductInfo()));
        } catch (SQLException e) {
            System.err.println("Search failed: " + e.getMessage());
        }

        // ---- Close connection ----
        DatabaseConnection.closeConnection();
    }
}