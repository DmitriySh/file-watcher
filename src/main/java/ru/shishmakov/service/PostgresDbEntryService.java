package ru.shishmakov.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.shishmakov.dao.DbRepository;
import ru.shishmakov.entity.Entry;


import java.util.UUID;

/**
 * Access to domain type {@link Entry} by instance of DAO object.
 *
 * @author Dmitriy Shishmakov
 */
@Service("postgresService")
public class PostgresDbEntryService implements DbService<Entry, Long> {

    @Autowired
    @Qualifier("postgresRepository")
    private DbRepository<Entry, Long> repository;

    @Override
    public Entry getById(Long id) {
        return repository.findOne(id);
    }

    @Override
    public <S extends Entry> S save(S entity) {
        return repository.save(entity);
    }
}
