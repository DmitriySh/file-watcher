package ru.shishmakov.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.shishmakov.entity.Entry;
import ru.shishmakov.service.PostgresDbEntryService;

import javax.annotation.Resource;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for persisting files.
 *
 * @author Dmitriy Shishmakov
 */
public class FilePersist {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final AtomicInteger quantity = new AtomicInteger(0);

    @Autowired
    PostgresDbEntryService service;

    @Resource(name = "successQueue")
    private BlockingQueue<Entry> successQueue;

    @Autowired
    private AtomicBoolean serverLock;

    private final AtomicBoolean lock = new AtomicBoolean(true);
    private final int number = quantity.incrementAndGet();

    public void start() {
        logger.info("Initialise file persist {} ...", number);
        try {
            while (lock.get()) {
                final Entry entity = successQueue.poll(200, TimeUnit.MILLISECONDS);
                if (entity == null) {
                    continue;
                }
                logger.info("<-- take entity \'{}\' : successQueue", entity);

                service.save(entity);
                logger.info("persist entity \'{}\'", entity);
            }
        } catch (Exception e) {
            logger.error("Error in time of persisting", e);
        } finally {
            serverLock.compareAndSet(true, false);
        }
    }

    public void stop() {
        logger.info("Finalization persist {} ...", number);
        lock.compareAndSet(true, false);
    }


}
