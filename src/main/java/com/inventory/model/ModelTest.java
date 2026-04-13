package com.inventory.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ModelTest {
    public static void main(String[] args) {

        // Create one of each product type
        PerishableProduct milk = new PerishableProduct(
                "Fresh Milk", 50, 120.00, LocalDate.of(2025, 4, 20));

        NonPerishableProduct flour = new NonPerishableProduct(
                "Maize Flour", 100, 180.00);

        NonPerishableProduct sugar = new NonPerishableProduct(
                "Sugar", 0, 160.00); // Zero stock

        // Test polymorphism — same method call, different output
        System.out.println("=== Polymorphism Demo ===");
        System.out.println(milk.getProductInfo());
        System.out.println(flour.getProductInfo());
        System.out.println(sugar.getProductInfo());

        // Test out-of-stock check
        System.out.println("\n=== Stock Status ===");
        System.out.println(milk.getName()  + " out of stock? " + milk.isOutOfStock());
        System.out.println(sugar.getName() + " out of stock? " + sugar.isOutOfStock());

        // Test method overloading
        System.out.println("\n=== Stock Update (Overloading) ===");
        flour.updateStock(20);            // Adds 20 to existing stock
        System.out.println("After adding 20: " + flour.getQuantity());

        flour.updateStock(50, true);      // Replaces with 50
        System.out.println("After replacing with 50: " + flour.getQuantity());

        // Test Generics — using a typed List
        System.out.println("\n=== Generics Demo (List<Product>) ===");
        List<Product> inventory = new ArrayList<>();
        inventory.add(milk);
        inventory.add(flour);
        inventory.add(sugar);

        for (Product p : inventory) {
            System.out.println(p.getProductInfo()); // Polymorphic call
        }
    }
}