package ru.shishmakov;

import org.junit.Test;
import org.xml.sax.*;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.InputStream;

/**
 * Check xsd schema and xml files
 *
 * @author Dmitriy Shishmakov
 */
public class XmlValidationTest extends TestBase {

    @Test
    public void xmlShouldBeValidByXsd() throws Exception {
        final InputStream xsd = getClass().getClassLoader().getResourceAsStream("xml/entry.xsd");
        final InputStream xml = getClass().getClassLoader().getResourceAsStream("xml/entry_valid.xml");

        parseXml(xsd, xml);
    }

    @Test
    public void xmlHasEmptyContentAndShouldBeValidByXsd() throws Exception {
        final InputStream xsd = getClass().getClassLoader().getResourceAsStream("xml/entry.xsd");
        final InputStream xml = getClass().getClassLoader().getResourceAsStream("xml/entry_valid_empty_content.xml");

        parseXml(xsd, xml);
    }

    @Test(expected = SAXParseException.class)
    public void xmlShouldNotBeValidByXsd() throws Exception {
        final InputStream xsd = getClass().getClassLoader().getResourceAsStream("xml/entry.xsd");
        final InputStream xml = getClass().getClassLoader().getResourceAsStream("xml/entry_broken.xml");

        parseXml(xsd, xml);
    }

    @Test(expected = SAXParseException.class)
    public void xmlHasIllegalElementsAndShouldNotBeValidByXsd() throws Exception {
        final InputStream xsd = getClass().getClassLoader().getResourceAsStream("xml/entry.xsd");
        final InputStream xml = getClass().getClassLoader().getResourceAsStream("xml/entry_not_valid_elements.xml");

        parseXml(xsd, xml);
    }

    @Test(expected = SAXParseException.class)
    public void xmlHasOnlyDateAndShouldNotBeValidByXsd() throws Exception {
        final InputStream xsd = getClass().getClassLoader().getResourceAsStream("xml/entry.xsd");
        final InputStream xml = getClass().getClassLoader().getResourceAsStream("xml/entry_not_valid_date.xml");

        parseXml(xsd, xml);
    }

    private void parseXml(InputStream xsd, InputStream xml) throws Exception {
        SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);
        factory.setSchema(schemaFactory.newSchema(new StreamSource(xsd)));

        XMLReader reader = factory.newSAXParser().getXMLReader();
        reader.setErrorHandler(new TestErrorHandler());
        reader.parse(new InputSource(xml));
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
