package ru.shishmakov.dao;


import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.shishmakov.entity.Entry;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;


/**
 * Captures the domain type {@link Entry} to manage by PostgreSQL.
 *
 * @author Dmitriy Shishmakov
 */
@Repository("postgresRepository")
public class PostgresDbEntryRepository implements DbRepository<Entry, Long> {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    @Override
    public Entry findOne(final Long id) {
        TypedQuery<Entry> query = em.createNamedQuery(Entry.QueryName.FIND_BY_ID, Entry.class);
        query.setParameter("id", id);
        return query.getSingleResult();
    }

    @Transactional
    @Override
    public <S extends Entry> S save(S entity) {
        if (entity.getId() == null) {
            em.persist(entity);
            return entity;
        } else {
            return em.merge(entity);
        }
    }

    @Transactional
    @Override
    public long count() {
        return em.createNamedQuery(Entry.QueryName.CALCULATE_ENTRIES, Long.class).getSingleResult();
    }

}
