package ru.shishmakov.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.shishmakov.config.AppConfig;
import ru.shishmakov.core.exception.ConnectionlessException;
import ru.shishmakov.core.exception.DirectoryException;
import ru.shishmakov.core.exception.SymbolicLinkLoopException;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.*;
import java.sql.DatabaseMetaData;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static ru.shishmakov.util.SymlinkLoops.isSymbolicLinkLoop;

/**
 * Manage life cycle of File Watch Server.
 *
 * @author Dmitriy Shishmakov
 */
@Component("server")
public class Server {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    private DataSource dataSource;

    @Autowired
    private DirectoryFileWatcher watcher;

    @Resource(name = "directoryQueue")
    private BlockingQueue<Path> directoryQueue;

    @Resource(name = "successQueue")
    private BlockingQueue<Path> successQueue;

    @Resource(name = "failQueue")
    private BlockingQueue<Path> failQueue;

    @Autowired
    @Qualifier("eventExecutor")
    private ExecutorService executor;

    @Autowired
    @Qualifier("scheduledExecutor")
    public ScheduledExecutorService scheduled;

    @Autowired
    private AppConfig config;

    @Autowired
    private AtomicBoolean lock;

    public void start() throws InterruptedException {
        logger.debug("Initialise server ...");

        try {
            // hook
            registerShutdownHook();
            // db
            while (!hasDbConnection()) {
                logger.debug("Trying to check the DB connection again ...");
                Thread.sleep(10_000);
            }
            // directory
            final Path path = checkDirectory();
            // load factor of queues
            runLoadFactorTask(directoryQueue, "directoryQueue");
            runLoadFactorTask(successQueue, "successQueue");
            runLoadFactorTask(failQueue, "failQueue");
            // watcher task
            runWatcherTask(path);
            logger.info("Start the server: {}. Watch on: {}", this.getClass().getSimpleName(), path);
        } catch (Throwable e) {
            logger.error("Error starting the server", e);
            lock.compareAndSet(true, false);
        }
    }

    @PreDestroy
    public void stop() {
        logger.debug("Finalization server ...");

        logger.info("Shutdown the server: {}", this.getClass().getSimpleName());
    }

    public void await() {
        while (lock.get()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }
    }

    public boolean hasDbConnection() {
        logger.debug("Check connection to DB ... ");
        try {
            if (!dataSource.getConnection().isValid(5)) {
                throw new ConnectionlessException("The DB connection is not established");
            }
            final DatabaseMetaData metaData = dataSource.getConnection().getMetaData();
            logger.info("Connected to DB on {}: driver: {}", metaData.getURL(), metaData.getDriverVersion());
            return true;
        } catch (Exception e) {
            logger.error("Error: {}", e.getMessage());
        }
        return false;
    }

    private void runLoadFactorTask(final BlockingQueue<Path> queue, final String nameQueue) {
        scheduled.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                final int size = queue.size();
                final int capacity = queue.remainingCapacity() + size;
                final int percent = size * 100 / capacity;
                logger.debug("{}: {}/{} = {}%", nameQueue, size, capacity, percent);
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    private Path checkDirectory() {
        logger.debug("Check directory ... ");

        final Path path = Paths.get(config.getDirectoryPath()).normalize();
        if (isSymbolicLinkLoop(path)) {
            throw new SymbolicLinkLoopException("Target directory shouldn't be a symlink loop");
        }
        if (Files.exists(path) && Files.isRegularFile(path)) {
            throw new DirectoryException(String.format("This is not a directory: '%s'", path));
        }
        try {
            Files.createDirectories(path.resolve("success"));
            Files.createDirectories(path.resolve("fail"));
        } catch (IOException e) {
            final String message = String.format("Directories 'success'/'fail' can't be created in: '%s'", path);
            throw new DirectoryException(message, e);
        }
        logger.info("Directory path: {}", path);
        return path;
    }

    private void runWatcherTask(final Path path) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                watcher.start(path);
            }
        });
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                lock.compareAndSet(true, false);
                logger.debug("Shutdown hook has been invoked");
            }
        });
    }
}