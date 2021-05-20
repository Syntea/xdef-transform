package org.xdef.transform.xsd.schema2xd.model;


import org.apache.commons.lang3.tuple.Pair;
import org.apache.ws.commons.schema.XmlSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdef.sys.ReportWriter;
import org.xdef.transform.xsd.msg.XSD;
import org.xdef.transform.xsd.schema2xd.definition.Xsd2XdFeature;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.xdef.transform.xsd.schema2xd.definition.Xsd2XdLoggerDefs.XD_ADAPTER_CTX;
import static org.xdef.transform.xsd.util.LoggingUtil.logHeader;
import static org.xdef.transform.xsd.xd2schema.definition.AlgPhase.PREPROCESSING;

/**
 * Basic x-definition context for transformation XSD document to x-definition
 */
public class XdAdapterCtx {

    private static final Logger LOG = LoggerFactory.getLogger(XdAdapterCtx.class);

    /**
     * Enabled algorithm features
     */
    final private Set<Xsd2XdFeature> features;

    /**
     * Target namespaces per x-definition
     * Key:     x-definition name
     * Value:   target namespace prefix, target namespace URI
     */
    private Map<String, Pair<String, String>> targetNamespaces;

    /**
     * All used namespaces per x-definition
     * Key:     x-definition name
     * Value:   target namespace prefix, target namespace URI
     */
    private Map<String, Map<String, String>> xDefNamespaces;

    /**
     * Target namespace URI per x-definition
     * Key:     target namespace URI
     * Value:   x-definition name
     */
    private Map<String, Set<String>> xDefTargetNamespaces;

    /**
     * Input XSD document file names
     * Key:     XML schema
     * Value:   XML schema name
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
        targetNamespaces = new HashMap<String, Pair<String, String>>();
        xDefNamespaces = new HashMap<String, Map<String, String>>();
        xDefTargetNamespaces = new HashMap<String, Set<String>>();
        xsdNames = new HashMap<XmlSchema, String>();
    }

    /**
     * Add target namespace information used by given x-definition to context
     * @param xDefName          X-definition name
     * @param targetNamespace   Target namespace information
     */
    public void addTargetNamespace(final String xDefName, final Pair<String, String> targetNamespace) {
        if (targetNamespaces.containsKey(xDefName)) {
            reportWriter.warning(XSD.XSD217, xDefName);
            LOG.warn("{}X-definition target namespace already exists. xDefinitionName='{}'",
                    logHeader(PREPROCESSING, XD_ADAPTER_CTX), xDefName);
            return;
        }

        LOG.info("{}Add x-definition target namespace. xDefinitionName='{}', targetNsPrefix='{}', targetNsUri='{}'",
                logHeader(PREPROCESSING, XD_ADAPTER_CTX), xDefName, targetNamespace.getLeft(), targetNamespace.getRight());
        targetNamespaces.put(xDefName, targetNamespace);

        Set<String> xDefNames = xDefTargetNamespaces.get(targetNamespace.getValue());
        if (xDefNames == null) {
            xDefNames = new HashSet<String>();
            xDefTargetNamespaces.put(targetNamespace.getValue(), xDefNames);
        }

        xDefNames.add(xDefName);
    }

    /**
     * Finds target namespace used by given x-definition
     * @param xDefName      X-definition name
     * @return target namespace if exists, otherwise null
     */
    public Pair<String, String> findTargetNamespace(final String xDefName) {
        return targetNamespaces.get(xDefName);
    }

    /**
     * Add namespace used by given x-definition to context
     * @param xDefName      X-definition name
     * @param nsPrefix      Namespace prefix
     * @param nsUri         Namespace URI
     */
    public void addNamespace(final String xDefName, final String nsPrefix, final String nsUri) {
        Map<String, String> namespaces = xDefNamespaces.get(xDefName);
        if (namespaces == null) {
            namespaces = new HashMap<String, String>();
            xDefNamespaces.put(xDefName, namespaces);
        }

        if (xDefNamespaces.containsKey(nsUri)) {
            reportWriter.warning(XSD.XSD218, xDefName, nsPrefix);
            LOG.warn("{}X-definition namespace already exists. xDefinitionName='{}', nsPrefix='{}'",
                    logHeader(PREPROCESSING, XD_ADAPTER_CTX), xDefName, nsPrefix);
            return;
        }

        LOG.info("{}Add x-definition namespace. xDefinitionName='{}', nsPrefix='{}', nsUri='{}'",
                logHeader(PREPROCESSING, XD_ADAPTER_CTX), xDefName, nsPrefix, nsUri);
        namespaces.put(nsUri, nsPrefix);
    }

    /**
     * Finds namespaces used by given x-definition
     * @param xDefName      X-definition name
     * @return namespaces if exist, otherwise null
     */
    public Map<String, String> findNamespaces(final String xDefName) {
        return xDefNamespaces.get(xDefName);
    }

    /**
     * Finds namespace prefix used by given x-definition and namespace URI
     * @param xDefName      X-definition name
     * @param nsUri         Namespace URI
     * @return namespace prefix
     */
    public String findNamespacePrefix(final String xDefName, final String nsUri) {
        final Map<String, String> namespaces = xDefNamespaces.get(xDefName);
        if (namespaces == null) {
            return null;
        }

        return namespaces.get(nsUri);
    }

    /**
     * Finds x-definitions by given target namespace URI
     * @param nsUri     Namespace URI
     * @return x-definitions if exist, otherwise null
     */
    public Set<String> findXDefByNamespace(final String nsUri) {
        return xDefTargetNamespaces.get(nsUri);
    }

    /**
     * Add XSD document name to context
     * @param schema        XSD document
     * @param schemaName    XSD document name
     */
    public void addXmlSchemaName(final XmlSchema schema, final String schemaName) {
        LOG.info("{}Add x-definition. name='{}'", logHeader(PREPROCESSING, XD_ADAPTER_CTX), schemaName);
        xsdNames.put(schema, schemaName);
    }

    /**
     * Finds XSD document name by given XSD document
     * @param schema        XSD document
     * @return XSD document name if exists, otherwise null
     */
    public String findXmlSchemaName(final XmlSchema schema) {
        return xsdNames.get(schema);
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
