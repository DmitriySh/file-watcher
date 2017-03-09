package ru.shishmakov.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.shishmakov.entity.Entry;
import ru.shishmakov.service.PostgresDbEntryService;
import ru.shishmakov.util.EntryWrapper;

import javax.annotation.Resource;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

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
    private BlockingQueue<EntryWrapper> successQueue;
    @Autowired
    private AtomicBoolean serverLock;

    private final int number = quantity.incrementAndGet();
    private final AtomicBoolean lock = new AtomicBoolean(true);

    public void start() {
        logger.info("Initialise file persist {} ...", number);
        try {
            while (lock.get()) {
                final EntryWrapper wrapper = successQueue.poll(200, TimeUnit.MILLISECONDS);
                if (wrapper == null) {
                    continue;
                }
                logger.info("<-- take transient entity \'{}\' : successQueue", wrapper.getEntry());

                final Entry entry = service.save(wrapper.getEntry());
                logger.info("persist entity \'{}\'", entry);

                moveToSuccessDirectory(wrapper.getFile());
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

    private void moveToSuccessDirectory(Path source) {
        try {
            final Path target = source.resolveSibling(Paths.get("success", source.getFileName().toString()));
            Files.move(source, target, REPLACE_EXISTING, ATOMIC_MOVE);
            logger.info("move file \'{}\' to directory \'{}\'", source.getFileName(), target.getParent());
        } catch (IOException e) {
            logger.error("File move error", e);
        }
    }


}
