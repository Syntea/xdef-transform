package org.xdef.transform.xsd.xd2schema.adapter;


import org.apache.commons.lang3.tuple.Pair;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaIdentityConstraint;
import org.apache.ws.commons.schema.XmlSchemaKey;
import org.apache.ws.commons.schema.XmlSchemaKeyref;
import org.apache.ws.commons.schema.XmlSchemaUnique;
import org.apache.ws.commons.schema.XmlSchemaXPath;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.xdef.XDPool;
import org.xdef.impl.XDefinition;
import org.xdef.model.XMDefinition;
import org.xdef.transform.xsd.msg.XSD;
import org.xdef.transform.xsd.xd2schema.definition.Xd2XsdFeature;
import org.xdef.transform.xsd.xd2schema.factory.XsdNodeFactory;
import org.xdef.transform.xsd.xd2schema.model.SchemaNode;
import org.xdef.transform.xsd.xd2schema.model.uc.UniqueConstraint;
import org.xdef.transform.xsd.xd2schema.model.uc.UniqueConstraintAttributeList;
import org.xdef.transform.xsd.xd2schema.model.uc.UniqueConstraintNodePathMap;
import org.xdef.transform.xsd.xd2schema.model.uc.UniqueConstraintVariableMap;
import org.xdef.transform.xsd.xd2schema.util.Xd2XsdUtils;
import org.xdef.transform.xsd.xd2schema.util.XsdNameUtils;
import org.xdef.transform.xsd.xd2schema.util.XsdPostProcessor;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.xdef.transform.xsd.util.LoggingUtil.HEADER_LINE;
import static org.xdef.transform.xsd.util.LoggingUtil.logHeader;
import static org.xdef.transform.xsd.xd2schema.definition.AlgPhase.POSTPROCESSING;
import static org.xdef.transform.xsd.xd2schema.definition.Xd2XsdLogGroup.XSD_PP_ADAPTER;

/**
 * Makes post processing transformation on created XSD documents
 */
public class Xd2XsdPostProcessingAdapter extends AbstractXd2XsdAdapter {

    /**
     * Set of post processing algorithms
     */
    private XsdPostProcessor postProcessor;

    /**
     * Run post processing on pool of x-definitions
     * Post processing features can be enabled by calling {@link #setFeatures(Set)} or {@link #addFeature(Xd2XsdFeature)}
     *
     * @param xdPool    pool of x-definitions to be processed
     */
    public void process(final XDPool xdPool) {
        if (!adapterCtx.hasEnableFeature(Xd2XsdFeature.POSTPROCESSING)) {
            return;
        }

        LOG.info(HEADER_LINE);
        LOG.info("{}Post-processing XDPool", logHeader(POSTPROCESSING, XSD_PP_ADAPTER));
        LOG.info(HEADER_LINE);

        postProcessor = new XsdPostProcessor(adapterCtx);
        final Set<String> updatedNamespaces = new HashSet<String>();
        processNodes(xdPool, updatedNamespaces);
        processReferences();
        processQNames(updatedNamespaces);
        createKeysAndRefs(xdPool);
    }

    /**
     * Run post processing on x-definition
     * Post processing features can be enabled by calling {@link #setFeatures(Set)} or {@link #addFeature(Xd2XsdFeature)}
     *
     * @param xDef  x-definition to be processed
     */
    public void process(final XDefinition xDef) {
        if (!adapterCtx.hasEnableFeature(Xd2XsdFeature.POSTPROCESSING)) {
            return;
        }

        LOG.info(HEADER_LINE);
        LOG.info("{}Post-processing XDefinition", logHeader(POSTPROCESSING, XSD_PP_ADAPTER));
        LOG.info(HEADER_LINE);

        postProcessor = new XsdPostProcessor(adapterCtx);
        final Set<String> updatedNamespaces = new HashSet<String>();
        if (!adapterCtx.getNodesToBePostProcessed().isEmpty() && !adapterCtx.getExtraSchemaLocationsCtx().isEmpty()) {
            processNodes(xDef, updatedNamespaces);
        }
        processReferences();
        processQNames(updatedNamespaces);
        createKeysAndRefs(xDef);
    }

    /**
     * Run post processing on x-definition pool
     * @param xdPool                x-definition pool to be processed
     * @param updatedNamespaces     already processed namespaces
     */
    private void processNodes(final XDPool xdPool, final Set<String> updatedNamespaces) {
        if (!adapterCtx.getNodesToBePostProcessed().isEmpty() && !adapterCtx.getExtraSchemaLocationsCtx().isEmpty()) {
            for (XMDefinition xDef : xdPool.getXMDefinitions()) {
                processNodes((XDefinition)xDef, updatedNamespaces);
            }
        }
    }

    /**
     * Creates XSD nodes which are originally located in different x-definition namespace
     * @param xDef                  x-definition source of XSD nodes
     * @param updatedNamespaces     already processed namespaces
     */
    private void processNodes(final XDefinition xDef, final Set<String> updatedNamespaces) {
        if (!adapterCtx.hasEnableFeature(Xd2XsdFeature.POSTPROCESSING_EXTRA_SCHEMAS)) {
            return;
        }

        LOG.info("{}Creating nodes ...", logHeader(POSTPROCESSING, xDef));
        final XmlSchema schema = adapterCtx.findSchema(XsdNameUtils.getSchemaName(xDef), true, POSTPROCESSING);
        final Xd2XsdExtraSchemaAdapter postProcessingAdapter = new Xd2XsdExtraSchemaAdapter(xDef);
        postProcessingAdapter.setAdapterCtx(adapterCtx);
        postProcessingAdapter.setReportWriter(reportWriter);
        postProcessingAdapter.setSourceNamespaceCtx((NamespaceMap) schema.getNamespaceContext(), schema.getSchemaNamespacePrefix());
        updatedNamespaces.addAll(postProcessingAdapter.transformNodes(adapterCtx.getNodesToBePostProcessed()));
    }

    /**
     * Updates XSD references which are breaking XSD document rules
     */
    private void processReferences() {
        if (!adapterCtx.hasEnableFeature(Xd2XsdFeature.POSTPROCESSING_REFS)) {
            return;
        }

        postProcessor.processRefs();
    }

    /**
     * Updates XSD attributes and elements QNames of schemas created by post processing
     * @param updatedNamespaces
     */
    private void processQNames(final Set<String> updatedNamespaces) {
        LOG.info("{}Processing qualified names ...", logHeader(POSTPROCESSING, XSD_PP_ADAPTER));
        for (String schemaNs : updatedNamespaces) {

            final Set<String> schemaNames = adapterCtx.findSchemaNamesByNamespace(schemaNs, true, POSTPROCESSING);

            for (String schemaName : schemaNames) {
                if (adapterCtx.isPostProcessingNamespace(schemaNs) && !adapterCtx.existsSchemaLocation(schemaNs, schemaName)) {
                    continue;
                }

                final XmlSchema schema = adapterCtx.findSchema(schemaName, true, POSTPROCESSING);
                final Map<String, SchemaNode> nodes = adapterCtx.getNodes().get(schemaName);
                if (nodes != null && !nodes.isEmpty()) {
                    for (SchemaNode n : nodes.values()) {
                        if (n.isXsdAttr()) {
                            XsdNameUtils.resolveAttributeQName(schema, n.toXsdAttr(), n.getXdName());
                            XsdNameUtils.resolveAttributeSchemaTypeQName(schema, n.toXsdAttr());
                        } else if (n.isXsdElem()) {
                            XsdNameUtils.resolveElementQName(schema, n.toXdElem(), n.toXsdElem(), adapterCtx);
                            XsdNameUtils.resolveElementSchemaTypeQName(schema, n.toXsdElem());
                        }
                    }
                }
            }
        }
    }

    /**
     * Creates unique constraints (xs:unique xs:key, xs:keyref) nodes based on gathered uniqueSet data from x-definition pool
     * @param xdPool    x-definition pool
     */
    private void createKeysAndRefs(final XDPool xdPool) {
        for (XMDefinition xDef : xdPool.getXMDefinitions()) {
            createKeysAndRefs((XDefinition)xDef);
        }
    }

    /**
     * Creates unique constraints (xs:unique xs:key, xs:keyref) nodes based on gathered uniqueSet data from x-definition
     * @param xDef    input x-definition where XSD elements should be inserted
     */
    private void createKeysAndRefs(final XDefinition xDef) {
        if (!adapterCtx.hasEnableFeature(Xd2XsdFeature.POSTPROCESSING_UNIQUE)) {
            return;
        }

        LOG.info("{}Processing unique constraints ...", logHeader(POSTPROCESSING, XSD_PP_ADAPTER));

        createRestrictionConstraints(xDef.getName(), "");
        createRestrictionConstraints(xDef.getName(), xDef.getName());
    }

    /**
     * Creates unique constraints xs:unique xs:key, xs:keyref) nodes based on gathered uniqueSet data from x-definition
     *
     * If constraint has no keyref or only unsupported keyref, then xs:unique is created instead of xs:key
     *
     * @param xDefName      x-definition name
     * @param systemId      source system id of unique set (empty for unique sets places in root of x-definition)
     */
    private void createRestrictionConstraints(final String xDefName, final String systemId) {
        final Map<String, List<UniqueConstraint>> uniqueInfoMap = adapterCtx.getSchemaUniqueConstraints(systemId);
        if (uniqueInfoMap == null || uniqueInfoMap.isEmpty()) {
            return;
        }

        LOG.info("{}Creating unique constraints for x-definition. xDefName='{}', systemId='{}'",
                logHeader(POSTPROCESSING, XSD_PP_ADAPTER), xDefName, systemId);

        int i = 0;
        int j = 0;

        for (Map.Entry<String, List<UniqueConstraint>> uniqueInfoEntry : uniqueInfoMap.entrySet()) {
            final List<UniqueConstraint> uniqueInfoList = uniqueInfoEntry.getValue();
            if (uniqueInfoList == null || uniqueInfoList.isEmpty()) {
                continue;
            }

            for (UniqueConstraint uniqueConstraint : uniqueInfoList) {
                LOG.info("{}Creating unique constraint. uniqueSetPath='{}'",
                        logHeader(POSTPROCESSING, XSD_PP_ADAPTER), uniqueConstraint.getPath());

                final UniqueConstraintVariableMap variableKeyMap = uniqueConstraint.getVariableKeyMap();
                final UniqueConstraintVariableMap variableRefMap = uniqueConstraint.getVariableRefMap();

                if (variableKeyMap.isEmpty()) {
                    LOG.debug("{}Unique constraint has no keys. uniqueSetPath='{}'",
                            logHeader(POSTPROCESSING, XSD_PP_ADAPTER), uniqueConstraint.getPath());
                    continue;
                }

                final Set<Map.Entry<String, UniqueConstraintNodePathMap>> varKeyEntrySet = variableKeyMap.entrySet();
                for (Map.Entry<String, UniqueConstraintNodePathMap> varKeyEntry : varKeyEntrySet) {
                    final String varName = varKeyEntry.getKey();
                    final UniqueConstraintNodePathMap keyNodePathMap = varKeyEntry.getValue();

                    if (keyNodePathMap.isEmpty()) {
                        LOG.info("{}Unique set has no key for variable. uniqueSetPath='{}', variableName='{}'",
                                logHeader(POSTPROCESSING, XSD_PP_ADAPTER), uniqueConstraint.getPath(), varName);
                        continue;
                    }

                    if (keyNodePathMap.size() > 1) {
                        LOG.info("{}Unique set variable has key in different XPaths - not supported yet. " +
                                "uniqueSetPath='{}', variableName='{}'",
                                logHeader(POSTPROCESSING, XSD_PP_ADAPTER), uniqueConstraint.getPath(), varName);
                        continue;
                    }

                    final Optional<UniqueConstraintNodePathMap> refNodePathMap = variableRefMap.findNodePathMap(varName);

                    final Set<Map.Entry<String, UniqueConstraintAttributeList>> keyAttrEntrySet = keyNodePathMap.entrySet();
                    for (Map.Entry<String, UniqueConstraintAttributeList> keyAttrEntry : keyAttrEntrySet) {
                        final String keyNodeXPath = keyAttrEntry.getKey();
                        final UniqueConstraintAttributeList keyAttrList = keyAttrEntry.getValue();

                        if (keyAttrList.size() > 1) {
                            LOG.info("{}Unique set variable is used in multiple attributes - not supported yet. " +
                                    "uniqueSetPath='{}', variableName='{}'",
                                    logHeader(POSTPROCESSING, XSD_PP_ADAPTER), uniqueConstraint.getPath(), varName);
                            continue;
                        }

                        final String xPathParentNode = getParentNodePath(uniqueInfoEntry.getKey(), keyNodeXPath);
                        final SchemaNode rootSchemaNode = adapterCtx.findSchemaNode(xDefName, xPathParentNode);

                        if (rootSchemaNode == null) {
                            reportWriter.warning(XSD.XSD015, uniqueConstraint.getPath(), xPathParentNode);
                            LOG.warn("{}Root node of unique set has not been found! uniqueSetPath='{}', xPath='{}'",
                                    logHeader(POSTPROCESSING, XSD_PP_ADAPTER), uniqueConstraint.getPath(), xPathParentNode);
                        } else if (!rootSchemaNode.isXsdElem()) {
                            reportWriter.warning(XSD.XSD015, uniqueConstraint.getPath(), xPathParentNode);
                            LOG.warn("{}Root node of unique set is not element! uniqueSetPath='{}', xPath='{}'",
                                    logHeader(POSTPROCESSING, XSD_PP_ADAPTER), uniqueConstraint.getPath(), xPathParentNode);
                        } else {
                            LOG.debug("{}Creating key/unique for unique set. uniqueSetPath='{}', variableName='{}'",
                                    logHeader(POSTPROCESSING, XSD_PP_ADAPTER), uniqueConstraint.getPath(), varName);

                            final XmlSchemaElement rootElem = rootSchemaNode.toXsdElem();
                            final String keyFieldPath = buildFieldXPath(keyAttrList);
                            final boolean createUnique = !refNodePathMap.isPresent() || refNodePathMap.get().isEmpty();
                            final String keyName = "key_" + uniqueConstraint.getName() + "_" + i;

                            LOG.debug("{}Unique set - key/unique. xPath='{}', xPathParent='{}'",
                                    logHeader(POSTPROCESSING, XSD_PP_ADAPTER), keyNodeXPath, xPathParentNode);

                            final XmlSchemaIdentityConstraint identityConstraint = createUnique
                                    ? new XmlSchemaUnique()
                                    : new XmlSchemaKey();
                            identityConstraint.setName(keyName);
                            addConstraintInfo(
                                    identityConstraint,
                                    Xd2XsdUtils.relativeXPath(keyNodeXPath, xPathParentNode),
                                    keyFieldPath);

                            if (uniqueInfoEntry.getKey().isEmpty() && systemId.isEmpty()) {
                                identityConstraint.setAnnotation(XsdNodeFactory.createAnnotation(
                                        "Unique set was placed in global declaration inside x-definition",
                                        adapterCtx));
                            }

                            rootElem.getConstraints().add(identityConstraint);

                            if (!refNodePathMap.isPresent()) {
                                i++;
                                continue;
                            }

                            final Set<Map.Entry<String, UniqueConstraintAttributeList>> refAttrEntrySet = refNodePathMap.get().entrySet();
                            for (Map.Entry<String, UniqueConstraintAttributeList> refAttrEntry : refAttrEntrySet) {
                                final String refNodeXPath = refAttrEntry.getKey();
                                final UniqueConstraintAttributeList refAttrList = refAttrEntry.getValue();

                                LOG.debug("{}Creating keyref for unique set. uniqueSetPath='{}', variableName='{}'",
                                        logHeader(POSTPROCESSING, XSD_PP_ADAPTER), uniqueConstraint.getPath(), varName);

                                for (Pair<String, XmlSchemaAttribute> varRef : refAttrList.values()) {
                                    final XmlSchemaKeyref ref = new XmlSchemaKeyref();
                                    ref.setName("ref_" + uniqueConstraint.getName() + "_" + i + "_" + j);
                                    ref.setRefer(new QName(keyName));
                                    addConstraintInfo(
                                            ref,
                                            Xd2XsdUtils.relativeXPath(refNodeXPath, xPathParentNode),
                                            "@" + varRef.getKey());

                                    LOG.debug("{}Unique set - keyref. xPath='{}', xPathParent='{}'",
                                            logHeader(POSTPROCESSING, XSD_PP_ADAPTER), refNodeXPath, xPathParentNode);

                                    rootElem.getConstraints().add(ref);
                                    j++;
                                }
                            }

                            i++;
                        }
                    }
                }
            }
        }
    }

    /**
     * Get parent node of restriction constraint node (xs:selector value)
     * @param uniquePath    unique set path
     * @param xPath         xPath of constraint node
     * @return xPath of parent node of restriction constraints
     */
    private String getParentNodePath(final String uniquePath, final String xPath) {
        String res;
        if (!uniquePath.isEmpty()) {
            int splitPos = xPath.lastIndexOf('/');
            if (splitPos != -1) {
                res = xPath.substring(0, splitPos);
            } else {
                res = xPath;
            }
        } else {
            int splitPos = xPath.indexOf('/');
            if (splitPos != -1) {
                res = xPath.substring(0, splitPos);
            } else {
                res = xPath;
            }
        }

        return res;
    }

    /**
     * Creates xPath xs:field value for identity constraint
     * @param attributeList     XPath of restriction constrains nodes
     * @return xPath for xs:field
     */
    private String buildFieldXPath(final UniqueConstraintAttributeList attributeList) {
        final Set<String> fieldXPathSet = new HashSet<>();

        for (Pair<String, XmlSchemaAttribute> keyPath : attributeList.values()) {
            fieldXPathSet.add("@" + keyPath.getKey());
        }

        Iterator<String> keyPathItr = fieldXPathSet.iterator();
        final StringBuilder pathStringBuilder = new StringBuilder();
        pathStringBuilder.append(keyPathItr.next());
        while (keyPathItr.hasNext()) {
            pathStringBuilder.append("|");
            pathStringBuilder.append(keyPathItr.next());
        }

        return pathStringBuilder.toString();
    }

    /**
     * Creates, initializes and inserts xs:selector and xs:field nodes into constraint node
     * @param identityConstraint    Constraint node where created nodes will be placed
     * @param selectorXPath         xs:selector xpath value
     * @param fieldXPath            xs:field xpath value
     */
    private void addConstraintInfo(final XmlSchemaIdentityConstraint identityConstraint, final String selectorXPath, final String fieldXPath) {
        final XmlSchemaXPath xPathSelectorRef = new XmlSchemaXPath();
        final XmlSchemaXPath xPathFieldRef = new XmlSchemaXPath();

        identityConstraint.setSelector(xPathSelectorRef);
        identityConstraint.getFields().add(xPathFieldRef);

        xPathSelectorRef.setXPath(selectorXPath);
        xPathFieldRef.setXPath(fieldXPath);
    }
}