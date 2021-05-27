package org.xdef.transform.xsd.xd2schema.adapter;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaForm;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.xdef.impl.XDefinition;
import org.xdef.impl.XNode;
import org.xdef.transform.xsd.xd2schema.factory.XsdNodeFactory;
import org.xdef.transform.xsd.xd2schema.model.SchemaFileNameLocationMap;
import org.xdef.transform.xsd.xd2schema.model.SchemaNamespaceLocationMap;
import org.xdef.transform.xsd.xd2schema.model.XsdSchemaImportLocation;
import org.xdef.transform.xsd.xd2schema.util.XsdNamespaceUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static org.xdef.transform.xsd.util.LoggingUtil.HEADER_LINE;
import static org.xdef.transform.xsd.util.LoggingUtil.logHeader;
import static org.xdef.transform.xsd.xd2schema.definition.AlgPhase.POSTPROCESSING;
import static org.xdef.transform.xsd.xd2schema.definition.AlgPhase.PREPROCESSING;
import static org.xdef.transform.xsd.xd2schema.definition.Xd2XsdLogGroup.XSD_XDEF_EXTRA_ADAPTER;

/**
 * Transforms x-definition nodes into xsd nodes
 *
 * Creates new schemas based on post-processing via {@link #transformNodes}
 */
public class Xd2XsdExtraSchemaAdapter extends AbstractXd2XsdAdapter {

    /**
     * Input x-definition used for transformation
     */
    private final XDefinition sourceXDefinition;

    /**
     * Original namespace context used in x-definition {@link #sourceXDefinition}
     */
    private NamespaceMap sourceNamespaceCtx = null;

    protected Xd2XsdExtraSchemaAdapter(XDefinition xDefinition) {
        this.sourceXDefinition = xDefinition;
    }

    /**
     * Set original (x-definition) namespace context
     * @param namespaceCtx
     * @param xsdTargetPrefix
     */
    public void setSourceNamespaceCtx(final NamespaceMap namespaceCtx, final String xsdTargetPrefix) {
        sourceNamespaceCtx = new NamespaceMap((HashMap)namespaceCtx.clone());
        sourceNamespaceCtx.remove(xsdTargetPrefix);
    }

    /**
     * Transform given x-definition nodes {@paramref allNodesToResolve} into XSD nodes and then insert them into related XSD documents
     * @param allNodesToResolve     nodes to be transformed
     * @return All namespaces which have been updated
     */
    protected Set<String> transformNodes(final Map<String, Map<String, XNode>> allNodesToResolve) {
        LOG.info("{}Transforming gathered nodes into extra schemas ...", logHeader(POSTPROCESSING, sourceXDefinition));

        final String sourceSystemId = XsdNamespaceUtils.getSystemIdFromXPosRequired(sourceXDefinition.getXDPosition());
        final Set<String> updatedNamespaces = new HashSet<>();

        SchemaNamespaceLocationMap schemasByNsToResolve = (SchemaNamespaceLocationMap)adapterCtx.getExtraSchemaLocationsCtx().clone();
        int lastSizeMap = schemasByNsToResolve.size();

        while (!schemasByNsToResolve.isEmpty()) {
            Iterator<Map.Entry<String, SchemaFileNameLocationMap>> itrSchemaUri = schemasByNsToResolve.entrySet().iterator();
            while (itrSchemaUri.hasNext()) {
                final Map.Entry<String, SchemaFileNameLocationMap> schemasByNameToResolve = itrSchemaUri.next();
                final String schemaTargetNsUri = schemasByNameToResolve.getKey();
                final SchemaFileNameLocationMap schemaFileNameLocationMap = schemasByNameToResolve.getValue();

                if (updatedNamespaces.contains(schemaTargetNsUri)) {
                    itrSchemaUri.remove();
                    continue;
                }

                if (schemaFileNameLocationMap.isEmpty()) {
                    continue;
                }

                if (schemaFileNameLocationMap.size() > 1) {
                    LOG.warn("{}Multiple schemas with same namespace URI are not currently supported for postprocessing!",
                            logHeader(POSTPROCESSING, sourceXDefinition));
                    continue;
                }

                final XsdSchemaImportLocation importLocation = schemaFileNameLocationMap.getSchemaLocations().stream()
                        .findFirst()
                        .orElseThrow(() ->
                                new RuntimeException("Schema file name location map should contain at least one location"));

                final Map<String, XNode> nodesInSchemaToResolve = allNodesToResolve.get(schemaTargetNsUri);

                if (nodesInSchemaToResolve != null) {
                    // Filter nodes which should be resolved by current x-definition
                    final ArrayList<XNode> nodesToResolve = new ArrayList<>(nodesInSchemaToResolve.values());
                    final Iterator<XNode> itr = nodesToResolve.iterator();
                    XNode n;
                    while (itr.hasNext()) {
                        n = itr.next();
                        if (!sourceSystemId.equals(XsdNamespaceUtils.getSystemIdFromXPosRequired(n.getXDPosition()))) {
                            itr.remove();
                        }
                    }

                    if (!nodesToResolve.isEmpty()) {
                        final SchemaAdapter adapter = new SchemaAdapter(sourceXDefinition);
                        adapter.setAdapterCtx(adapterCtx);
                        adapter.setReportWriter(reportWriter);
                        adapter.createOrUpdateSchema(new NamespaceMap((HashMap) sourceNamespaceCtx.clone()), nodesToResolve, schemaTargetNsUri, importLocation);
                        updatedNamespaces.add(schemaTargetNsUri);
                    }

                    itrSchemaUri.remove();
                }
            }

            int currSchemasToResolve = adapterCtx.getExtraSchemaLocationsCtx().size();
            if (lastSizeMap < currSchemasToResolve) {
                schemasByNsToResolve = (SchemaNamespaceLocationMap)adapterCtx.getExtraSchemaLocationsCtx().clone();
            } else if (lastSizeMap <= schemasByNsToResolve.size()) { // Prevent infinite loop - there is nothing to update
                break;
            }

            lastSizeMap = schemasByNsToResolve.size();
        }

        return updatedNamespaces;
    }

    /**
     * Internal class for transformation of given x-definition nodes into related XSD document
     */
    class SchemaAdapter extends AbstractXd2XsdAdapter {

        /**
         * Input x-definition used for transformation
         */
        private final XDefinition sourceXDefinition;

        /**
         * Output XSD document
         */
        private XmlSchema schema = null;

        protected SchemaAdapter(XDefinition xDefinition) {
            this.sourceXDefinition = xDefinition;
        }

        /**
         * Creates or updates XSD document and then inserts into it given nodes
         * @param namespaceCtx              X-definition namespace context
         * @param nodesInSchemaToResolve    Nodes to be created
         * @param targetNsUri               XSD document target namespace URI
         * @param importLocation            XSD document location
         */
        protected void createOrUpdateSchema(final NamespaceMap namespaceCtx,
                                            final ArrayList<XNode> nodesInSchemaToResolve,
                                            final String targetNsUri,
                                            final XsdSchemaImportLocation importLocation) {
            LOG.info(HEADER_LINE);
            LOG.info("{}Post-processing XSD document. targetNamespaceUri='{}'",
                    logHeader(XSD_XDEF_EXTRA_ADAPTER), targetNsUri);
            LOG.info(HEADER_LINE);

            final String schemaName = createOrGetXsdSchema(namespaceCtx, targetNsUri, importLocation);

            final XsdNodeFactory xsdFactory = new XsdNodeFactory(schema, adapterCtx);
            final Xd2XsdTreeAdapter treeAdapter = new Xd2XsdTreeAdapter(schema, schemaName, xsdFactory, adapterCtx);
            final Xd2XsdReferenceAdapter referenceAdapter = new Xd2XsdReferenceAdapter(schema, schemaName, xsdFactory, treeAdapter, adapterCtx);

            treeAdapter.setPostProcessing();
            referenceAdapter.setPostProcessing();
            referenceAdapter.extractRefsAndImports(nodesInSchemaToResolve);

            transformNodes(treeAdapter, nodesInSchemaToResolve);
        }

        /**
         * Creates or find XSD document. Then initialize XSD document
         *
         * @param namespaceCtx
         * @param targetNsUri
         * @param importLocation
         * @return  instance of xml schema
         */
        private String createOrGetXsdSchema(final NamespaceMap namespaceCtx,
                                            final String targetNsUri,
                                            final XsdSchemaImportLocation importLocation) {
            final String schemaName = importLocation.getFileName();
            if (adapterCtx.existsSchemaLocation(targetNsUri, schemaName)) {
                schema = adapterCtx.findSchema(schemaName, true, POSTPROCESSING);
            } else {
                schema = createOrGetXsdSchema(targetNsUri, schemaName);
                initSchemaNamespace(schemaName, namespaceCtx, targetNsUri, importLocation);
            }

            // TODO: based on top attributes/elements ... if attr -> then only attr qualified?
            initSchemaFormDefault();
            return schemaName;
        }

        /**
         * Creates or find XSD document
         * If schema already exists, return value is reference to already existing schema.
         *
         * @param targetNsUri   target namespace Uri
         * @param schemaName    XSD document name
         * @return instance of xml schema
         */
        private XmlSchema createOrGetXsdSchema(final String targetNsUri, final String schemaName) {
            XmlSchema schema = adapterCtx.findSchema(schemaName, false, POSTPROCESSING);

            if (schema == null) {
                adapterCtx.addSchemaName(schemaName);
                schema = new XmlSchema(targetNsUri, schemaName, adapterCtx.getXmlSchemaCollection());
                LOG.info("{}Initialize new XSD document. schemaName='{}'", logHeader(PREPROCESSING, schemaName), schemaName);
            } else {
                LOG.info("{}Schema already exists. schemaName='{}'", logHeader(PREPROCESSING, schemaName), schemaName);
            }

            return schema;
        }

        /**
         * Initializes XSD document namespace
         *
         * If schema namespace context already exist, then merge it with {@paramref namespaceCtx)
         *
         * @param schemaName        XSD document name
         * @param namespaceCtx      current x-definition namespace context
         * @param targetNsUri       XSD document target namespace URI
         * @param importLocation    XSD document location
         * @return
         */
        private void initSchemaNamespace(final String schemaName,
                                         final NamespaceMap namespaceCtx,
                                         final String targetNsUri,
                                         final XsdSchemaImportLocation importLocation) {
            LOG.debug("{}Initializing namespace context ...", logHeader(POSTPROCESSING, sourceXDefinition));

            // Namespace initialization
            final String targetNsPrefix = XsdNamespaceUtils.getNsPrefixFromExtraSchemaName(importLocation.getFileName());
            XsdNamespaceUtils.addNamespaceToCtx(namespaceCtx, targetNsPrefix, targetNsUri, schemaName, POSTPROCESSING);
            schema.setSchemaNamespacePrefix(targetNsPrefix);

            NamespaceMap currNamespaceCtx = (NamespaceMap)schema.getNamespaceContext();
            // Schema has already namespace context -> merge it
            if (currNamespaceCtx != null) {
                currNamespaceCtx.putAll(namespaceCtx);
                schema.setNamespaceContext(currNamespaceCtx);
            } else {
                schema.setNamespaceContext(namespaceCtx);
            }
        }

        /**
         * Sets attributeFormDefault and elementFormDefault
         */
        private void initSchemaFormDefault() {
            schema.setElementFormDefault(XmlSchemaForm.QUALIFIED);
            schema.setAttributeFormDefault(XmlSchemaForm.QUALIFIED);
        }

        /**
         * Transforms given x-definition nodes into org.xdef.transform.xsd elements and insert them into {@link #schema}
         * @param treeAdapter       transformation algorithm
         * @param nodes             source nodes to transform
         */
        private void transformNodes(final Xd2XsdTreeAdapter treeAdapter, final ArrayList<XNode> nodes) {
            LOG.info(HEADER_LINE);
            LOG.info("{}Transformation of x-definition tree to schema", logHeader(XSD_XDEF_EXTRA_ADAPTER));
            LOG.info(HEADER_LINE);

            for (XNode node : nodes) {
                final XmlSchemaObject xsdNode = treeAdapter.convertTree(node).orElse(null);
                if (xsdNode instanceof XmlSchemaElement) {
                    LOG.info("{}Add top-level element.", logHeader(POSTPROCESSING, node));
                } else if (xsdNode instanceof XmlSchemaAttribute) {
                    LOG.info("{}Add top-level attribute.", logHeader(POSTPROCESSING, node));
                }
            }
        }

    }
}
