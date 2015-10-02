package ru.shishmakov.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;
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

    @Resource(name = "successQueue")
    private BlockingQueue<Path> successQueue;

    @Autowired
    private AtomicBoolean serverLock;

    private final AtomicBoolean lock = new AtomicBoolean(true);
    private final int number = quantity.incrementAndGet();

    public void start() {
        logger.info("Initialise file persist {} ...", number);
        try {
            while (lock.get()) {
                // start
            }
        } catch (Exception e) {
            logger.error("Error", e);
        } finally {
            serverLock.compareAndSet(true, false);
        }
    }

    public void stop() {
        logger.info("Finalization persist {} ...", number);
        lock.compareAndSet(true, false);
    }


}
