package org.xdef.transform.xsd.xd2schema.util;


import org.apache.commons.lang3.tuple.Pair;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAll;
import org.apache.ws.commons.schema.XmlSchemaAllMember;
import org.apache.ws.commons.schema.XmlSchemaAnnotationItem;
import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaChoice;
import org.apache.ws.commons.schema.XmlSchemaChoiceMember;
import org.apache.ws.commons.schema.XmlSchemaComplexContentExtension;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaContent;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaForm;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSimpleContent;
import org.apache.ws.commons.schema.XmlSchemaSimpleContentExtension;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdef.impl.XElement;
import org.xdef.impl.XNode;
import org.xdef.transform.xsd.msg.XSD;
import org.xdef.transform.xsd.xd2schema.definition.Xd2XsdFeature;
import org.xdef.transform.xsd.xd2schema.factory.SchemaNodeFactory;
import org.xdef.transform.xsd.xd2schema.factory.XsdNameFactory;
import org.xdef.transform.xsd.xd2schema.factory.XsdNodeFactory;
import org.xdef.transform.xsd.xd2schema.model.SchemaNode;
import org.xdef.transform.xsd.xd2schema.model.XmlSchemaNodeMap;
import org.xdef.transform.xsd.xd2schema.model.XsdAdapterCtx;
import org.xdef.transform.xsd.xd2schema.model.xsd.XmlSchemaChoiceWrapper;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.xdef.transform.xsd.util.LoggingUtil.HEADER_LINE;
import static org.xdef.transform.xsd.util.LoggingUtil.logHeader;
import static org.xdef.transform.xsd.xd2schema.definition.AlgPhase.POSTPROCESSING;
import static org.xdef.transform.xsd.xd2schema.definition.AlgPhase.TRANSFORMATION;
import static org.xdef.transform.xsd.xd2schema.definition.Xd2XsdFeature.XSD_SKIP_DELETE_TOP_LEVEL_ELEMENTS;
import static org.xdef.transform.xsd.xd2schema.definition.Xd2XsdLogGroup.XSD_PP_PROCESSOR;


/**
 * All partial transforming algorithms for post processing of nodes structures and linking
 */
public class XsdPostProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(XsdPostProcessor.class);

    private final XsdAdapterCtx adapterCtx;

    public XsdPostProcessor(XsdAdapterCtx adapterCtx) {
        this.adapterCtx = adapterCtx;
    }

    /**
     * Updates XSD references which are currently breaking XSD document rules
     */
    public void processRefs() {
        LOG.info(HEADER_LINE);
        LOG.info("{}Updating reference", logHeader(POSTPROCESSING, XSD_PP_PROCESSOR));
        LOG.info(HEADER_LINE);

        final List<SchemaNode> nodesToRemove = new ArrayList<>();

        for (Map.Entry<String, XmlSchemaNodeMap.SchemaNodeMap> schemaNodes : adapterCtx.getXmlSchemaNodeMap().entrySet()) {
            final String schemaName = schemaNodes.getKey();
            LOG.info("{}Updating references - phase 1. systemId='{}'",
                    logHeader(POSTPROCESSING, XSD_PP_PROCESSOR), schemaName);

            final XmlSchema xmlSchema = adapterCtx.findSchemaReq(schemaName, POSTPROCESSING);
            final XsdNodeFactory xsdFactory = new XsdNodeFactory(xmlSchema, adapterCtx);
            final Set<String> schemaRootNodeNames = adapterCtx.findSchemaRootNodeNames(schemaName);

            for (Map.Entry<String, SchemaNode> refEntry : schemaNodes.getValue().entrySet()) {
                final SchemaNode node = refEntry.getValue();
                if (isTopElement(node)) {
                    // Process elements which are on top level but they are not root of X-Definition
                    boolean elementNotInXDefRoot = adapterCtx.hasEnableFeature(XSD_SKIP_DELETE_TOP_LEVEL_ELEMENTS)
                            ? (!schemaRootNodeNames.isEmpty() && !schemaRootNodeNames.contains(node.getXdName()))
                            : (schemaRootNodeNames.isEmpty() || !schemaRootNodeNames.contains(node.getXdName()));

                    if (!adapterCtx.isPostProcessingNamespace(xmlSchema.getTargetNamespace()) && elementNotInXDefRoot) {
                        if (!node.hasAnyPointer()) {
                            nodesToRemove.add(node);
                        } else {
                            elementTopToComplex(node, xsdFactory);
                        }
                    } else if (node.toXsdElem().isRef()) {
                        elementRootRef(node, xsdFactory);
                    }
                }
            }
        }

        for (SchemaNode node : nodesToRemove) {
            adapterCtx.removeNode((XNode)node.getXdNodeReq());
            Xd2XsdUtils.removeNode(node.toXsdElem().getParent(), node.toXsdElem());
        }

        for (Map.Entry<String, XmlSchemaNodeMap.SchemaNodeMap> schemaNodes : adapterCtx.getXmlSchemaNodeMap().entrySet()) {
            final String schemaName = schemaNodes.getKey();
            LOG.info("{}Updating references - phase 2. systemId='{}'",
                    logHeader(POSTPROCESSING, XSD_PP_PROCESSOR), schemaName);

            for (Map.Entry<String, SchemaNode> refEntry : schemaNodes.getValue().entrySet()) {
                final SchemaNode node = refEntry.getValue();

                updateRefType(node);

                if (!node.getReference().isPresent() && isQualifiedTopElementWithUnqualifiedPtr(node)) {
                    elementRootDecomposition(node);
                }
            }
        }
    }

    /**
     * Decomposition of XSD top-level qualified root element which is referenced by unqualified node
     * @param node  node to be decomposed
     */
    private void elementRootDecomposition(final SchemaNode node) {
        LOG.debug("{}Decomposition of root element with pointers ...", logHeader(POSTPROCESSING, node.getXdNode().orElse(null)));

        final XmlSchemaElement xsdElem = node.toXsdElem();

        if (xsdElem.getSchemaType() == null) {
            adapterCtx.getReportWriter().warning(XSD.XSD041);
            LOG.warn("{}Schema type has been expected!", logHeader(POSTPROCESSING, node.getXdNode().orElse(null)));
            return;
        }

        final XmlSchemaType schemaType = xsdElem.getSchemaType();
        final XElement xElem = node.toXdElem();
        final String localName = xsdElem.getName();
        String newLocalName = XsdNameFactory.createRootElemName(localName, schemaType);
        newLocalName = adapterCtx.getNameFactory().generateTopLevelName(xElem, newLocalName);
        final String elemNsUri = xsdElem.getParent()
                .getNamespaceContext()
                .getNamespaceURI(XsdNamespaceUtils.getNamespacePrefixRequired(xElem.getName()));

        // Move element's schema type to top
        schemaType.setName(newLocalName);
        Xd2XsdUtils.addSchemaTypeNode2TopLevel(xsdElem.getParent(), schemaType);
        node.setXsdNode(schemaType);

        xsdElem.setSchemaType(null);
        xsdElem.setSchemaTypeName(new QName(elemNsUri, newLocalName));

        SchemaNode newSchemaNode = SchemaNodeFactory.createElementNode(xsdElem, xElem);
        newSchemaNode = adapterCtx.addOrUpdateNode(newSchemaNode);
        SchemaNode.createBinding(newSchemaNode, node);

        updatePointers(node, newLocalName);
    }

    /**
     * Transform XSD top-level element node using reference to XSD complex type
     * @param node          node to be transformed
     * @param xsdFactory    XSD element factory
     */
    private void elementRootRef(final SchemaNode node, final XsdNodeFactory xsdFactory) {
        final SchemaNode refSchemaNode = node.getReference().orElse(null);
        if (refSchemaNode == null || (refSchemaNode.isXsdComplexType() && !node.hasAnyPointer())) {
            return;
        }

        LOG.info("{}Updating top-level element reference ...", logHeader(POSTPROCESSING, node.getXdNode().orElse(null)));

        elementTopToComplex(node, xsdFactory);

        if (isTopElement(refSchemaNode)) {
            final String systemId = XsdNamespaceUtils.getSystemIdFromXPosRequired(refSchemaNode.getXdNodeReq().getXDPosition());
            final XmlSchema xmlSchema = adapterCtx.findSchemaReq(systemId, POSTPROCESSING);
            final XsdNodeFactory refXsdFactory = new XsdNodeFactory(xmlSchema, adapterCtx);
            if (refSchemaNode.toXsdElem().isRef()) {
                elementRootRef(refSchemaNode, refXsdFactory);
            } else {
                elementTopToComplex(refSchemaNode, refXsdFactory);
            }
        }
    }

    /**
     * Transform XSD element node to XSD complex type
     * @param node          node to be transformed
     * @param xsdFactory    XSD element factory
     */
    private void elementTopToComplex(final SchemaNode node, final XsdNodeFactory xsdFactory) {
        LOG.info("{}Converting top-level element to complex-type ...", logHeader(POSTPROCESSING, node.getXdNode().orElse(null)));

        final XmlSchemaElement xsdElem = node.toXsdElem();
        final XElement xElem = node.toXdElem();
        final String newRefLocalName = adapterCtx.getNameFactory().findTopLevelName(xElem)
                .orElseGet(() -> {
                    String generatedRefLocalName = XsdNameFactory.createComplexRefName(xElem.getName());
                    generatedRefLocalName = adapterCtx.getNameFactory().generateTopLevelName(xElem, generatedRefLocalName);
                    return generatedRefLocalName;
                });

        // Creating complex content with extension to original reference
        XmlSchemaType schemaType = null;
        if (xsdElem.getRef().getTargetQName() != null) {
            schemaType = xsdFactory.createComplexTypeWithComplexExtensionTop(newRefLocalName, xsdElem.getRef().getTargetQName());
        } else if (xsdElem.getSchemaTypeName() != null) {
            schemaType = xsdFactory.createComplexTypeWithSimpleExtensionTop(newRefLocalName, xsdElem.getSchemaTypeName());
        }

        // If element does not contain schema type, create new empty complex type
        if (schemaType == null) {
            schemaType = xsdFactory.createEmptyComplexType(true);
            schemaType.setName(newRefLocalName);
        }

        node.setXsdNode(schemaType);

        // Remove original element from schema
        Xd2XsdUtils.removeNode(xsdElem.getParent(), xsdElem);

        updatePointers(node, newRefLocalName);
    }

    /**
     * Update all elements referencing to given node which has been transformed previously
     * @param node              transformed node (be referenced)
     * @param newLocalName      new name of transformed node
     */
    private static void updatePointers(final SchemaNode node, final String newLocalName) {
        // Update all pointers to element
        for (SchemaNode ptrNode : node.getPointers()) {
            if (ptrNode.isXsdElem()) {
                final XmlSchemaElement xsdPtrElem = ptrNode.toXsdElem();
                final QName ptrQName = xsdPtrElem.getRef().getTargetQName();
                if (ptrQName != null) {
                    if (xsdPtrElem.getForm() == XmlSchemaForm.UNQUALIFIED) {
                        xsdPtrElem.getRef().setTargetQName(null);
                        final QName newPtrQName = new QName(ptrQName.getNamespaceURI(), newLocalName);
                        final String newPtrElemName = XsdNameUtils.getReferenceName(ptrNode.getXdName());
                        xsdPtrElem.setName(newPtrElemName);
                        xsdPtrElem.setSchemaTypeName(newPtrQName);

                        LOG.info("{}Change element reference to schema type. " +
                                        "elementXDefName='{}', newQName='{}', oldQName='{}'",
                                logHeader(POSTPROCESSING, node.getXdNode().orElse(null)),
                                ptrNode.getXdName(), newPtrQName, ptrQName);
                    }
                }
            } else if (ptrNode.isXsdComplexExt()) {
                final XmlSchemaComplexContentExtension xsdPtrExt = ptrNode.toXsdComplexExt();
                final QName ptrQName = xsdPtrExt.getBaseTypeName();
                final QName newPtrQName = new QName(ptrQName.getNamespaceURI(), newLocalName);
                xsdPtrExt.setBaseTypeName(newPtrQName);

                LOG.info("{}Change complex extension base. elementXDefName='{}', newQName='{}', oldQName='{}'",
                        logHeader(POSTPROCESSING, node.getXdNode().orElse(null)),
                        ptrNode.getXdName(), newPtrQName, ptrQName);
            }
        }
    }

    /**
     * Check if given node is XSD top-level element node
     * @param node  XSD element node
     * @return  true if given node is XSD top-level element node
     */
    private static boolean isTopElement(final SchemaNode node) {
        return node.isXsdElem() && node.toXsdElem().isTopLevel();
    }

    /**
     * Check if given node is XSD top-level element node and has any pointer
     * @param node  XSD element node
     * @return  true if given node is XSD top-level element node and has any pointer
     */
    private static boolean isTopElementWithPtr(final SchemaNode node) {
        return node.isXsdElem() && node.toXsdElem().isTopLevel() && node.hasAnyPointer();
    }

    /**
     * Check if given node is XSD top-level qualified element node with any pointer from unqualified XSD node
     * @param node  XSD element node
     * @return  true if given node is XSD top-level qualified element node with any pointer from unqualified XSD node
     */
    private static boolean isQualifiedTopElementWithUnqualifiedPtr(final SchemaNode node) {
        if (isTopElementWithPtr(node)) {
            final XmlSchemaForm nodeSchema = node.toXsdElem().getForm();
            for (SchemaNode ptr : node.getPointers()) {
                if (ptr.isXsdElem()) {
                    final XmlSchemaForm ptrSchema = ptr.toXsdElem().getForm();
                    final boolean ptrHasNs = XsdNamespaceUtils.containsNsPrefix(ptr.getXdName());
                    if (!ptrHasNs && XmlSchemaForm.UNQUALIFIED.equals(ptrSchema) && XmlSchemaForm.QUALIFIED.equals(nodeSchema)) {
                        return true;
                    }
                } else if (ptr.isXsdComplexExt()) {
                    final boolean ptrHasNs = XsdNamespaceUtils.containsNsPrefix(ptr.getXdName());
                    if (!ptrHasNs) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Updates reference type if necessary
     * @param node  XSD node
     */
    private static void updateRefType(final SchemaNode node) {
        LOG.debug("{}Updating reference type", logHeader(POSTPROCESSING, node.getXdNode().orElse(null)));

        final SchemaNode refSchemaNode = node.getReference().orElse(null);
        if (refSchemaNode != null) {
            if (refSchemaNode.isXsdElem() && node.isXsdElem() && node.toXsdElem().getRef().getTargetQName() == null) {
                // Reference element to element
                LOG.trace("{}Update reference to element", logHeader(POSTPROCESSING, node.getXdNode().orElse(null)));
                XmlSchemaElement xsdElem = node.toXsdElem();
                xsdElem.getRef().setNamedObject(null);
                xsdElem.getRef().setTargetQName(xsdElem.getSchemaTypeName());
                xsdElem.setSchemaTypeName(null);
            } else if (refSchemaNode.isXsdComplexType() && node.isXsdElem() && node.toXsdElem().getTargetQName() != null) {
                // Reference element to complex type
                LOG.trace("{}\"Update reference to complex type", logHeader(POSTPROCESSING, node.getXdNode().orElse(null)));
                XmlSchemaElement xsdElem = node.toXsdElem();
                xsdElem.setSchemaTypeName(xsdElem.getTargetQName());
                xsdElem.getRef().setTargetQName(null);
                xsdElem.setName(node.getXdLocalName());
            }
        }
    }

    /**
     * Transform XSD complex type of XSD element node to be valid
     * @param complexType   XSD complex type
     * @param defEl         X-Definition element node
     */
    public void elementComplexType(final XmlSchemaComplexType complexType, final XElement defEl) {
        LOG.debug("{}Updating complex content of element", logHeader(POSTPROCESSING, defEl));

        if (complexType.getParticle() instanceof XmlSchemaAll) {
            final XmlSchemaChoice newGroupChoice = groupParticleAllToChoice((XmlSchemaAll)complexType.getParticle());
            if (newGroupChoice != null) {
                complexType.setParticle(newGroupChoice);
            }
        }

        // element contains simple content and particle
        // -> XSD does not support restrictions for text if element contains elements

        // We have to use mixed attribute for root element and remove simple content
        if (adapterCtx.hasEnableFeature(Xd2XsdFeature.POSTPROCESSING_MIXED)) {
            if (complexType.getParticle() != null
                    && complexType.getContentModel() != null
                    && complexType.getContentModel() instanceof XmlSchemaSimpleContent) {
                adapterCtx.getReportWriter().warning(XSD.XSD042);
                LOG.warn("{}!Lossy transformation! Remove simple content from element due to existence " +
                        "of complex content. Use mixed attr.", logHeader(POSTPROCESSING, defEl));

                // Copy attributes from simple content
                XmlSchemaContent content = complexType.getContentModel().getContent();
                if (content instanceof XmlSchemaSimpleContentExtension) {
                    List attrs = ((XmlSchemaSimpleContentExtension) content).getAttributes();
                    if (attrs != null && !attrs.isEmpty()) {
                        complexType.getAttributes().addAll(attrs);
                    }
                }

                // TODO: remove by reference handler
                //Xd2XsdUtils.removeItem(schema, complexType.getContentModel());
                complexType.setContentModel(null);
                complexType.setMixed(true);
                XsdNodeFactory.createAnnotation(
                        "Text content has been originally restricted by X-Definition", adapterCtx
                ).ifPresent(complexType::setAnnotation);
            }
        }
    }

    /**
     * Transform given XSD group particle all to XSD group particle choice
     * @param groupParticleAll      XSD group particle all
     * @return XSD group particle choice node
     */
    private XmlSchemaChoice groupParticleAllToChoice(final XmlSchemaAll groupParticleAll) {
        if (!adapterCtx.hasEnableFeature(Xd2XsdFeature.POSTPROCESSING_ALL_TO_CHOICE)) {
            return null;
        }

        boolean anyElementMultiple = false;
        boolean anyElementUnbound = false;

        for (XmlSchemaAllMember member : groupParticleAll.getItems()) {
            if (member instanceof XmlSchemaElement) {
                final XmlSchemaElement memberElem = (XmlSchemaElement) member;
                if (memberElem.getMaxOccurs() == Long.MAX_VALUE) {
                    anyElementUnbound = true;
                } else if (memberElem.getMaxOccurs() > 1) {
                    anyElementMultiple = true;
                }
            }
        }

        if (anyElementUnbound || anyElementMultiple) {
            return groupParticleAllToChoice(groupParticleAll, anyElementUnbound);
        }

        return null;
    }

    /**
     * Transform given XSD group particle all to XSD group particle choice
     * @param groupParticleAll      XSD group particle all
     * @param unbounded             flag, if member's occurrence should be calculated (otherwise will be unbounded)
     * @return  XSD group particle choice node
     */
    public XmlSchemaChoice groupParticleAllToChoice(final XmlSchemaAll groupParticleAll, boolean unbounded) {
        if (!adapterCtx.hasEnableFeature(Xd2XsdFeature.POSTPROCESSING_ALL_TO_CHOICE)) {
            return null;
        }

        LOG.debug("{}Converting group particle xsd:all to xsd:choice ...", logHeader(TRANSFORMATION));
        adapterCtx.getReportWriter().warning(XSD.XSD043);
        LOG.warn("{}!Lossy transformation! Node xsd:sequence/choice contains xsd:all node -> " +
                "converting xsd:all node to xsd:choice!", logHeader(TRANSFORMATION));

        final XmlSchemaChoice newGroupChoice = new XmlSchemaChoice();

        XsdNodeFactory.createAnnotation(
                "Original group particle: all", adapterCtx
        ).ifPresent(newGroupChoice::setAnnotation);

        long elementMinOccursSum;
        long elementMaxOccursSum;

        // Calculate member occurrences
        if (!unbounded) {
            final Pair<Long, Long> memberOccurrence = Xd2XsdUtils.calculateGroupAllMembersOccurrence(
                    groupParticleAll,
                    adapterCtx);
            elementMinOccursSum = memberOccurrence.getKey();
            elementMaxOccursSum = memberOccurrence.getValue();
        } else {
            elementMinOccursSum = groupParticleAll.getMinOccurs();
            elementMaxOccursSum = Long.MAX_VALUE;
        }

        newGroupChoice.setMaxOccurs(elementMaxOccursSum);
        if (groupParticleAll.getMinOccurs() == 0) {
            newGroupChoice.setMinOccurs(0);
        } else {
            newGroupChoice.setMinOccurs(elementMinOccursSum);
        }
        copyAllMembersToChoice(groupParticleAll, newGroupChoice);
        return newGroupChoice;
    }

    /**
     * Transform XSD simple-type node content with empty restriction to given XSD attribute node
     * @param simpleTypeRestriction     XSD simple-type restriction node
     * @param attr                      XSD attribute node
     */
    public void simpleTypeRestrictionToAttr(final XmlSchemaSimpleTypeRestriction simpleTypeRestriction,
                                            final XmlSchemaAttribute attr) {
        if (simpleTypeRestriction.getFacets().isEmpty()) {
            attr.setSchemaTypeName(simpleTypeRestriction.getBaseTypeName());
            if (attr.getAnnotation() != null) {
                final List<XmlSchemaAnnotationItem> annotationItems = simpleTypeRestriction.getAnnotation().getItems();
                if (annotationItems != null && !annotationItems.isEmpty()) {
                    attr.getAnnotation().getItems().addAll(annotationItems);
                }
            } else {
                attr.setAnnotation(simpleTypeRestriction.getAnnotation());
            }
        }
    }

    /**
     * Copy all XSD group all members to XSD group choice
     * @param groupParticleAll      XSD group all node
     * @param schemaChoice          XSD group choice node
     */
    private void copyAllMembersToChoice(final XmlSchemaAll groupParticleAll, final XmlSchemaChoice schemaChoice) {
        LOG.debug("{}Converting group particle's members of xsd:all to xsd:choice", logHeader(TRANSFORMATION));
        for (XmlSchemaAllMember member : groupParticleAll.getItems()) {
            allMemberToChoiceMember(member);
            schemaChoice.getItems().add((XmlSchemaChoiceMember) member);
        }
    }

    /**
     * Additional transformation of XSD choice member node, which has been originally transformed from XSD group all member node
     * @param member    XSD choice member node
     */
    public void allMemberToChoiceMember(final XmlSchemaObjectBase member) {
        if (!adapterCtx.hasEnableFeature(Xd2XsdFeature.POSTPROCESSING_ALL_TO_CHOICE)) {
            return;
        }

        if (member instanceof XmlSchemaParticle) {
            final XmlSchemaParticle memberParticle = (XmlSchemaParticle)member;
            if (memberParticle.getMinOccurs() != 1 || memberParticle.getMaxOccurs() != 1) {
                final String minOcc = memberParticle.getMinOccurs() == Long.MAX_VALUE ? "unbounded" : String.valueOf(memberParticle.getMinOccurs());
                final String maxOcc = memberParticle.getMaxOccurs() == Long.MAX_VALUE ? "unbounded" : String.valueOf(memberParticle.getMaxOccurs());
                XsdNodeFactory.createAnnotation(
                        "Occurrence: [" + minOcc + ", " + maxOcc + "]", adapterCtx
                ).ifPresent(memberParticle::setAnnotation);
            }

            memberParticle.setMaxOccurs(1);
            memberParticle.setMinOccurs(1);
        }
    }

    /**
     * Transform XSD group all node to XSD group choice node
     * @param transformDirection    direction of transformation
     * @return XSD group choice node
     */
    public XmlSchemaChoiceWrapper groupParticleAllToChoice(final XmlSchemaChoiceWrapper.TransformDirection transformDirection) {
        if (!adapterCtx.hasEnableFeature(Xd2XsdFeature.POSTPROCESSING_ALL_TO_CHOICE)) {
            return null;
        }
        final XmlSchemaChoiceWrapper newGroupChoice = new XmlSchemaChoiceWrapper(new XmlSchemaChoice());
        newGroupChoice.setTransformDirection(transformDirection);
        XsdNodeFactory.createAnnotation(
                "Original group particle: all", adapterCtx
        ).ifPresent(xmlSchemaAnnotation -> newGroupChoice.xsd().setAnnotation(xmlSchemaAnnotation));

        adapterCtx.getReportWriter().warning(XSD.XSD043);
        LOG.warn("{}!Lossy transformation! Node xsd:sequence/choice contains xsd:all node -> " +
                "converting xsd:all node to xsd:choice!", logHeader(TRANSFORMATION));

        return newGroupChoice;
    }
}
