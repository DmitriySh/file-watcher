package ru.shishmakov.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.lang.invoke.MethodHandles;
import java.nio.file.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Service for retrieving files.
 *
 * @author Dmitriy Shishmakov
 */
@Component
public class DirectoryFileWatcher {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    @Qualifier("eventExecutor")
    private ExecutorService executor;

    @Resource
    private BlockingQueue<Path> directoryQueue;

    @Autowired
    private AtomicBoolean lock;

    final CountDownLatch latch = new CountDownLatch(2);

    public void start(Path dir) {
        logger.debug("Initialise file watcher ...");


        executor.execute(buildIterateFilesTask(dir));
        watchNewFiles(dir);
    }

    private void watchNewFiles(Path dir) {
        final FileSystem fileSystem = FileSystems.getDefault();
        final PathMatcher matcher = fileSystem.getPathMatcher("glob:*.xml");
        try (WatchService watchService = fileSystem.newWatchService()) {
            dir.register(watchService, ENTRY_CREATE);
            WatchKey watchKey = null;
            while (true) {
                latch.countDown();
                latch.await();
                watchKey = watchService.take();
                for (WatchEvent<?> watchEvent : watchKey.pollEvents()) {
                    final WatchEvent.Kind<?> kind = watchEvent.<Path>kind();
                    final Path path = ((WatchEvent<Path>) watchEvent).context();
                    if (kind == ENTRY_CREATE && matcher.matches(path)) {
                        putFile(path);
                    }
                }
                if (!watchKey.reset()) {
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("Error in file watcher", e);
        } finally {
            lock.compareAndSet(true, false);
        }
    }

    private Runnable buildIterateFilesTask(final Path dir) {
        return new Runnable() {
            @Override
            public void run() {
                int i = 0;
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.xml")) {
                    latch.countDown();
                    latch.await();
                    for (Path file : stream) {
                        final Path temp = dir.resolve("crated" + i++ + ".xml");
                        Files.deleteIfExists(temp);
                        Files.createFile(temp);
                        Thread.sleep(2000);
                        putFile(file);
                    }
                } catch (Exception e) {
                    logger.error("Error in file watcher", e);
                    lock.compareAndSet(true, false);
                }
            }
        };
    }

    private void putFile(Path file) throws InterruptedException {
        logger.debug("Put file: \'{}\' into directoryQueue", file.getFileName());
        directoryQueue.put(file);
    }

    @PreDestroy
    public void stop() {
        logger.debug("Finalization watcher ...");

    }
}
