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
import javax.sql.DataSource;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.*;
import java.sql.DatabaseMetaData;
import java.util.concurrent.ExecutorService;
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

    @Autowired
    @Qualifier("eventExecutor")
    private ExecutorService executor;

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
            // watcher task
            executor.execute(buildTask(path));
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

    private Runnable buildTask(final Path path) {
        return new Runnable() {
            @Override
            public void run() {
                watcher.start(path);
            }
        };
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