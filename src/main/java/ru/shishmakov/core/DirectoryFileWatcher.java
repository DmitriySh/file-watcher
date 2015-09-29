package ru.shishmakov.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;

/**
 * Service for retrieving files.
 *
 * @author Dmitriy Shishmakov
 */
@Component("fileWatcher")
public class DirectoryFileWatcher {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public void start(Path path) {
        logger.debug("Initialise file watcher ...");
    }

    @PreDestroy
    public void stop() {
        logger.debug("Finalization watcher ...");

    }
}
