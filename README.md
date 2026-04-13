# InvenTrack — Inventory Management System

> BBT 2202: Advanced Object Oriented Programming  
> Strathmore University — School of Computing and Engineering Sciences

---

## Overview

InvenTrack is a JavaFX-based desktop Inventory Management System built as part of the BBT 2202 Advanced OOP project. It demonstrates core OOP principles — encapsulation, inheritance, polymorphism, generics, and exception handling — in a practical, real-world application connected to a PostgreSQL database.

### Key Features

- **Add products** — capture name, quantity, and price
- **Update stock** — via a dedicated form or inline table button
- **View all products** — sortable, searchable TableView with live stats
- **Out-of-stock highlighting** — product name and row turn red when quantity reaches zero
- **Dashboard stats** — live totals for products, out-of-stock count, and estimated stock value
- **Input validation** — visual field feedback with error/success states
- **CSS styling** — professional themed UI with sidebar navigation

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17+ |
| GUI Framework | JavaFX 17+ |
| Database | PostgreSQL 14+ |
| DB Connectivity | JDBC (postgresql-42.7.3.jar) |
| Build Tool | Maven or plain Java (javac) |
| IDE Recommended | IntelliJ IDEA |

---

## Project Structure

```
InventorySystem/
├── src/
│   └── com/inventory/
│       ├── main/
│       │   └── MainApp.java                  ← JavaFX entry point
│       ├── model/
│       │   ├── Product.java                  ← Abstract base class (encapsulation)
│       │   ├── PerishableProduct.java         ← Extends Product (inheritance)
│       │   └── NonPerishableProduct.java      ← Extends Product (polymorphism)
│       ├── db/
│       │   └── DatabaseConnection.java        ← Singleton JDBC connection manager
│       ├── dao/
│       │   ├── GenericDAO.java                ← Generic interface (generics)
│       │   └── ProductDAO.java                ← CRUD implementation
│       ├── controller/
│       │   └── InventoryController.java       ← All UI logic and DB interaction
│       └── util/
│           └── InputValidator.java            ← Reusable validation utility
├── resources/
│   └── styles.css                             ← JavaFX CSS stylesheet
└── database/
    └── inventory_db.sql                       ← PostgreSQL setup script
```

---

## OOP Principles Demonstrated

### Encapsulation
`Product.java` declares all fields as `private` with public getters and setters:
```java
private String name;
private int quantity;
private double price;

public String getName()           { return name; }
public void setName(String name)  { this.name = name; }
public boolean isOutOfStock()     { return quantity == 0; }
```

### Inheritance
`PerishableProduct` and `NonPerishableProduct` both extend the abstract `Product` class:
```java
public abstract class Product { ... }
public class PerishableProduct extends Product { ... }
public class NonPerishableProduct extends Product { ... }
```

### Polymorphism
Method **overriding** — each subclass provides its own implementation of `getProductInfo()`:
```java
@Override
public String getProductInfo() {
    // PerishableProduct: includes expiry info
    // NonPerishableProduct: includes "No expiry date"
}
```
Method **overloading** — `updateStock()` has two signatures:
```java
public void updateStock(int amount)               // adds to existing stock
public void updateStock(int newQty, boolean replace)  // replaces entirely
```

### Generics
`GenericDAO<T>` defines type-safe CRUD operations:
```java
public interface GenericDAO<T> {
    void insert(T entity) throws SQLException;
    List<T> getAll() throws SQLException;
    T getById(int id) throws SQLException;
    void update(T entity) throws SQLException;
    void delete(int id) throws SQLException;
}

public class ProductDAO implements GenericDAO<Product> { ... }
```

### Exception Handling
All database operations use try-catch with proper resource management:
```java
try (Connection conn = DatabaseConnection.getConnection();
     PreparedStatement stmt = conn.prepareStatement(sql)) {
    stmt.executeUpdate();
} catch (SQLException e) {
    System.err.println("DB Error: " + e.getMessage());
    throw e;
}
```

---

## Setup Instructions

### Prerequisites

- Java 17 or higher
- JavaFX 17 SDK — download from [gluonhq.com/products/javafx](https://gluonhq.com/products/javafx)
- PostgreSQL 14 or higher
- IntelliJ IDEA (recommended) or any Java IDE
- PostgreSQL JDBC Driver — `postgresql-42.7.3.jar`

---

### Step 1 — Clone the Repository

```bash
git clone git@github.com:thomasasamba-bot/InventorySystem.git
cd InventorySystem
```

---

### Step 2 — Set Up the Database

Open **pgAdmin** or the **psql** terminal and run:

```sql
-- Create the database
CREATE DATABASE inventory_db;

-- Connect to it
\c inventory_db

-- Create the products table
CREATE TABLE products (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL,
    category    VARCHAR(50)   NOT NULL,
    quantity    INTEGER       NOT NULL DEFAULT 0,
    price       NUMERIC(10,2) NOT NULL,
    expiry_date DATE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert sample data
INSERT INTO products (name, category, quantity, price, expiry_date) VALUES
('Fresh Milk',    'Perishable',     50,  120.00, '2025-04-20'),
('Sliced Bread',  'Perishable',     30,   65.00, '2025-04-15'),
('Yoghurt',       'Perishable',      0,   85.00, '2025-04-18'),
('Maize Flour',   'Non-Perishable', 100, 180.00, NULL),
('Cooking Oil',   'Non-Perishable',  75, 280.00, NULL),
('Sugar',         'Non-Perishable',   0, 160.00, NULL),
('Laundry Soap',  'Non-Perishable',  45,  55.00, NULL);
```

Or run the provided script directly:
```bash
psql -U postgres -f database/inventory_db.sql
```

---

### Step 3 — Configure Database Credentials

Open `src/com/inventory/db/DatabaseConnection.java` and update:

```java
private static final String HOST     = "localhost";
private static final String PORT     = "5432";
private static final String DATABASE = "inventory_db";
private static final String USERNAME = "postgres";       // your username
private static final String PASSWORD = "your_password";  // your password
```

---

### Step 4 — Add JavaFX and JDBC to Project

**In IntelliJ IDEA:**

1. Go to `File → Project Structure → Libraries`
2. Click `+` and add the JavaFX `lib/` folder
3. Click `+` again and add `postgresql-42.7.3.jar`

**If using Maven**, add to `pom.xml`:
```xml
<dependencies>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>17.0.2</version>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <version>42.7.3</version>
    </dependency>
</dependencies>
```

---

### Step 5 — Configure VM Options for JavaFX

In your IDE run configuration, add the following VM arguments (adjust the path to your JavaFX SDK):

```
--module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml
```

---

### Step 6 — Run the Application

Run `MainApp.java` as the main class. The application will:

1. Test the database connection on startup
2. Show an error dialog if the connection fails
3. Launch the full UI if connected successfully

---

## How to Use the System

### Adding a Product
1. Click **Add Product** in the sidebar
2. Enter the product name, quantity, and price
3. Optionally select a category (defaults to Non-Perishable)
4. Click **Add Product** — the table refreshes automatically

### Updating Stock
**Option A — Update Stock view:**
1. Click **Update Stock** in the sidebar
2. Select a product from the dropdown
3. Enter the new quantity
4. Click **Update Stock**

**Option B — Inline from the table:**
1. Find the product in the All Products table
2. Click the **Edit Stock** button on that row
3. Enter the new quantity in the dialog
4. Click **Save**

### Viewing Products
- Click **All Products** in the sidebar
- Use the search bar to filter by product name
- Products with zero quantity appear with a **red name** and red row background
- Click column headers to sort

---

## Database Schema

```sql
CREATE TABLE products (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL,
    category    VARCHAR(50)   NOT NULL DEFAULT 'Non-Perishable',
    quantity    INTEGER       NOT NULL DEFAULT 0,
    price       NUMERIC(10,2) NOT NULL,
    expiry_date DATE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## Bonus Features Implemented

| Bonus Feature | Details |
|---|---|
| Input Validation | `InputValidator.java` — visual red/green border feedback on all fields |
| CSS Styling | `styles.css` — full theme with sidebar, badges, stat cards, red row highlighting |
| Advanced TableView | Sortable columns, live search, inline Edit Stock button per row |

---

## Group Members

| Student Number | Name |
|---|---|
| [191900] | [Monica Michelle Njeri Mwangi] |
| [008331] | [Thomas Asamba] |

---

## License

This project was developed for academic purposes as part of the BBT 2202 course at Strathmore University.
