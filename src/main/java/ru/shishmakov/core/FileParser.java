package ru.shishmakov.core;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import ru.shishmakov.entity.Entry;
import ru.shishmakov.util.EntryWrapper;
import ru.shishmakov.util.SymlinkLoopUtil;

import javax.annotation.Resource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
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
 * Service detects content of files.
 *
 * @author Dmitriy Shishmakov
 */
public class FileParser {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final AtomicInteger quantity = new AtomicInteger(0);
    private static final String schema = "xml/entry.xsd";
    private static final DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-DD HH:mm:ss");

    @Resource(name = "directoryQueue")
    private BlockingQueue<Path> directoryQueue;

    @Resource(name = "successQueue")
    private BlockingQueue<EntryWrapper> successQueue;

    @Autowired
    private AtomicBoolean serverLock;

    private final int number = quantity.incrementAndGet();

    private AtomicBoolean lock = new AtomicBoolean(true);

    public void start() {
        logger.info("Initialise file parser {} ...", number);
        try {
            while (lock.get()) {
                final Path file = directoryQueue.poll(200, TimeUnit.MILLISECONDS);
                if (file == null) {
                    continue;
                }
                logger.info("<-- take file \'{}\' : directoryQueue", file.getFileName());

                if (isReadyToParse(file)) {
                    parse(file);
                    continue;
                }
                final String description = String.format("symlink loop: %s; readable: %s; file: %s",
                        SymlinkLoopUtil.isSymbolicLinkLoop(file), Files.isReadable(file), Files.isRegularFile(file));
                moveToFailDirectory(file, description);
            }
        } catch (Exception e) {
            logger.error("Error in file parser", e);
        } finally {
            serverLock.compareAndSet(true, false);
        }
    }

    public void stop() {
        logger.info("Finalization parser {} ...", number);
        lock.compareAndSet(true, false);
    }

    private boolean isReadyToParse(Path file) {
        return Files.isRegularFile(file) && Files.isReadable(file) && SymlinkLoopUtil.isNotSymbolicLinkLoop(file);
    }

    private void parse(Path file) throws InterruptedException {
        try {
            final InputStream xsd = getClass().getClassLoader().getResourceAsStream(schema);
            final SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
            final SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(true);
            factory.setSchema(schemaFactory.newSchema(new StreamSource(xsd)));

            final XMLReader reader = factory.newSAXParser().getXMLReader();
            reader.setErrorHandler(new TestErrorHandler());
            reader.parse(new InputSource(Files.newBufferedReader(file, StandardCharsets.UTF_8)));
            final Entry entry = buildTransientEntry(factory, file);
            moveToNextQueue(EntryWrapper.build(file, entry));
        } catch (SAXException | ParserConfigurationException | IOException e) {
            moveToFailDirectory(file, "schema is not valid");
        }
    }

    private Entry buildTransientEntry(SAXParserFactory factory, Path file) throws
            ParserConfigurationException, SAXException, IOException {
        final Entry entry = new Entry();
        final SAXParser parser = factory.newSAXParser();
        parser.parse(Files.newInputStream(file), new ParserHandler(file, entry));
        return entry;
    }

    private void moveToNextQueue(EntryWrapper boat) throws InterruptedException {
        while (lock.get()) {
            if (!successQueue.offer(boat, 200, TimeUnit.MILLISECONDS)) {
                continue;
            }
            logger.info("--> put transient entity and file '{}' : successQueue", boat);
            break;
        }
    }

    private void moveToFailDirectory(Path source, String description) {
        logger.warn("!!! File: \'{}\' not processed; {}", source, description);
        try {
            final Path target = source.resolveSibling(Paths.get("fail", source.getFileName().toString()));
            Files.move(source, target, REPLACE_EXISTING, ATOMIC_MOVE);
            logger.info("move file \'{}\' to directory \'{}\'", source.getFileName(), target.getParent());
        } catch (IOException e) {
            logger.error("File move error", e);
        }
    }

    private class ParserHandler extends DefaultHandler {
        private final Path file;
        private final Entry entry;
        private String element;

        public ParserHandler(Path file, Entry entry) {
            this.file = file;
            this.entry = entry;
        }

        @Override
        public void startDocument() throws SAXException {
            logger.debug("Start parse XML: \'{}\' ...", file);
        }

        @Override
        public void endDocument() throws SAXException {
            logger.debug("Stop parse XML: \'{}\' ...", file);
        }

        @Override
        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
            element = qName;
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (element.equals("content")) {
                entry.setContent(new String(ch, start, length));
            }
            if (element.equals("creationDate")) {
                LocalDateTime localDateTime = formatter.parseLocalDateTime(new String(ch, start, length));
                entry.setCreationDate(localDateTime);
            }
        }
    }

    private class TestErrorHandler implements ErrorHandler {
        @Override
        public void warning(SAXParseException e) throws SAXException {
            throw e;
        }

        @Override
        public void error(SAXParseException e) throws SAXException {
            throw e;
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            throw e;
        }
    }
}
