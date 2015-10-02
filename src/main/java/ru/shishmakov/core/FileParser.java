package ru.shishmakov.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

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

/**
 * Service detects content of files.
 *
 * @author Dmitriy Shishmakov
 */
@Component
public class FileParser {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Resource(name = "directoryQueue")
    private BlockingQueue<Path> directoryQueue;

    @Resource(name = "successQueue")
    private BlockingQueue<Path> successQueue;

    @Resource(name = "failQueue")
    private BlockingQueue<Path> failQueue;

    @Autowired
    private AtomicBoolean serverLock;

    private AtomicBoolean lock = new AtomicBoolean(true);

    public void start() {
        logger.info("Initialise file parser ...");
        try {
            while (lock.get()) {
                final Path file = directoryQueue.take();
                logger.debug("<-- take file \'{}\' : directoryQueue", file.getFileName());

                parse(file);
            }
        } catch (Exception e) {
            logger.error("Error in file parser", e);
        } finally {
            serverLock.compareAndSet(true, false);
        }
    }

    private void parse(Path file) throws SAXException, ParserConfigurationException, IOException {
        try{
            SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(true);
            factory.setSchema(schemaFactory.newSchema(new StreamSource(getClass().getResourceAsStream("entry.xsd"))));

            XMLReader reader = factory.newSAXParser().getXMLReader();
            reader.parse(new InputSource(Files.newBufferedReader(file, StandardCharsets.UTF_8)));
            successQueue.put(file);
            logger.debug("--> put file \'{}\' : successQueue", file.getFileName());
        } catch (SAXParseException e) {
            Files.move(file, file.resolveSibling(String.format("fail/%s", file.getFileName())));
            logger.debug("-->/--> move file to \'fail/\'", file.getFileName());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        logger.info("Finalization parser ...");
        lock.compareAndSet(true, false);
    }
}
