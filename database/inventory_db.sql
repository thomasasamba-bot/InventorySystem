-- ================================
-- Stage 1: PostgreSQL Database Setup
-- ================================

-- Drop database if it exists (optional but useful during development)
DROP DATABASE IF EXISTS inventory_db;

-- Create the database
CREATE DATABASE inventory_db;

-- Connect to the database
\c inventory_db;

-- ================================
-- Create products table
-- ================================
CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL CHECK (category IN ('Perishable', 'Non-Perishable')),
    quantity INTEGER NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    price NUMERIC(10,2) NOT NULL CHECK (price >= 0),
    expiry_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ================================
-- Insert sample data
-- ================================
INSERT INTO products (name, category, quantity, price, expiry_date) VALUES
('Fresh Milk',    'Perishable',     50, 120.00, '2025-04-20'),
('Sliced Bread',  'Perishable',     30,  65.00, '2025-04-15'),
('Yoghurt',       'Perishable',      0,  85.00, '2025-04-18'),
('Maize Flour',   'Non-Perishable', 100, 180.00, NULL),
('Cooking Oil',   'Non-Perishable',  75, 280.00, NULL),
('Sugar',         'Non-Perishable',   0, 160.00, NULL),
('Laundry Soap',  'Non-Perishable',  45,  55.00, NULL);

-- ================================
-- Verify data
-- ================================
SELECT * FROM products;