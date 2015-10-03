package ru.shishmakov.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.shishmakov.config.AppConfig;
import ru.shishmakov.core.exception.DirectoryException;
import ru.shishmakov.core.exception.SymbolicLinkLoopException;
import ru.shishmakov.util.DbUtil;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.DatabaseMetaData;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static ru.shishmakov.util.SymlinkLoopUtil.isSymbolicLinkLoop;

/**
 * Manage life cycle of File Watch Server.
 *
 * @author Dmitriy Shishmakov
 */
public abstract class Server {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    private DataSource dataSource;

    @Resource(name = "directoryQueue")
    private BlockingQueue<Path> directoryQueue;

    @Resource(name = "successQueue")
    private BlockingQueue<Path> successQueue;

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

    @Autowired
    private FileWatcher watcher;

    private FileParser[] parsers;

    private FilePersist[] persists;

    public void start() throws InterruptedException {
        logger.info("Initialise server ...");

        try {
            // hook
            registerShutdownHook();
            // db
            while (!DbUtil.hasDbConnection(dataSource)) {
                logger.debug("Trying to check the DB connection again ...");
                Thread.sleep(10_000);
            }
            final DatabaseMetaData metaData = dataSource.getConnection().getMetaData();
            logger.info("Connected to DB on {}: driver: {}", metaData.getURL(), metaData.getDriverVersion());
            // directory
            final Path path = checkDirectory();
            // load factor of queues
            runLoadFactorTask();
            // watcher task
            runWatcherTask(path);
            // parser task
            parsers = runParserTasks(7);
            // persist task
            persists = runPersistTasks(7);
            logger.info("Start the server: {}. Watch on: {}", this.getClass().getSimpleName(), path);
        } catch (Throwable e) {
            logger.error("Error starting the server", e);
            lock.compareAndSet(true, false);
        }
    }

    public void stop() {
        logger.info("Finalization server ...");
        watcher.stop();
        for (FileParser parser : parsers) {
            parser.stop();
        }
        for (FilePersist persist : persists) {
            persist.stop();
        }
        try {
            executor.shutdown();
            executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignored) {
        }

        try {
            scheduled.shutdown();
            scheduled.awaitTermination(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignored) {
        }

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

    private void runLoadFactorTask() {
        scheduled.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                percentages(directoryQueue, "directoryQueue");
                percentages(successQueue, "successQueue");
            }
        }, 2, 15, TimeUnit.SECONDS);
    }

    private void percentages(BlockingQueue<Path> queue, String queueName) {
        final int size = queue.size();
        final int capacity = queue.remainingCapacity() + size;
        final int percent = size * 100 / capacity;
        logger.debug("{}: {}/{} = {}%", queueName, size, capacity, percent);
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

    private FileParser[] runParserTasks(int count) {
        final FileParser[] parsers = new FileParser[count];
        for (int i = 0; i < count; i++) {
            final FileParser fileParser = getFileParser();
            parsers[i] = fileParser;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    fileParser.start();
                }
            });
        }
        return parsers;
    }

    private FilePersist[] runPersistTasks(int count) {
        final FilePersist[] persists = new FilePersist[count];
        for (int i = 0; i < count; i++) {
            final FilePersist filePersist = getFilePersist();
            persists[i] = filePersist;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    filePersist.start();
                }
            });
        }
        return persists;
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

    protected abstract FileParser getFileParser();

    protected abstract FilePersist getFilePersist();
}