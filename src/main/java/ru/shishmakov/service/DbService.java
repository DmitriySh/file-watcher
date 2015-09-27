package ru.shishmakov.service;


import java.io.Serializable;

/**
 * Defines pattern "Business Service Facade" as an interface
 * that hides the complexities of business components and their interactions.
 *
 * @param <T>  type of entity
 * @param <ID> identifier of current entity
 * @author Dmitriy Shishmakov
 */
public interface DbService<T, ID extends Serializable> {

    /**
     * Retrieves an entity by its id.
     *
     * @param id must not be {@literal null}.
     * @return the entity with the given {@literal id}.
     */
    T getById(ID id);

    <S extends T> S save(S entity);
}
