package org.xdef.transform.xsd.util;

import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;

/**
 * Simple XML validator used only for testing purposes
 */
public class XmlValidator {

    private final Source xmlDataSource;
    private final Source xmlSchemaSource;

    public XmlValidator(Source xmlDataSource, Source xmlSchemaSource) {
        this.xmlDataSource = xmlDataSource;
        this.xmlSchemaSource = xmlSchemaSource;
    }

    public boolean validate() {
        if (xmlDataSource == null || xmlSchemaSource == null) {
            throw new IllegalStateException("xmlDataSource == null || xmlSchemaSource == null");
        }

        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        try {
            Schema schema = schemaFactory.newSchema(xmlSchemaSource);

            Validator validator = schema.newValidator();
            validator.validate(xmlDataSource);
            return true;
        } catch (SAXException | IOException ex) {
            throw new RuntimeException("Error occurs while validating XML Schema", ex);
        }
    }
}
