package org.xdef.transform.xsd.console.impl;

import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.xdef.transform.xsd.error.FormattedRuntimeException;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * @author smid
 * @since 2021-05-13
 */
public class XDefTransformResult {

    /**
     * Created XML Schema collection
     */
    private XmlSchemaCollection xmlSchemaCollection;

    /**
     * Transformed X-definition(s)
     *
     * Key: XML Schema name
     * Right: Path to output XML Schema file
     */
    private final Map<String, Path> outputSchemaMap = new HashMap<>();

    public XmlSchemaCollection getXmlSchemaCollection() {
        return xmlSchemaCollection;
    }

    public XDefTransformResult setXmlSchemaCollection(XmlSchemaCollection xmlSchemaCollection) {
        this.xmlSchemaCollection = xmlSchemaCollection;
        return this;
    }

    public void addOutputSchema(String schemaName, Path xsdPath) {
        outputSchemaMap.computeIfPresent(schemaName, (key, val) -> {
            throw new FormattedRuntimeException("Output XML Schema already exist for given name. " +
                    "schemaName='{}', currXsdPath='{}', newXsdPath='{}'",
                    key, val.toAbsolutePath(), xsdPath.toAbsolutePath());
        });

        outputSchemaMap.put(schemaName, xsdPath);
    }

    public Map<String, Path> getOutputSchemaMap() {
        return outputSchemaMap;
    }
}
