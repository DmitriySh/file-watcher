package ru.shishmakov.dao;

import org.springframework.data.repository.Repository;

import java.io.Serializable;

/**
 * Defines pattern "Data Access Object" as a mechanism for encapsulating storage,
 * retrieval, and search behavior which emulates a collection of objects.
 *
 * @param <T>  type of entity
 * @param <ID> identifier of current entity
 * @author Dmitriy Shishmakov
 */
public interface DbRepository<T, ID extends Serializable> extends Repository<T, ID> {

    /**
     * Retrieves an entity by its id.
     *
     * @param id must not be {@literal null}.
     * @return the entity with the given {@literal id}.
     */
    T findOne(final ID id);

    <S extends T> S save(S entity);

    long count();
}
