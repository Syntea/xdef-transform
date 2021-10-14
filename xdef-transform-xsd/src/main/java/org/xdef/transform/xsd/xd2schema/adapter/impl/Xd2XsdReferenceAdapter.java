package org.xdef.transform.xsd.xd2schema.adapter.impl;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaGroup;
import org.apache.ws.commons.schema.XmlSchemaGroupParticle;
import org.apache.ws.commons.schema.XmlSchemaSimpleContent;
import org.apache.ws.commons.schema.XmlSchemaSimpleContentExtension;
import org.apache.ws.commons.schema.XmlSchemaSimpleContentRestriction;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdef.impl.XData;
import org.xdef.impl.XDefinition;
import org.xdef.impl.XElement;
import org.xdef.impl.XNode;
import org.xdef.model.XMElement;
import org.xdef.model.XMNode;
import org.xdef.transform.xsd.model.impl.OptionalExt;
import org.xdef.transform.xsd.msg.XSD;
import org.xdef.transform.xsd.schema2xd.error.XdAdapterCtxException;
import org.xdef.transform.xsd.xd2schema.def.Xd2XsdFeature;
import org.xdef.transform.xsd.xd2schema.factory.XsdNameFactory;
import org.xdef.transform.xsd.xd2schema.factory.XsdNodeFactory;
import org.xdef.transform.xsd.xd2schema.model.impl.XsdAdapterCtx;
import org.xdef.transform.xsd.xd2schema.model.impl.XsdSchemaImportLocation;
import org.xdef.transform.xsd.xd2schema.model.uc.impl.UniqueConstraint;
import org.xdef.transform.xsd.xd2schema.util.Xd2XsdParserMapping;
import org.xdef.transform.xsd.xd2schema.util.Xd2XsdUtils;
import org.xdef.transform.xsd.xd2schema.util.XsdNameUtils;
import org.xdef.transform.xsd.xd2schema.util.XsdNamespaceUtils;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.xdef.model.XMNode.XMATTRIBUTE;
import static org.xdef.transform.xsd.util.LoggingUtil.HEADER_LINE;
import static org.xdef.transform.xsd.util.LoggingUtil.logHeader;
import static org.xdef.transform.xsd.xd2schema.def.AlgPhase.PREPROCESSING;
import static org.xdef.transform.xsd.xd2schema.def.AlgPhase.TRANSFORMATION;

/**
 * Transforms all X-Definition references into XML Schema (complex/simple) schema types
 */
public class Xd2XsdReferenceAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(Xd2XsdReferenceAdapter.class);

    /**
     * Output XML Schema document
     */
    private final XmlSchema schema;

    /**
     * Output XML Schema document name
     */
    private final String schemaName;

    /**
     * XML Schema node factory
     */
    private final XsdNodeFactory xsdFactory;

    /**
     * X-definition tree adapter
     */
    private final Xd2XsdTreeAdapter treeAdapter;

    /**
     * XML Schema adapter context
     */
    private final XsdAdapterCtx adapterCtx;

    /**
     * Flag, if current instance is used in post processing phase
     */
    private boolean isPostProcessingPhase = false;

    /**
     * Storage of names of already created XML Schema simple types references
     */
    private Set<String> simpleTypeReferences;

    /**
     * Storage of namespace URI of already created XML Schema imports
     */
    private Set<String> namespaceImports;

    /**
     * Storage of XML Schema document names of already created XML Schema includes
     * Used for X-Definition without namespace
     */
    private Set<String> namespaceIncludes;

    public Xd2XsdReferenceAdapter(XmlSchema schema, String schemaName, XsdNodeFactory xsdFactory, Xd2XsdTreeAdapter treeAdapter, XsdAdapterCtx adapterCtx) {
        this.schema = schema;
        this.schemaName = schemaName;
        this.xsdFactory = xsdFactory;
        this.treeAdapter = treeAdapter;
        this.adapterCtx = adapterCtx;
    }

    /**
     * Set flag, phase of algorithm is post processing
     */
    public void setPostProcessing() {
        this.isPostProcessingPhase = true;
    }

    /**
     * Creates following XML Schema nodes from X-Definition nodes:
     *      simpleType      - attribute, text
     *      complexType     - element
     *      group           - mixed
     *      import          - used namespaces in reference of attributes and elements
     * @param xDef  input X-Definition
     */
    public void createRefsAndImports(XDefinition xDef) {
        simpleTypeReferences = new HashSet<>();
        namespaceImports = new HashSet<>();
        namespaceIncludes = new HashSet<>();
        extractRefsAndImports(xDef);
    }

    /**
     * Creates following XML Schema nodes from X-Definition nodes:
     *      simpleType      - attribute, text
     *      import          - used namespaces in reference of attributes and elements
     * @param nodes list of X-Definition nodes
     */
    public void extractRefsAndImports(final List<XNode> nodes) {
        simpleTypeReferences = new HashSet<>();
        namespaceImports = new HashSet<>();
        namespaceIncludes = new HashSet<>();

        final Set<XMNode> processed = new HashSet<>();

        for (XNode node : nodes) {
            // Extract all simple types and imports
            LOG.info("{}Extracting simple references and imports ...", logHeader(PREPROCESSING, node));
            extractSimpleRefsAndImports(node, processed, false);

            // TODO: Should be used?
            // Extract all complex types
            /*
            if (node.getKind() == XNode.XMELEMENT) {
                LOG.info("{}", logHeader());PREPROCESSING, node, "Extracting complex references ...");
                XElement xElem = (XElement)node;
                for (XNode childNode : xElem._childNodes) {
                    if (childNode.getKind() == XNode.XMELEMENT) {
                        extractTopLevelElementRefs(childNode);
                    }
                }
            }
            */
        }
    }

    /**
     * Extracts references and imports from given X-Definition
     * @param xDef  input X-Definition
     */
    private void extractRefsAndImports(final XDefinition xDef) {
        LOG.info(HEADER_LINE);
        LOG.info("{}Creating definition of references and schemas import/include", logHeader(PREPROCESSING, xDef));
        LOG.info(HEADER_LINE);

        final Set<XMNode> processed = new HashSet<>();

        // Extract all simple types and imports
        LOG.info("{}Extracting simple references and imports ...", logHeader(PREPROCESSING, xDef));
        for (XMElement elem : xDef.getModels()) {
            extractSimpleRefsAndImports(elem, processed, false);
        }

        // Extract all complex types
        LOG.info("{}Extracting complex references ...", logHeader(PREPROCESSING, xDef));
        final Set<String> rootNodeNames = adapterCtx.findSchemaRootNodeNames(schemaName);
        for (XMElement elem : xDef.getModels()) {
            if ((rootNodeNames.isEmpty() || !rootNodeNames.contains(elem.getName()))
                    && !adapterCtx.hasEnableFeature(Xd2XsdFeature.XSD_ALL_ELEMENTS_TOPLEVEL)) {
                transformTopLevelElem(elem);
            }
        }
    }

    /**
     * Transform top-level X-Definition element node into XML Schema node (element, complex-type, simple-type, group)
     * @param xElem top-level X-Definition element to be transformed
     */
    private void transformTopLevelElem(final XMElement xElem) {
        LOG.debug("{}Creating definition of reference.", logHeader(PREPROCESSING, xElem));

        final XmlSchemaElement xsdElem = (XmlSchemaElement) treeAdapter.convertTree(xElem)
                .orElseThrow(() -> new XdAdapterCtxException("Required element not created"));
        final XmlSchemaType elementType = xsdElem.getSchemaType();

        if (elementType == null) {
            LOG.info("{}Add definition of reference as element. name='{}'",
                    logHeader(PREPROCESSING, xElem), xsdElem.getName());
        } else {
            if (Xd2XsdUtils.containsMixedElement(xElem) && elementType instanceof XmlSchemaComplexType) {
                // Convert xd:mixed to group
                final XmlSchemaGroup schemaGroup = xsdFactory.createEmptyGroup(xsdElem.getName());
                schemaGroup.setParticle((XmlSchemaGroupParticle)((XmlSchemaComplexType)elementType).getParticle());
                adapterCtx.updateNode(xElem, schemaGroup);
                Xd2XsdUtils.removeNode(schema, xsdElem);
                LOG.info("{}Add definition of group. name='{}'", logHeader(PREPROCESSING, xElem), xsdElem.getName());
            } else {
                // Move schema type (complex-type/simple-type) to top-level and remove original element
                elementType.setName(xsdElem.getName());
                adapterCtx.updateNode(xElem, elementType);
                Xd2XsdUtils.addSchemaTypeNode2TopLevel(schema, elementType);
                Xd2XsdUtils.removeNode(schema, xsdElem);
                LOG.info("{}Add definition of reference as complex/simple type. name='{}'",
                        logHeader(PREPROCESSING, xElem), xsdElem.getName());

                // If complex-type contains simpleType with restriction, the simpleType should be move to top-level
                if (elementType instanceof XmlSchemaComplexType
                        && ((XmlSchemaComplexType) elementType).getContentModel() instanceof XmlSchemaSimpleContent
                        && ((XmlSchemaComplexType) elementType).getContentModel().getContent() instanceof XmlSchemaSimpleContentRestriction) {
                    if (xElem instanceof XElement) {
                        final XElement xElement = (XElement) xElem;
                        if (xElement._attrs.size() == 0
                                && xElement._childNodes.length == 1
                                && xElement._childNodes[0].getKind() == XNode.XMTEXT) {
                            // Create new simple type at top-level
                            processSimpleTypeReference((XData) xElement.getChildNodeModels()[0])
                                    .ifPresent(simpleTypeReferenceName -> {
                                        // Update simpleType restriction to extension
                                        final XmlSchemaSimpleContent schemaSimpleContent =
                                                (XmlSchemaSimpleContent) ((XmlSchemaComplexType) elementType).getContentModel();
                                        final String targetNamespace = elementType.getParent().getTargetNamespace();
                                        final XmlSchemaSimpleContentExtension schemaContent =
                                                xsdFactory.createEmptySimpleContentExtension(
                                                        new QName(targetNamespace,
                                                                simpleTypeReferenceName)
                                                );

                                        schemaSimpleContent.setContent(schemaContent);
                                    });
                        }
                    }
                }
            }
        }
    }

    /**
     * Extract simple-type references and schema imports from X-Definition tree.
     * @param xNode         root of X-Definition tree
     * @param processed     already processed nodes
     * @param parentRef     flag if parent is node using reference
     */
    private void extractSimpleRefsAndImports(final XMNode xNode, final Set<XMNode> processed, boolean parentRef) {
        if (!processed.add(xNode)) {
            LOG.trace("{}Already processed. This node should be reference.", logHeader(PREPROCESSING, xNode));
            return;
        }

        switch (xNode.getKind()) {
            case XMATTRIBUTE: {
                processSimpleTypeReference((XData)xNode);
                break;
            }
            case XNode.XMELEMENT: {
                LOG.debug("{}Processing XMElement node. nodeName='{}'",
                        logHeader(PREPROCESSING, xNode), xNode.getName());

                final XElement xElem = (XElement)xNode;
                boolean isRef = false;
                treeAdapter.loadElementUniqueSets(xElem);

                if (xElem.isReference() || xElem.getReferencePos() != null) {
                    final String refPos = xElem.getReferencePos();
                    final String nodeNsUri = xElem.getNSUri();
                    if (XsdNamespaceUtils.isNodeInDifferentNamespace(xElem.getName(), nodeNsUri, schema)) {
                        addSchemaImportFromElem(nodeNsUri, refPos);
                    } else if (XsdNamespaceUtils.isRefInDifferentNamespacePrefix(refPos, schema)) {
                        final String refSystemId = XsdNamespaceUtils.getSystemIdFromXPosRequired(refPos);
                        final XmlSchema refSchema = adapterCtx.findSchemaReq(refSystemId, PREPROCESSING);
                        final String refNsPrefix = XsdNamespaceUtils.getReferenceNamespacePrefix(refPos);
                        final String nsUri = refSchema.getNamespaceContext().getNamespaceURI(refNsPrefix);
                        if (!XsdNamespaceUtils.isValidNsUri(nsUri)) {
                            adapterCtx.getReportWriter().error(XSD.XSD004, refNsPrefix);
                            LOG.error("{}Element referencing to unknown namespace! refNamespacePrefix='{}'",
                                    logHeader(PREPROCESSING, xElem), refNsPrefix);
                        } else {
                            addSchemaImportFromElem(nsUri, refPos);
                        }
                    } else if (XsdNamespaceUtils.isRefInDifferentSystem(refPos, xElem.getXDPosition())) {
                        addSchemaInclude(refPos);
                    } // else {} // Reference in same X-Definition and same namespace

                    isRef = true;
                } else {
                    // Element is not reference but name contains different namespace prefix ->
                    // we will have to create reference in new namespace in post-processing
                    if (XsdNamespaceUtils.isNodeInDifferentNamespacePrefix(xElem, schema) && !isPostProcessingPhase) {
                        String nsPrefix = XsdNamespaceUtils.getNamespacePrefixRequired(xElem.getName());
                        String nsUri = schema.getNamespaceContext().getNamespaceURI(nsPrefix);

                        // Post-processing
                        if (XsdNamespaceUtils.isValidNsUri(nsUri)) {
                            final List<XsdSchemaImportLocation> importLocations = adapterCtx.findSchemaLocations(nsUri);
                            if (!importLocations.isEmpty()) {
                                for (XsdSchemaImportLocation importLocation : importLocations) {
                                    adapterCtx.addExtraSchemaLocation(nsUri, importLocation);
                                }
                            } else {
                                addPostProcessingSchemaImport(nsPrefix, nsUri, true);
                            }
                        } else {
                            final String xDefPos = xElem.getXDPosition();
                            nsUri = XsdNamespaceUtils.getNodeNamespaceUri(xElem, adapterCtx, PREPROCESSING);

                            if (XsdNamespaceUtils.isValidNsUri(nsUri)) {
                                addSchemaImportFromElem(nsUri, xDefPos);
                            } else {
                                if (!parentRef) {
                                    nsPrefix = XsdNamespaceUtils.getReferenceNamespacePrefix(xDefPos);
                                    adapterCtx.getReportWriter().error(XSD.XSD004, nsPrefix);
                                    LOG.error("{}Element referencing to unknown namespace! namespacePrefix='{}'",
                                            logHeader(PREPROCESSING, xElem), nsPrefix);
                                }
                            }
                        }

                        isRef = true;
                    }
                }

                final String parentSystemId = XsdNamespaceUtils.getSystemIdFromXPosRequired(xElem.getXDPosition());

                final XMNode[] elemAttrs = xElem.getAttrs();
                for (XMNode attr : elemAttrs) {
                    if (!isRef && XsdNamespaceUtils.equalsSystemId(parentSystemId, attr)) {
                        processSimpleTypeReference((XData) attr);
                    }
                }

                int childrenCount = xElem._childNodes.length;
                for (XNode xChild : xElem._childNodes) {
                    if (xChild.getKind() == XNode.XMSELECTOR_END
                            || !XsdNamespaceUtils.equalsSystemId(parentSystemId, xChild)) {
                        continue;
                    }

                    if (xChild.getKind() == XNode.XMTEXT
                            && (adapterCtx.hasEnableFeature(Xd2XsdFeature.XSD_ELEMENT_NO_SIMPLE_TYPE)
                            || (childrenCount > 1 || ((XData) xChild).getRefTypeName() != null))) {
                        processSimpleTypeReference((XData) xChild);
                    } else {
                        boolean isParentRef = xElem.isReference()
                                || XsdNamespaceUtils.isNodeInDifferentNamespacePrefix(xElem, schema);
                        extractSimpleRefsAndImports(xChild, processed, isParentRef);
                    }
                }

                break;
            }
            case XNode.XMDEFINITION: {
                LOG.debug("{}Processing X-Definition node. nodeName='{}'",
                        logHeader(PREPROCESSING, xNode), xNode.getName());

                final XDefinition def = (XDefinition)xNode;
                final XMElement[] xmElements = def.getModels();
                for (XMElement elem : xmElements) {
                    extractSimpleRefsAndImports(elem, processed, false);
                }
                break;
            }
        }
    }

    /**
     * Process simple-type XML Schema reference.
     * Insert X-Definition node into post processing queue if it is using different namespace.
     *
     * @param xData attribute/text node using reference
     * @return reference name if simple type reference has been found or created
     */
    private Optional<String> processSimpleTypeReference(final XData xData) {
        // Element is not reference but name contains different namespace prefix ->
        // we will have to create reference in new namespace in post-processing
        if (XsdNamespaceUtils.isNodeInDifferentNamespacePrefix(xData, schema) && !isPostProcessingPhase) {
            final String nsPrefix = XsdNamespaceUtils.getNamespacePrefixRequired(xData.getName());
            final String nsUri = schema.getNamespaceContext().getNamespaceURI(nsPrefix);

            // Post-processing
            if (nsUri != null && !nsUri.isEmpty()) {
                final List<XsdSchemaImportLocation> importLocations = adapterCtx.findSchemaLocations(nsUri);
                if (!importLocations.isEmpty()) {
                    for (XsdSchemaImportLocation importLocation : importLocations) {
                        adapterCtx.addExtraSchemaLocation(nsUri, importLocation);
                    }
                }
            }
        } else {
            final boolean isAttrRef = xData.getKind() == XMATTRIBUTE;

            if (isAttrRef) {
                final UniqueConstraint uniqueConstraint = adapterCtx.findUniqueConst(xData).orElse(null);
                // Do not create reference if attribute is using unique set
                if (uniqueConstraint != null) {
                    uniqueConstraint.addVariable(xData, adapterCtx);
                    return Optional.empty();
                }
            }

            String refTypeName = adapterCtx.getNameFactory().findTopLevelName(xData, false)
                    .orElseGet(() ->
                        OptionalExt.of(XsdNameFactory.createLocalSimpleTypeName(xData))
                                .ifPresent(defaultRefTypeNameOpt ->
                                        adapterCtx.getNameFactory().addTopSimpleTypeName(xData, defaultRefTypeNameOpt))
                                .orElseGet(() -> null)
                    );

            if (refTypeName != null && simpleTypeReferences.add(refTypeName)) {
                xsdFactory.createSimpleTypeTop(xData, refTypeName);
                LOG.info("{}Creating simple type definition of reference. refTypeName='{}'",
                        logHeader(TRANSFORMATION, xData), refTypeName);
                return Optional.of(refTypeName);
            }

            if (!isAttrRef
                    && refTypeName == null
                    && !Xd2XsdParserMapping.findDefaultParserQName(xData, adapterCtx, true).isPresent()
                    && xData.getValueTypeName() != null) {
                refTypeName = XsdNameUtils.createRefNameFromParser(xData, adapterCtx).orElse(null);
                if (refTypeName != null && simpleTypeReferences.add(refTypeName)) {
                    xsdFactory.createSimpleTypeTop(xData, refTypeName);
                    LOG.info("{}Creating simple type reference from parser. refTypeName='{}'",
                            logHeader(TRANSFORMATION, xData), refTypeName);
                    return Optional.of(refTypeName);
                }
            }
        }

        final String nodeNsUri = xData.getNSUri();
        if (nodeNsUri != null && XsdNamespaceUtils.isNodeInDifferentNamespace(xData.getName(), nodeNsUri, schema)) {
            addSchemaImportFromSimpleType(XsdNamespaceUtils.getNamespacePrefixRequired(xData.getName()), nodeNsUri);
        }

        return Optional.empty();
    }

    /**
     * Add XML Schema document include.
     * @param refPos    reference position of X-Definition node
     */
    private void addSchemaInclude(final String refPos) {
        final String refSystemId = XsdNamespaceUtils.getSystemIdFromXPosRequired(refPos);
        XmlSchema refSchema = adapterCtx.findSchemaReq(refSystemId, PREPROCESSING);
        final String refNsPrefix = XsdNamespaceUtils.getReferenceNamespacePrefix(refPos);
        final String nsUri = refSchema.getNamespaceContext().getNamespaceURI(refNsPrefix);

        if (refSystemId == null || !namespaceIncludes.add(refSystemId)) {
            return;
        }

        final List<XsdSchemaImportLocation> importLocations = adapterCtx.findSchemaLocations(nsUri);
        if (!importLocations.isEmpty()) {
            for (XsdSchemaImportLocation importLocation : importLocations) {
                if (!importLocation.getXDefName().isPresent() || refSystemId.equals(importLocation.getXDefName().get())) {
                    LOG.info("{}Add schema include. schemaName='{}', namespaceURI='{}'",
                            logHeader(PREPROCESSING), importLocation.getFileName(), nsUri);
                    xsdFactory.createSchemaInclude(
                            schema,
                            importLocation.buildLocation(XsdNamespaceUtils.getSystemIdFromXPosRequired(refPos)));
                }
            }
        } else {
            adapterCtx.getReportWriter().warning(XSD.XSD012, refSystemId);
            LOG.warn("{}Required schema include has not been found! namespaceURI='{}'", logHeader(PREPROCESSING), nsUri);
        }
    }

    /**
     * Add XML Schema document import based on X-Definition element node.
     * @param nsUri     X-Definition node namespace URI
     * @param refPos    X-Definition reference position
     */
    private void addSchemaImportFromElem(final String nsUri, final String refPos) {
        if (nsUri == null || !namespaceImports.add(nsUri)) {
            return;
        }

        final List<XsdSchemaImportLocation> importLocations = adapterCtx.findSchemaLocations(nsUri);
        if (!importLocations.isEmpty()) {
            for (XsdSchemaImportLocation importLocation : importLocations) {
                LOG.info("{}Add namespace import. schemaName='{}', namespaceURI='{}'",
                        logHeader(PREPROCESSING), importLocation.getFileName(), nsUri);
                xsdFactory.createSchemaImport(
                        schema,
                        nsUri,
                        importLocation.buildLocation(XsdNamespaceUtils.getSystemIdFromXPosRequired(refPos)));
            }
        } else {
            adapterCtx.getReportWriter().warning(XSD.XSD013, nsUri);
            LOG.warn("{}Required schema import has not been found! namespaceURI='{}'",
                    logHeader(PREPROCESSING), nsUri);
        }
    }

    /**
     * Add XML Schema document import based on attribute/text X-Definition node
     * @param nsPrefix  X-Definition node namespace prefix
     * @param nsUri     X-Definition node namespace URI
     */
    private void addSchemaImportFromSimpleType(final String nsPrefix, final String nsUri) {
        if (nsUri == null || !namespaceImports.add(nsUri)) {
            return;
        }

        List<XsdSchemaImportLocation> importLocations = adapterCtx.findSchemaLocations(nsUri);
        if (!importLocations.isEmpty()) {
            for (XsdSchemaImportLocation importLocation : importLocations) {
                LOG.info("{}Add namespace import. schemaName='{}', namespaceURI='{}'",
                        logHeader(PREPROCESSING), importLocation.getFileName(), nsUri);
                xsdFactory.createSchemaImport(schema, nsUri, importLocation.buildLocation(null));
            }
        } else {
            if (!adapterCtx.isPostProcessingNamespace(nsUri)) {
                addPostProcessingSchemaImport(nsPrefix, nsUri, false);
            } else if (isPostProcessingPhase) {
                importLocations = adapterCtx.findPostProcessingSchemaLocations(nsUri);
                if (!importLocations.isEmpty()) {
                    for (XsdSchemaImportLocation importLocation : importLocations) {
                        LOG.info("{}Add namespace import. schemaName='{}', namespaceURI='{}'",
                                logHeader(PREPROCESSING), importLocation.getFileName(), nsUri);
                        xsdFactory.createSchemaImport(schema, nsUri, importLocation.buildLocation(null));
                    }
                } else {
                    adapterCtx.getReportWriter().warning(XSD.XSD013, nsUri);
                    LOG.warn("{}Required schema import has not been found! namespaceURI='{}'",
                            logHeader(PREPROCESSING), nsUri);
                }
            }
        }
    }

    /**
     * Add XML Schema document import of post processed schema
     * @param nsPrefix  schema namespace prefix
     * @param nsUri     schema namespace URI
     */
    private void addPostProcessingSchemaImport(final String nsPrefix, final String nsUri, boolean addNamespace) {
        if (nsUri == null || (addNamespace && !namespaceImports.add(nsUri))) {
            return;
        }

        final XsdSchemaImportLocation importLocation = adapterCtx.addExtraSchemaLocation(nsPrefix, nsUri);
        if (importLocation != null) {
            LOG.info("{}Add external namespace import. schemaName='{}', namespaceURI='{}'",
                    logHeader(PREPROCESSING), schemaName, nsUri);
            xsdFactory.createSchemaImport(schema, nsUri, importLocation.buildLocation(null));
        } else {
            adapterCtx.getReportWriter().warning(XSD.XSD013, nsUri);
            LOG.warn("{}Required postprocessing schema import has not been found! schemaName='{}', namespaceURI='{}'",
                    logHeader(PREPROCESSING), schemaName, nsUri);
        }
    }

}
