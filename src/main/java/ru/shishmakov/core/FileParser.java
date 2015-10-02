package ru.shishmakov.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import ru.shishmakov.util.SymlinkLoops;

import javax.annotation.Resource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service detects content of files.
 *
 * @author Dmitriy Shishmakov
 */
@Component
public class FileParser {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final AtomicInteger number = new AtomicInteger(0);
    private static final String ENTRY_XSD = "entry.xsd";

    @Resource(name = "directoryQueue")
    private BlockingQueue<Path> directoryQueue;

    @Resource(name = "successQueue")
    private BlockingQueue<Path> successQueue;

    @Autowired
    private AtomicBoolean serverLock;

    private AtomicBoolean lock = new AtomicBoolean(true);

    public void start() {
        logger.info("Initialise file parser {} ...", number.incrementAndGet());
        try {
            while (lock.get()) {
                final Path file = directoryQueue.take();
                logger.debug("<-- take file \'{}\' : directoryQueue", file.getFileName());

                if (isReady(file)) {
                    parse(file);
                }
                final String description = String.format("exists: %s; symlink loop: %s; readable: %s; file: %s",
                        Files.exists(file), SymlinkLoops.isSymbolicLinkLoop(file),
                        Files.isReadable(file), Files.isRegularFile(file));
                notProcessed(file, description);
            }
        } catch (Exception e) {
            logger.error("Error in file parser", e);
        } finally {
            serverLock.compareAndSet(true, false);
        }
    }

    private boolean isReady(Path file) {
        return Files.exists(file) && Files.isRegularFile(file) && Files.isReadable(file) && SymlinkLoops.isNotSymbolicLinkLoop(file);
    }

    private void parse(Path file) throws InterruptedException {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(true);
            factory.setSchema(schemaFactory.newSchema(new StreamSource(getClass().getResourceAsStream(ENTRY_XSD))));

            XMLReader reader = factory.newSAXParser().getXMLReader();
            reader.parse(new InputSource(Files.newBufferedReader(file, StandardCharsets.UTF_8)));
            moveToNextQueue(file);
        } catch (SAXException | ParserConfigurationException | IOException e) {
            logger.error("Error", e);
            notProcessed(file, "schema is not valid");
        }
    }

    public void stop() {
        logger.info("Finalization parser ...");
        lock.compareAndSet(true, false);
    }

    private void moveToNextQueue(Path file) throws InterruptedException {
        successQueue.put(file);
        logger.debug("--> put file \'{}\' : successQueue", file.getFileName());
    }

    private void notProcessed(Path file, String description) {
        logger.warn("!!! File: \'{}\' not processed; {}", file, description);
    }
}
