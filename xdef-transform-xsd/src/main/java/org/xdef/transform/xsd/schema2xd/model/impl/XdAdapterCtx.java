package org.xdef.transform.xsd.schema2xd.model.impl;


import org.apache.ws.commons.schema.XmlSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdef.sys.ReportWriter;
import org.xdef.transform.xsd.model.Namespace;
import org.xdef.transform.xsd.msg.XSD;
import org.xdef.transform.xsd.schema2xd.def.Xsd2XdFeature;
import org.xdef.transform.xsd.schema2xd.error.XdAdapterCtxException;
import org.xdef.transform.xsd.schema2xd.model.NamespaceMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.xdef.transform.xsd.schema2xd.def.Xsd2XdLoggerDefs.XD_ADAPTER_CTX;
import static org.xdef.transform.xsd.util.LoggingUtil.logHeader;
import static org.xdef.transform.xsd.xd2schema.def.AlgPhase.PREPROCESSING;

/**
 * Basic X-Definition context for transformation XML Schema document to X-Definition
 */
public class XdAdapterCtx {

    private static final Logger LOG = LoggerFactory.getLogger(XdAdapterCtx.class);

    /**
     * Enabled algorithm features
     */
    final private Set<Xsd2XdFeature> features;

    /**
     * Target namespaces per X-Definition
     * Key:     X-Definition name
     * Value:   target namespace
     */
    private Map<String, Namespace> targetNamespaces;

    /**
     * All used namespaces per X-Definition
     * Key:     X-Definition name
     * Value:   namespace map
     */
    private Map<String, NamespaceMap> xDefNamespaces;

    /**
     * Target namespace URI per X-Definition
     * Key:     target namespace URI
     * Value:   set of X-Definition names
     */
    private Map<String, Set<String>> xDefTargetNamespaces;

    /**
     * Input XML Schema document file names
     * Key:     XML Schema
     * Value:   XML Schema file name
     */
    private Map<XmlSchema, String> xsdNames;

    /**
     * Output report writer
     */
    final private ReportWriter reportWriter;

    public XdAdapterCtx(Set<Xsd2XdFeature> features, ReportWriter reportWriter) {
        this.features = features;
        this.reportWriter = reportWriter;
    }

    public ReportWriter getReportWriter() {
        return reportWriter;
    }

    /**
     * Initializes XD adapter context
     */
    public void init() {
        targetNamespaces = new HashMap<>();
        xDefNamespaces = new HashMap<>();
        xDefTargetNamespaces = new HashMap<>();
        xsdNames = new HashMap<>();
    }

    /**
     * Add target namespace information used by given X-Definition to context
     * @param xDefName          X-definition name
     * @param targetNamespace   Target namespace information
     */
    public void addTargetNamespace(final String xDefName, final Namespace targetNamespace) {
        if (targetNamespaces.containsKey(xDefName)) {
            reportWriter.warning(XSD.XSD217, xDefName);
            LOG.warn("{}X-definition target namespace already registered. xDefinitionName='{}'",
                    logHeader(PREPROCESSING, XD_ADAPTER_CTX), xDefName);
            return;
        }

        LOG.info("{}Add X-Definition target namespace. xDefinitionName='{}', targetNsPrefix='{}', targetNsUri='{}'",
                logHeader(PREPROCESSING, XD_ADAPTER_CTX), xDefName, targetNamespace.getPrefix(), targetNamespace.getUri());
        targetNamespaces.put(xDefName, targetNamespace);

        final Set<String> xDefNames = xDefTargetNamespaces.computeIfAbsent(
                targetNamespace.getUri(),
                key -> new HashSet<>());

        xDefNames.add(xDefName);
    }

    /**
     * Finds target namespace used by given X-Definition
     * @param xDefName      X-definition name
     * @return  namespace if found
     *          otherwise {@link Optional#empty()}
     */
    public Optional<Namespace> findTargetNamespace(final String xDefName) {
        return Optional.ofNullable(targetNamespaces.get(xDefName));
    }

    /**
     * Add namespace used by given X-Definition to context
     * @param xDefName      X-definition name
     * @param nsPrefix      Namespace prefix
     * @param nsUri         Namespace URI
     */
    public void addNamespace(final String xDefName, final String nsPrefix, final String nsUri) {
        final NamespaceMap namespaceMap = xDefNamespaces.computeIfAbsent(xDefName, key -> new DefaultNamespaceMap(reportWriter));
        namespaceMap.add(nsPrefix, nsUri, xDefName);
    }

    /**
     * Finds namespaces used by given X-Definition
     * @param xDefName      X-definition name
     * @return  namespace map if found
     *          otherwise {@link Optional#empty()}
     */
    public Optional<NamespaceMap> findNamespaces(final String xDefName) {
        return Optional.ofNullable(xDefNamespaces.get(xDefName));
    }

    /**
     * Finds namespace prefix used by given X-Definition and namespace URI
     * @param xDefName      X-definition name
     * @param nsUri         Namespace URI
     * @return namespace prefix
     */
    public Optional<String> findNamespacePrefix(final String xDefName, final String nsUri) {
        final Optional<NamespaceMap> namespaceMap = Optional.ofNullable(xDefNamespaces.get(xDefName));
        return namespaceMap.flatMap(namespaces -> namespaces.findByUri(nsUri));
    }

    /**
     * Finds x-definitions by given target namespace URI
     * @param nsUri     Namespace URI
     * @return x-definitions names. If not found, the result will be empty set.
     */
    public Set<String> findXDefByNamespace(final String nsUri) {
        return Optional.ofNullable(xDefTargetNamespaces.get(nsUri))
                .orElse(Collections.emptySet());
    }

    /**
     * Add XML Schema document name to context
     * @param schema            XML Schema document
     * @param schemaFileName    XML Schema file name
     */
    public void addXmlSchemaFileName(final XmlSchema schema, final String schemaFileName) {
        LOG.info("{}Add X-Definition. name='{}'", logHeader(PREPROCESSING, XD_ADAPTER_CTX), schemaFileName);
        xsdNames.put(schema, schemaFileName);
    }

    /**
     * Finds XML Schema document name by given XML Schema document
     * @param schema        XML Schema document
     * @return XML Schema document name if exists
     */
    public String findXmlSchemaFileName(final XmlSchema schema) {
        return Optional.ofNullable(xsdNames.get(schema))
                .orElseThrow(() -> new XdAdapterCtxException("Required XML Schema file not found."));
    }

    /**
     * Check if algorithm feature is enabled
     * @param feature       algorithm feature
     * @return  true if algorithm feature is enabled
     */
    public boolean hasEnableFeature(final Xsd2XdFeature feature) {
        return features.contains(feature);
    }
}
