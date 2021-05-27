package org.xdef.transform.xsd.xd2schema.model.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdef.sys.ReportWriter;
import org.xdef.transform.xsd.msg.XSD;
import org.xdef.transform.xsd.xd2schema.model.SchemaFileNameLocationMap;
import org.xdef.transform.xsd.xd2schema.model.SchemaNamespaceLocationMap;
import org.xdef.transform.xsd.xd2schema.util.XsdNamespaceUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.xdef.transform.xsd.util.LoggingUtil.logHeader;
import static org.xdef.transform.xsd.xd2schema.def.AlgPhase.PREPROCESSING;
import static org.xdef.transform.xsd.xd2schema.def.Xd2XsdLogGroup.XSD_ADAPTER_CTX;


/**
 * Key:     schema namespace URI
 * Value:   schema file name location map
 */
public class DefaultSchemaNamespaceLocationMap extends HashMap<String, SchemaFileNameLocationMap> implements SchemaNamespaceLocationMap {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultSchemaNamespaceLocationMap.class);

    /**
     * Output report writer
     */
    final private ReportWriter reportWriter;
    final private String mapName;

    public DefaultSchemaNamespaceLocationMap(ReportWriter reportWriter, String mapName) {
        this.reportWriter = reportWriter;
        this.mapName = mapName;
    }

    @Override
    public XsdSchemaImportLocation addSchemaLocation(final String nsUri, final XsdSchemaImportLocation importLocation) {
        final SchemaFileNameLocationMap importLocations = computeIfAbsent(nsUri,
                key -> new DefaultSchemaFileNameLocationMap()
        );

        final XsdSchemaImportLocation currImportLocation = importLocations.findSchemaLocation(importLocation.getFileName())
                .orElse(null);

        if (currImportLocation != null) {
            if (!currImportLocation.equals(importLocation)) {
                reportWriter.warning(XSD.XSD036, nsUri);
                LOG.warn("{}Schema location already exists for given namespace URI and file name. " +
                        "mapName='{}', namespaceURI='{}', schema='{}'",
                        logHeader(XSD_ADAPTER_CTX), mapName, nsUri, importLocation);
            }

            return currImportLocation;
        }

        LOG.info("{}Add schema location. mapName='{}', schemaName='{}', namespaceURI='{}', path='{}'",
                logHeader(PREPROCESSING), mapName, importLocation.getFileName(), nsUri,
                importLocation.buildLocation(null));

        importLocations.addSchema(importLocation.getFileName(), importLocation);
        return importLocation;
    }

    @Override
    public XsdSchemaImportLocation addSchemaLocation(final String nsPrefix, final String nsUri) {
        final String schemaName = XsdNamespaceUtils.createExtraSchemaNameFromNsPrefix(nsPrefix);
        return addSchemaLocation(nsUri, new XsdSchemaImportLocation(nsUri, schemaName));
    }

    @Override
    public Optional<XsdSchemaImportLocation> findSchemaImport(final String nsUri, final String schemaName) {
        LOG.trace("{}Finding schema by namespace URI and name. mapName='{}', schemaName='{}', namespaceURI='{}'",
                logHeader(XSD_ADAPTER_CTX), mapName, schemaName, nsUri);

        return Optional.ofNullable(get(nsUri))
                .flatMap(schemaFileNameLocationMap -> schemaFileNameLocationMap.findSchemaLocation(schemaName));
    }

    @Override
    public List<XsdSchemaImportLocation> findSchemaImports(final String nsUri) {
        LOG.trace("{}Finding schema by namespace URI. mapName='{}', namespaceURI='{}'",
                logHeader(XSD_ADAPTER_CTX), mapName, nsUri);

        return Optional.ofNullable(get(nsUri))
                .map(schemaFileNameLocationMap -> new ArrayList<>(schemaFileNameLocationMap.getSchemaLocations()))
                .orElse(new ArrayList<>());
    }

    @Override
    public Optional<SchemaFileNameLocationMap> findSchemaFileLocationMap(final String nsUri) {
        return Optional.ofNullable(super.get(nsUri));
    }

    @Override
    public boolean containsSchemaFileLocationMap(final String nsUri) {
        return super.containsKey(nsUri);
    }

}
