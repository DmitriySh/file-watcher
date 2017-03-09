package ru.shishmakov.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.invoke.MethodHandles;
import java.nio.file.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

/**
 * Service for retrieving files.
 *
 * @author Dmitriy Shishmakov
 */
@Component
public class FileWatcher {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    @Qualifier("eventExecutor")
    private ExecutorService executor;
    @Resource
    private BlockingQueue<Path> directoryQueue;
    @Autowired
    private AtomicBoolean serverLock;

    private final AtomicBoolean lock = new AtomicBoolean(true);
    private final CountDownLatch latch = new CountDownLatch(1);

    public void start(Path dir) {
        logger.info("Initialise file watcher ...");

        executor.execute(buildIterateFilesTask(dir));
        watchNewFiles(dir);
    }

    public void stop() {
        logger.info("Finalization watcher ...");
        lock.compareAndSet(true, false);
    }

    private void watchNewFiles(Path dir) {
        final FileSystem fileSystem = FileSystems.getDefault();
        final PathMatcher matcher = fileSystem.getPathMatcher("glob:*.xml");
        try (WatchService watchService = fileSystem.newWatchService()) {
            latch.await();
            dir.register(watchService, ENTRY_CREATE);
            while (lock.get()) {
                final WatchKey watchKey = watchService.poll(200, TimeUnit.MILLISECONDS);
                if (watchKey == null) {
                    continue;
                }
                for (WatchEvent<?> watchEvent : watchKey.pollEvents()) {
                    final WatchEvent.Kind<?> kind = watchEvent.<Path>kind();
                    @SuppressWarnings("unchecked")
                    final Path path = dir.resolve(((WatchEvent<Path>) watchEvent).context());
                    if (kind == ENTRY_CREATE && matcher.matches(path.getFileName())) {
                        moveToNextQueue(path);
                    }
                }
                if (!watchKey.reset()) {
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("Error in file watcher", e);
        } finally {
            serverLock.compareAndSet(true, false);
        }
    }

    private Runnable buildIterateFilesTask(final Path dir) {
        return () -> {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.xml")) {
                latch.countDown();
                latch.await();
                for (Path file : stream) {
                    moveToNextQueue(file);
                }
            } catch (Exception e) {
                logger.error("Error in file watcher", e);
                serverLock.compareAndSet(true, false);
            }
        };
    }

    private void moveToNextQueue(Path file) throws InterruptedException {
        while (lock.get()) {
            if (!directoryQueue.offer(file, 200, TimeUnit.MILLISECONDS)) {
                continue;
            }
            logger.info("--> put file \'{}\' : directoryQueue", file.getFileName());
            break;
        }
    }
}
