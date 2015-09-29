package ru.shishmakov.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.shishmakov.config.AppConfig;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.*;
import java.sql.DatabaseMetaData;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

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
    @Qualifier("fileWatcher")
    private DirectoryFileWatcher watcher;

    @Autowired
    @Qualifier("eventExecutor")
    private ExecutorService executor;

    @Autowired
    private AppConfig config;

    private final AtomicBoolean await = new AtomicBoolean(true);

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
            logger.error("Error starting the server:", e);
            await.set(false);
        }
    }

    @PreDestroy
    public void stop() {
        logger.debug("Finalization server ...");

        logger.info("Shutdown the server: {}", System.getProperty("user.home"));
    }

    public void await() {
        while (await.get()) {
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

        try {
            Files.createDirectories(path.resolve("success"));
            Files.createDirectories(path.resolve("fail"));
        } catch (IOException e) {
            final String message = String.format("Directories 'success'/'fail' can't be created in: '%s'", path);
            throw new CreateDirectoryException(message, e);
        }
        logger.info("Directory path: {}", path);
        return path;
    }

    private boolean isSymbolicLinkLoop(Path path) {
        if (!Files.isSymbolicLink(path)) {
            return false;
        }

        logger.debug("Check symlink ... ");
        try {
            final Path link = path.normalize();
            final Path target = Files.readSymbolicLink(path);
            logger.debug("Target of link \'{}\' -> \'{}\'", link, target);
            if (link.toString().equalsIgnoreCase(Files.readSymbolicLink(target).toString())) {
                logger.debug("Loop symlink \'{}\' -> \'{}\'", target, Files.readSymbolicLink(target));
                return true;
            }
        } catch (IOException ignored) {
        }
        return false;
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
                await.compareAndSet(true, false);
                logger.debug("Shutdown hook has been invoked");
            }
        });
    }
}