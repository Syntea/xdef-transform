package org.xdef.transform.xsd.xd2schema.factory;

import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaComplexContentExtension;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaGroupRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdef.impl.XData;
import org.xdef.impl.XElement;
import org.xdef.transform.xsd.xd2schema.model.XmlSchemaNodeMap;
import org.xdef.transform.xsd.xd2schema.model.impl.SchemaNode;
import org.xdef.transform.xsd.xd2schema.model.impl.XsdAdapterCtx;
import org.xdef.transform.xsd.xd2schema.util.XsdNameUtils;
import org.xdef.transform.xsd.xd2schema.util.XsdNamespaceUtils;

import java.util.Optional;

import static org.xdef.transform.xsd.util.LoggingUtil.logHeader;
import static org.xdef.transform.xsd.xd2schema.def.Xd2XsdLogGroup.XSD_REFERENCE;


/**
 * Factory which is creating schema nodes, which pairs up X-Definition nodes with XML Schema nodes
 */
public class SchemaNodeFactory {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaNodeFactory.class);

    /**
     * Creates schema node and reference schema node based on element node
     * Creates binding between schema element node and reference definition schema node
     *
     * @param xElem         X-Definition element node
     * @param xsdElem       XML Schema element node
     * @param refSystemId   reference node system identifier
     * @param refNodePos    reference node x-position
     * @param refNodePath   reference node path
     * @param adapterCtx    XML Schema adapter context
     */
    public static void createElemRefAndDef(final XElement xElem,
                                           final XmlSchemaElement xsdElem,
                                           final String refSystemId,
                                           final String refNodePos,
                                           final String refNodePath,
                                           final XsdAdapterCtx adapterCtx) {
        LOG.trace("{}createElemRefAndDef ...", logHeader(XSD_REFERENCE));

        SchemaNode node = createElementNode(xsdElem, xElem);
        final SchemaNode nodeRef = createDefNode(refSystemId, refNodePos, refNodePath, adapterCtx);
        node = adapterCtx.addOrUpdateNode(node);
        SchemaNode.createBinding(node, nodeRef);
    }

    /**
     * Creates schema node and reference definition schema node based on element node which is using different namespace
     * Creates binding between schema element node and reference definition schema node
     *
     * @param xElem         X-Definition element node
     * @param xsdElem       XML Schema element node
     * @param systemId      X-Definition element node system identifier
     * @param nodePath      X-Definition element node path
     * @param refSystemId   reference node system identifier
     * @param refNodePos    reference node x-position
     * @param refNodePath   reference node path
     * @param adapterCtx    XML Schema adapter context
     */
    public static void createElemRefAndDefDiffNamespace(final XElement xElem,
                                                        final XmlSchemaElement xsdElem,
                                                        final String systemId,
                                                        final String nodePath,
                                                        final String refSystemId,
                                                        final String refNodePos,
                                                        final String refNodePath,
                                                        final XsdAdapterCtx adapterCtx) {
        LOG.trace("{}createElemRefAndDefDiffNamespace ...", logHeader(XSD_REFERENCE));

        SchemaNode node = createElementNode(xsdElem, xElem);
        final SchemaNode nodeRef = createDefNode(refSystemId, refNodePos, refNodePath, adapterCtx);
        node = adapterCtx.addOrUpdateNode(systemId, nodePath, node);
        SchemaNode.createBinding(node, nodeRef);
    }

    /**
     * Creates schema node and reference schema node based on X-Definition element and XML Schema complex content extension
     * Creates binding between schema complex node and reference definition schema node
     *
     * @param xElem             X-Definition element node
     * @param xsdComplexExt     XML Schema complex content extension
     * @param refSystemId       reference node system identifier
     * @param refNodePos        reference node x-position
     * @param refNodePath       reference node path
     * @param adapterCtx        XML Schema adapter context
     */
    public static void createComplexExtRefAndDef(final XElement xElem,
                                                 final XmlSchemaComplexContentExtension xsdComplexExt,
                                                 final String refSystemId,
                                                 final String refNodePos,
                                                 final String refNodePath,
                                                 final XsdAdapterCtx adapterCtx) {
        LOG.trace("{}createComplexExtRefAndDef ...", logHeader(XSD_REFERENCE));

        SchemaNode node = createComplexExtNode(xElem, xsdComplexExt);
        final SchemaNode nodeRef = createDefNode(refSystemId, refNodePos, refNodePath, adapterCtx);

        final String xPos = node.getXdNodeReq().getXDPosition();
        final String systemId = XsdNamespaceUtils.getSystemIdFromXPosRequired(xPos);
        // Single X-Definition position is related to two XML Schema nodes ...
        //  1) XML Schema element
        //  2) XML Schema element's complex content extension
        // Mark complex content extension as _internal
        final String nodePath = XsdNameUtils.getXNodePath(xPos) + "_internal";

        node = adapterCtx.addOrUpdateNode(systemId, nodePath, node);
        SchemaNode.createBinding(node, nodeRef);
    }

    /**
     * Creates schema node based on element node
     * @param xsdElem   XML Schema element node
     * @param xElem     X-Definition element node
     * @return schema node
     */
    public static SchemaNode createElementNode(final XmlSchemaElement xsdElem, final XElement xElem) {
        return new SchemaNode(xElem.getXDPosition(), xsdElem, xElem);
    }

    /**
     * Creates schema node based on attribute node
     * @param xsdAttr       XML Schema attribute node
     * @param xDataAttr     X-Definition attribute node
     * @return schema node
     */
    public static SchemaNode createAttributeNode(final XmlSchemaAttribute xsdAttr, final XData xDataAttr) {
        return new SchemaNode(xDataAttr.getXDPosition(), xsdAttr, xDataAttr);
    }

    /**
     * Creates schema node based on X-Definition element node and XML Schema group reference
     * @param xElem         X-Definition element node
     * @param xsdGroupRef   XML Schema group reference node
     * @param nodeRef       reference schema node
     * @param adapterCtx    XML Schema adapter context
     */
    public static void createGroupRefNode(final XElement xElem, final XmlSchemaGroupRef xsdGroupRef,
                                          final SchemaNode nodeRef, final XsdAdapterCtx adapterCtx) {
        SchemaNode node = createGroupRefNode(xElem, xsdGroupRef);
        node = adapterCtx.addOrUpdateNode(node);
        SchemaNode.createBinding(node, nodeRef);
    }

    /**
     * Creates schema node based on X-Definition element node and XML Schema complex content extension
     * @param xElem             X-Definition element node
     * @param xsdComplexExt     XML Schema complex content extension node
     * @return created schema node
     */
    private static SchemaNode createComplexExtNode(final XElement xElem, final XmlSchemaComplexContentExtension xsdComplexExt) {
        return new SchemaNode(xElem.getXDPosition(), xsdComplexExt, xElem);
    }

    /**
     * Creates schema node based on X-Definition element node and XML Schema group reference
     * @param xElem             X-Definition element node
     * @param xsdGroupRef       XML Schema group reference node
     * @return created schema node
     */
    private static SchemaNode createGroupRefNode(final XElement xElem, final XmlSchemaGroupRef xsdGroupRef) {
        return new SchemaNode(xElem.getXDPosition(), xsdGroupRef, xElem);
    }

    /**
     * Creates schema node of reference definition
     * @param systemId      definition system identified
     * @param nodePos       definition x-position
     * @param nodePath      definition path
     * @param adapterCtx    XML Schema adapter context
     * @return schema node
     */
    private static SchemaNode createDefNode(final String systemId, final String nodePos, final String nodePath, final XsdAdapterCtx adapterCtx) {
        final XmlSchemaNodeMap.SchemaNodeMap schemaNodeMap = adapterCtx.findOrCreateSchemaNodeMap(systemId);
        final String localName = XsdNameUtils.getReferenceName(nodePos);
        final Optional<SchemaNode> schemaNodeOpt = schemaNodeMap.findSchemaNode(nodePath);

        schemaNodeOpt.ifPresent(schemaNode ->
            LOG.debug("{}Reference definition of node already exists. systemId='{}', refLocalName='{}'",
                    logHeader(XSD_REFERENCE), systemId, localName)
        );

        return schemaNodeOpt.orElseGet(() -> {
            LOG.info("{}Creating reference definition node. systemId='{}', refLocalName='{}'",
                    logHeader(XSD_REFERENCE), systemId, localName);

            final SchemaNode schemaNode = new SchemaNode(nodePos);
            schemaNodeMap.addNode(nodePath, schemaNode);
            return schemaNode;
        });
    }

}
