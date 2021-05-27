package org.xdef.transform.xsd.xd2schema.model;

import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaComplexContentExtension;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaGroup;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.utils.XmlSchemaNamed;
import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdef.impl.XElement;
import org.xdef.impl.XNode;
import org.xdef.model.XMNode;
import org.xdef.transform.xsd.schema2xd.util.XdNameUtils;
import org.xdef.transform.xsd.xd2schema.error.SchemaNodeException;
import org.xdef.transform.xsd.xd2schema.util.XsdNameUtils;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.xdef.transform.xsd.NamespaceConst.NAMESPACE_DELIMITER;
import static org.xdef.transform.xsd.XDefConst.XDEF_REF_DELIMITER;
import static org.xdef.transform.xsd.util.LoggingUtil.logHeader;
import static org.xdef.transform.xsd.xd2schema.definition.Xd2XsdLogGroup.XSD_REFERENCE;

/**
 * Couples X-Definition nodes with XML Schema nodes. Saves binding between element references.
 * Nodes are created in transformation phase and used for advanced post-processing.
 *
 * Supported types of X-Definition nodes:
 *      element ({@link XElement})
 *      attribute ({@link org.xdef.impl.XData}, kind {@link XNode.XMATTRIBUTE})
 *
 * Supported types of XML Schema nodes:
 *      element ({@link XmlSchemaElement})
 *      attribute ({@link XmlSchemaAttribute})
 *      complex-type ({@link XmlSchemaComplexType})
 *      complex-content-extension ({@link XmlSchemaComplexContentExtension})
 *      group ({@link XmlSchemaGroup})
 */
public class SchemaNode {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaNode.class);

    /**
     * X-definition position of node
     */
    private String xdPosition;

    /**
     * X-definition node
     */
    private XMNode xdNode;

    /**
     * XML Schema document node
     */
    private XmlSchemaObjectBase xsdNode;

    /**
     * Referenced node
     */
    private SchemaNode reference = null;

    /**
     * Nodes which has reference to this node
     */
    private List<SchemaNode> pointers = null;

    public SchemaNode(String xdPosition) {
        this.xdPosition = xdPosition;
    }

    public SchemaNode(String xdPosition, XmlSchemaObject xsdNode, XMNode xdNode) {
        this.xdPosition = xdPosition;
        this.xsdNode = xsdNode;
        this.xdNode = xdNode;
    }

    public String getXdPosition() {
        return xdPosition;
    }

    public Optional<XmlSchemaObjectBase> getXsdNode() {
        return Optional.ofNullable(xsdNode);
    }

    /**
     * Sets XML Schema node and updates X-Definition position
     * @param xsdNode   XML Schema node
     */
    public void setXsdNode(XmlSchemaObjectBase xsdNode) {
        this.xsdNode = xsdNode;

        if (xsdNode instanceof XmlSchemaNamed) {
            final XmlSchemaNamed xsdNamedNode = (XmlSchemaNamed)xsdNode;
            final QName qName = xsdNamedNode.getQName();
            if (qName != null) {
                final String nsPrefix = xsdNamedNode.getParent()
                        .getNamespaceContext()
                        .getPrefix(qName.getNamespaceURI());

                int systemDelPos = xdPosition.indexOf(XDEF_REF_DELIMITER);
                if (nsPrefix != null) {
                    if (systemDelPos != -1) {
                        xdPosition = xdPosition.substring(0, systemDelPos + 1)
                                .concat(nsPrefix + NAMESPACE_DELIMITER + xsdNamedNode.getName());
                    } else {
                        xdPosition = nsPrefix + NAMESPACE_DELIMITER + xsdNamedNode.getName();
                    }
                } else {
                    if (systemDelPos != -1) {
                        xdPosition = xdPosition.substring(0, systemDelPos + 1).concat(xsdNamedNode.getName());
                    } else {
                        xdPosition = xsdNamedNode.getName();
                    }
                }
            }
        }
    }

    public Optional<XMNode> getXdNode() {
        return Optional.ofNullable(xdNode);
    }

    public XMNode getXdNodeReq() {
        return getXdNode().orElseThrow(() -> new SchemaNodeException("Schema node does not contain required related " +
                "X-definition node."));
    }

    public Optional<SchemaNode> getReference() {
        return Optional.ofNullable(reference);
    }

    public List<SchemaNode> getPointers() {
        return Optional.ofNullable(pointers).orElse(Collections.emptyList());
    }

    public void copyNodes(SchemaNode src) {
        this.xsdNode = src.xsdNode;
        this.xdNode = src.xdNode;
    }

    /**
     *
     * @return true if XML Schema node is element
     */
    public boolean isXsdElem() {
        return (xsdNode instanceof XmlSchemaElement);
    }

    /**
     *
     * @return true if XML Schema node is attribute
     */
    public boolean isXsdAttr() {
        return (xsdNode instanceof XmlSchemaAttribute);
    }

    /**
     *
     * @return true if XML Schema node is complex type
     */
    public boolean isXsdComplexType() {
        return (xsdNode instanceof XmlSchemaComplexType);
    }

    /**
     *
     * @return true if XML Schema node is complex content extension
     */
    public boolean isXsdComplexExt() {
        return (xsdNode instanceof XmlSchemaComplexContentExtension);
    }

    /**
     *
     * @return true if XML Schema node is group
     */
    public boolean isXsdGroup() {
        return (xsdNode instanceof XmlSchemaGroup);
    }

    public XmlSchemaElement toXsdElem() {
        return (XmlSchemaElement)xsdNode;
    }

    public XmlSchemaAttribute toXsdAttr() {
        return (XmlSchemaAttribute)xsdNode;
    }

    public XmlSchemaComplexType toXsdComplexType() {
        return (XmlSchemaComplexType)xsdNode;
    }

    public XmlSchemaComplexContentExtension toXsdComplexExt() {
        return (XmlSchemaComplexContentExtension)xsdNode;
    }

    public XmlSchemaGroup toXsdGroup() {
        return (XmlSchemaGroup)xsdNode;
    }

    /**
     *
     * @return true if X-Definition node is element
     */
    public boolean isXdElem() {
        return xdNode != null && xdNode.getKind() == XNode.XMELEMENT;
    }

    /**
     *
     * @return true if X-Definition node is attribute
     */
    public boolean isXdAttr() {
        return xdNode != null && xdNode.getKind() == XNode.XMATTRIBUTE;
    }

    public XElement toXdElem() {
        return (XElement)xdNode;
    }

    /**
     * @return X-Definition node name
     */
    public String getXdName() {
        return xdNode.getName();
    }

    /**
     * @return X-Definition node local name
     */
    public String getXdLocalName() {
        return XdNameUtils.getLocalName(xdNode.getName());
    }

    /**
     *
     * @return true if any node is referencing to current node
     */
    public boolean hasAnyPointer() {
        return pointers != null && !pointers.isEmpty();
    }

    /**
     *
     * @return true if node has reference
     */
    public boolean hasReference() {
        return reference != null;
    }

    /**
     * Add node which refers to current instance
     * @param ptr referencing node
     */
    private void addPointer(final SchemaNode ptr) {
        if (pointers == null) {
            pointers = new LinkedList<>();
        }

        pointers.add(ptr);
    }

    /**
     * Set reference definition
     * @param reference reference definition
     */
    private void setReference(final SchemaNode reference) {
        this.reference = reference;
    }

    /**
     * Creates binding between referencing node and reference definition
     * @param ptr       node containing reference
     * @param ref       node reference definition
     */
    public static void createBinding(final SchemaNode ptr, final SchemaNode ref) {
        LOG.info("{}Creating binding between nodes. from='{}', to='{}'",
                logHeader(XSD_REFERENCE), ptr.getXdPosition(), ref.getXdPosition());

        ptr.setReference(ref);
        ref.addPointer(ptr);
    }


    /**
     * Get X-Definition reference node path for post processing
     * @param refPos    X-Definition reference node position
     * @return  position for post processing
     */
    public static String getPostProcessingReferenceNodePath(final String refPos) {
        int xdefSystemSeparatorPos = refPos.indexOf('/');
        if (xdefSystemSeparatorPos != -1) {
            return refPos.substring(xdefSystemSeparatorPos + 1);
        }

        return XsdNameUtils.getXNodePath(refPos);
    }

    /**
     * Get X-Definition reference node position for post processing
     * @param systemId          XML Schema document name
     * @param xDefNodePos       XDefinition node position
     * @return X-Definition node position in X-Definition
     */
    public static String getPostProcessingNodePos(final String systemId, final String xDefNodePos) {
        return systemId + XDEF_REF_DELIMITER + xDefNodePos;
    }
}
