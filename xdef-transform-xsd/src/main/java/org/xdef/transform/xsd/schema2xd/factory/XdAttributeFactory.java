package org.xdef.transform.xsd.schema2xd.factory;

import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaForm;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction;
import org.apache.ws.commons.schema.XmlSchemaUse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.xdef.transform.xsd.msg.XSD;
import org.xdef.transform.xsd.schema2xd.factory.declaration.IDeclarationTypeFactory;
import org.xdef.transform.xsd.schema2xd.model.XdAdapterCtx;
import org.xdef.transform.xsd.schema2xd.util.XdNameUtils;

import javax.xml.namespace.QName;

import static org.xdef.transform.xsd.NamespaceConst.XDEF_DEFAULT_NAMESPACE_URI;
import static org.xdef.transform.xsd.XDefConst.XDEF_REF;
import static org.xdef.transform.xsd.XDefConst.XDEF_REF_DELIMITER;
import static org.xdef.transform.xsd.schema2xd.definition.Xsd2XdDefinitions.XD_ATTR_SCRIPT;
import static org.xdef.transform.xsd.schema2xd.definition.Xsd2XdDefinitions.XD_ATTR_TEXT;
import static org.xdef.transform.xsd.schema2xd.definition.Xsd2XdFeature.XD_EXPLICIT_OCCURRENCE;
import static org.xdef.transform.xsd.schema2xd.definition.Xsd2XdFeature.XD_MIXED_REQUIRED;
import static org.xdef.transform.xsd.util.LoggingUtil.logHeader;
import static org.xdef.transform.xsd.xd2schema.definition.AlgPhase.TRANSFORMATION;

/**
 * Creates X-Definition node's attributes
 */
public class XdAttributeFactory {

    private static final Logger LOG = LoggerFactory.getLogger(XdAttributeFactory.class);

    /**
     * X-definition adapter context
     */
    final private XdAdapterCtx adapterCtx;

    /**
     * X-definition declaration node factory
     */
    final private XdDeclarationFactory xdDeclarationFactory;

    public XdAttributeFactory(XdAdapterCtx adapterCtx, XdDeclarationFactory xdDeclarationFactory) {
        this.adapterCtx = adapterCtx;
        this.xdDeclarationFactory = xdDeclarationFactory;
    }

    /**
     * Add attribute to given X-Definition node
     * @param el            X-Definition node
     * @param attrName      attribute name
     * @param attrValue     attribute value
     */
    public static void addAttr(final Element el, final String attrName, final String attrValue) {
        LOG.debug("{}Add attribute. name='{}', value='{}'", logHeader(TRANSFORMATION, el), attrName, attrValue);
        el.setAttribute(attrName, attrValue);
    }

    /**
     * Add attribute based on input XSD attribute to given X-Definition node
     * @param el                X-Definition node
     * @param xsdAttr           XSD attribute node
     * @param xDefName          XSD document name
     */
    public void addAttr(final Element el, final XmlSchemaAttribute xsdAttr, final String xDefName) {
        LOG.debug("{}Add attribute. qName='{}'", logHeader(TRANSFORMATION, el), xsdAttr.getQName());

        final String attribute = createAttribute(xsdAttr);

        if (xsdAttr.isRef()) {
            final QName xsdQName = xsdAttr.getRef().getTargetQName();
            if (xsdQName != null) {
                final String qualifiedName = XdNameUtils.createQualifiedName(xsdQName, xDefName, adapterCtx);
                el.setAttributeNS(xsdQName.getNamespaceURI(), qualifiedName, attribute);
            } else {
                adapterCtx.getReportWriter().warning(XSD.XSD213);
                LOG.warn("{}Unknown attribute reference QName!", logHeader(TRANSFORMATION, xsdAttr));
            }
        } else {
            final QName xsdQName = xsdAttr.getQName();
            if (xsdQName != null && xsdQName.getNamespaceURI() != null
                    && !XmlSchemaForm.UNQUALIFIED.equals(xsdAttr.getForm())) {
                final String qualifiedName = XdNameUtils.createQualifiedName(xsdQName, xDefName, adapterCtx);
                el.setAttributeNS(xsdQName.getNamespaceURI(), qualifiedName, attribute);
            } else {
                el.setAttribute(xsdAttr.getName(), attribute);
            }
        }
    }

    /**
     * Add reference attribute into given X-Definition element node
     * @param el        X-Definition element node
     * @param qName     reference qualified name
     */
    public static void addAttrRef(final Element el, final QName qName) {
        addAttrXDef(el, XD_ATTR_SCRIPT, XDEF_REF + ' ' + qName.getLocalPart());
    }

    /**
     * Add reference (in different X-Definition) attribute into given X-Definition element node
     * @param el        X-Definition element node
     * @param xDefName  X-Definition name
     * @param qName     reference qualified name
     */
    public static void addAttrRefInDiffXDef(final Element el, final String xDefName, final QName qName) {
        addAttrXDef(el, XD_ATTR_SCRIPT,
                XDEF_REF + ' ' + xDefName + XDEF_REF_DELIMITER + XdNameUtils.createQualifiedName(qName));
    }

    /**
     * Add or append value to currently existing X-Definition attribute (using namespace {@value Xsd2XdDefinitions.XD_NAMESPACE_URI})
     * to given X-Definition element node
     * @param el        X-Definition element node
     * @param qName     reference qualified name
     * @param value     attribute value
     */
    private static void addAttrXDef(final Element el, final String qName, final String value) {
        LOG.debug("{}Add X-Definition attribute. qName='{}', value='{}'",
                logHeader(TRANSFORMATION, el), qName, value);
        final String localName = XdNameUtils.getLocalName(qName);
        final Attr attr = el.getAttributeNodeNS(XDEF_DEFAULT_NAMESPACE_URI, localName);
        if (attr != null) {
            el.setAttributeNS(XDEF_DEFAULT_NAMESPACE_URI, qName, attr.getValue() + "; " + value);
        } else {
            el.setAttributeNS(XDEF_DEFAULT_NAMESPACE_URI, qName, value);
        }
    }

    /**
     * Add X-Definition occurrence attribute into given X-Definition element node
     * @param xdNode        X-Definition node
     * @param xsdNode       XSD document node containing occurrence info
     */
    public void addOccurrence(final Element xdNode, final XmlSchemaParticle xsdNode) {
        if (xsdNode.getMaxOccurs() == 1 && xsdNode.getMinOccurs() == 1) {
            if (adapterCtx.hasEnableFeature(XD_EXPLICIT_OCCURRENCE)) {
                addAttrXDef(xdNode, XD_ATTR_SCRIPT, "occurs 1");
            }
            return;
        }

        if (xsdNode.getMaxOccurs() == Long.MAX_VALUE) {
            if (xsdNode.getMinOccurs() == 0) {
                addAttrXDef(xdNode, XD_ATTR_SCRIPT, "occurs *");
                return;
            }

            if (xsdNode.getMinOccurs() == 1) {
                addAttrXDef(xdNode, XD_ATTR_SCRIPT, "occurs +");
                return;
            }

            addAttrXDef(xdNode, XD_ATTR_SCRIPT, "occurs " + xsdNode.getMinOccurs() + "..*");
            return;
        }

        if (xsdNode.getMinOccurs() == 0 && xsdNode.getMaxOccurs() == 1) {
            addAttrXDef(xdNode, XD_ATTR_SCRIPT, "occurs ?");
            return;
        }

        if (xsdNode.getMinOccurs() == xsdNode.getMaxOccurs()) {
            addAttrXDef(xdNode, XD_ATTR_SCRIPT, "occurs " + xsdNode.getMinOccurs());
            return;
        }

        addAttrXDef(xdNode, XD_ATTR_SCRIPT, "occurs " + xsdNode.getMinOccurs() + ".." + xsdNode.getMaxOccurs());
    }

    /**
     * Add X-Definition text attribute into given X-Definition element node
     * @param el        X-Definition element node
     */
    public void addAttrText(final Element el) {
        addAttrXDef(el, XD_ATTR_TEXT, (!adapterCtx.hasEnableFeature(XD_MIXED_REQUIRED) ? "? " : "") + "string()");
    }

    /**
     * Add X-Definition nillable attribute into given X-Definition element node
     * @param el        X-Definition element node
     * @param xsdElem   XSD element node
     */
    public void addAttrNillable(final Element el, final XmlSchemaElement xsdElem) {
        if (xsdElem.isNillable()) {
            addAttrXDef(el, XD_ATTR_SCRIPT, "options nillable");
        }
    }

    /**
     * Creates X-Definition attribute based on given XSD attribute node
     * @param xsdAttr   XSD attribute node
     * @return X-Definition attribute
     */
    private String createAttribute(final XmlSchemaAttribute xsdAttr) {
        LOG.debug("{}Creating attribute.", logHeader(TRANSFORMATION, xsdAttr));

        final StringBuilder valueBuilder = new StringBuilder();
        if (XmlSchemaUse.REQUIRED.equals(xsdAttr.getUse())) {
            valueBuilder.append("required ");
        } else {
            valueBuilder.append("optional ");
        }

        if (xsdAttr.getSchemaTypeName() != null) {
            valueBuilder.append(xsdAttr.getSchemaTypeName().getLocalPart()).append("()");
        } else if (xsdAttr.getSchemaType() != null) {
            if (xsdAttr.getSchemaType().getContent() instanceof XmlSchemaSimpleTypeRestriction) {
                final XdDeclarationBuilder b = xdDeclarationFactory.createBuilder()
                        .setSimpleType(xsdAttr.getSchemaType())
                        .setType(IDeclarationTypeFactory.Type.DATATYPE_DECL);

                valueBuilder.append(xdDeclarationFactory.createDeclarationContent(b));
            }
        }

        if (xsdAttr.getDefaultValue() != null && !xsdAttr.getDefaultValue().isEmpty()) {
            valueBuilder.append("; default \"").append(xsdAttr.getDefaultValue()).append("\"");
        }

        if (xsdAttr.getFixedValue() != null && !xsdAttr.getFixedValue().isEmpty()) {
            valueBuilder.append("; fixed \"").append(xsdAttr.getFixedValue()).append("\"");
        }

        return valueBuilder.toString();
    }
}
