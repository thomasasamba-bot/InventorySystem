package com.inventory.dao;

import java.sql.SQLException;
import java.util.List;

/**
 * Generic Data Access Object (DAO) interface.
 * Defines standard CRUD operations for any entity type T.
 *
 * This satisfies the GENERICS requirement of the project.
 * The type parameter <T> is bounded — any class can be used as T.
 *
 * @param <T> the model type this DAO manages (e.g., Product)
 */
public interface GenericDAO<T> {

    /**
     * Inserts a new record into the database.
     * @param entity the object to persist
     * @throws SQLException if a database error occurs
     */
    void insert(T entity) throws SQLException;

    /**
     * Retrieves all records from the database.
     * @return List of all entities of type T
     * @throws SQLException if a database error occurs
     */
    List<T> getAll() throws SQLException;

    /**
     * Retrieves a single record by its unique ID.
     * @param id the primary key of the record
     * @return the entity with the given ID, or null if not found
     * @throws SQLException if a database error occurs
     */
    T getById(int id) throws SQLException;

    /**
     * Updates an existing record in the database.
     * @param entity the updated object (must have a valid ID)
     * @throws SQLException if a database error occurs
     */
    void update(T entity) throws SQLException;

    /**
     * Deletes a record from the database by its ID.
     * @param id the primary key of the record to delete
     * @throws SQLException if a database error occurs
     */
    void delete(int id) throws SQLException;
}