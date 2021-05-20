package org.xdef.transform.xsd.xd2schema.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdef.sys.ReportWriter;
import org.xdef.transform.xsd.msg.XSD;
import org.xdef.transform.xsd.xd2schema.util.XsdNamespaceUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.xdef.transform.xsd.util.LoggingUtil.logHeader;
import static org.xdef.transform.xsd.xd2schema.definition.AlgPhase.PREPROCESSING;
import static org.xdef.transform.xsd.xd2schema.definition.Xd2XsdLogGroup.XSD_ADAPTER_CTX;


/**
 * Schema namespace location map
 * Key:     schema namespace URI
 * Value:   schema name location Map
 */
public class SchemaNsLocationMap extends HashMap<String, SchemaNameLocationMap> {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaNsLocationMap.class);

    /**
     * Output report writer
     */
    final private ReportWriter reportWriter;
    final private String name;

    public SchemaNsLocationMap(ReportWriter reportWriter, String name) {
        this.reportWriter = reportWriter;
        this.name = name;
    }

    /**
     * Add XSD document location into map
     * @param nsUri             XSD document namespace URI
     * @param importLocation    XSD document location definition
     */
    public XsdSchemaImportLocation addSchemaLocation(final String nsUri, final XsdSchemaImportLocation importLocation) {
        SchemaNameLocationMap importLocations = get(nsUri);
        if (importLocations == null) {
            importLocations = new SchemaNameLocationMap();
            put(nsUri, importLocations);
        }

        final XsdSchemaImportLocation currImportLocation = importLocations.get(importLocation.getFileName());
        if (currImportLocation != null) {
            if (!currImportLocation.equals(importLocation)) {
                reportWriter.warning(XSD.XSD036, nsUri);
                LOG.warn("{}Schema location already exists for given namespace URI and file name. " +
                        "mapName='{}', namespaceURI='{}', schema='{}'",
                        logHeader(XSD_ADAPTER_CTX), name, nsUri, importLocation);
            }

            return currImportLocation;
        }

        LOG.info("{}Add schema location. mapName='{}', schemaName='{}', namespaceURI='{}', path='{}'",
                logHeader(PREPROCESSING), name, importLocation.getFileName(), nsUri, importLocation.buildLocation(null));
        importLocations.put(importLocation.getFileName(), importLocation);
        return importLocation;
    }

    /**
     * Add XSD document into map. Document location is created internally.
     * @param nsPrefix          XSD document namespace prefix
     * @param nsUri             XSD document namespace URI
     */
    public XsdSchemaImportLocation addSchemaLocation(final String nsPrefix, final String nsUri) {
        final String schemaName = XsdNamespaceUtils.createExtraSchemaNameFromNsPrefix(nsPrefix);
        return addSchemaLocation(nsUri, new XsdSchemaImportLocation(nsUri, schemaName));
    }

    /**
     * Finds XSD document location if exists by given namespace URI
     * @param nsUri     XSD document namespace URI
     * @return XSD document location if exists, otherwise null
     */
    public XsdSchemaImportLocation findSchemaImport(final String nsUri, final String schemaName) {
        LOG.trace("{}Finding schema by namespace URI and name. mapName='{}', schemaName='{}', namespaceURI='{}'",
                logHeader(XSD_ADAPTER_CTX), name, schemaName, nsUri);

        final Map<String, XsdSchemaImportLocation> importLocations = get(nsUri);
        if (importLocations == null || importLocations.isEmpty()) {
            return null;
        }

        return importLocations.get(schemaName);
    }

    public List<XsdSchemaImportLocation> findSchemaImports(final String nsUri) {
        LOG.trace("{}Finding schema by namespace URI. mapName='{}', namespaceURI='{}'",
                logHeader(XSD_ADAPTER_CTX), name, nsUri);
        final Map<String, XsdSchemaImportLocation> importLocations = get(nsUri);
        if (importLocations == null) {
            return new ArrayList<>();
        }

        return new ArrayList<>(importLocations.values());
    }

}
