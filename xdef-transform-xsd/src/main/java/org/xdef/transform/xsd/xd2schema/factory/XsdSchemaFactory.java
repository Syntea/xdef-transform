package org.xdef.transform.xsd.xd2schema.factory;


import org.apache.commons.lang3.tuple.Pair;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaForm;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdef.impl.XDefinition;
import org.xdef.impl.XElement;
import org.xdef.impl.XNode;
import org.xdef.model.XMNode;
import org.xdef.transform.xsd.msg.XSD;
import org.xdef.transform.xsd.xd2schema.model.impl.XsdAdapterCtx;
import org.xdef.transform.xsd.xd2schema.util.XsdNameUtils;
import org.xdef.transform.xsd.xd2schema.util.XsdNamespaceUtils;

import java.util.Map;

import static org.xdef.transform.xsd.def.NamespaceConst.XML_SCHEMA_DEFAULT_NAMESPACE_URI;
import static org.xdef.transform.xsd.def.NamespaceConst.XML_SCHEMA_DEFAULT_NAMESPACE_PREFIX;
import static org.xdef.transform.xsd.util.LoggingUtil.logHeader;
import static org.xdef.transform.xsd.xd2schema.def.AlgPhase.INITIALIZATION;

/**
 * Creates and initialize XML Schema document
 */
public class XsdSchemaFactory {

    private static final Logger LOG = LoggerFactory.getLogger(XsdSchemaFactory.class);

    private final XsdAdapterCtx adapterCtx;

    public XsdSchemaFactory(XsdAdapterCtx adapterCtx) {
        this.adapterCtx = adapterCtx;
    }

    /**
     * Creates and initialize XML Schema document
     * @param xDef              source X-Definition
     * @param targetNamespace   target namespace (prefix, URI) of input X-Definition
     * @return empty initialized XML Schema document
     */
    public XmlSchema createXsdSchema(final XDefinition xDef, Pair<String, String> targetNamespace) {
        LOG.info("{}Initialize XML Schema document.", logHeader(INITIALIZATION, xDef));

        if (targetNamespace != null) {
            LOG.info("{}Creating XML Schema document. systemName='{}', targetNamespacePrefix='{}', targetNamespaceUri='{}'",
                    logHeader(INITIALIZATION, xDef),
                    xDef.getName(),
                    targetNamespace.getKey(),
                    targetNamespace.getValue());
        } else {
            LOG.info("{}Creating XML Schema document. systemName='{}'", logHeader(INITIALIZATION, xDef), xDef.getName());
            targetNamespace = Pair.of(null, null);
        }

        final String schemaName = XsdNameUtils.getSchemaName(xDef);

        adapterCtx.addSchemaName(schemaName);

        final XmlSchema xmlSchema = new XmlSchema(targetNamespace.getValue(), schemaName, adapterCtx.getXmlSchemaCollection());
        initSchemaNamespace(xmlSchema, xDef, targetNamespace);
        initSchemaFormDefault(xmlSchema, xDef, targetNamespace);
        return xmlSchema;
    }

    /**
     * Initialize XML Schema document namespace context
     * @param xmlSchema         XML Schema document to be initialized
     * @param xDef              source X-Definition
     * @param targetNamespace   XML Schema document target namespace (prefix, URI)
     */
    private void initSchemaNamespace(final XmlSchema xmlSchema,
                                     final XDefinition xDef,
                                     final Pair<String, String> targetNamespace) {
        LOG.debug("{}Initializing namespace context ...", logHeader(INITIALIZATION, xDef));

        final String targetNsPrefix = targetNamespace.getKey();
        final String targetNsUri = targetNamespace.getValue();

        if (targetNamespace.getKey() != null && targetNamespace.getValue() != null) {
            xmlSchema.setSchemaNamespacePrefix(targetNamespace.getKey());
        }

        NamespaceMap namespaceCtx = new NamespaceMap();
        // Default XML Schema namespace with prefix xs
        namespaceCtx.add(XML_SCHEMA_DEFAULT_NAMESPACE_PREFIX, XML_SCHEMA_DEFAULT_NAMESPACE_URI);

        if (targetNsPrefix != null && targetNsUri != null) {
            XsdNamespaceUtils.addNamespaceToCtx(namespaceCtx, targetNsPrefix, targetNsUri, xDef.getName(), INITIALIZATION);
        }

        for (Map.Entry<String, String> entry : xDef._namespaces.entrySet()) {
            final String nsPrefix = entry.getKey();
            final String nsUri = entry.getValue();

            if (XsdNamespaceUtils.isDefaultNamespacePrefix(nsPrefix) || nsPrefix.equals(targetNsPrefix)) {
                continue;
            }

            if (!namespaceCtx.containsKey(nsPrefix)) {
                XsdNamespaceUtils.addNamespaceToCtx(namespaceCtx, nsPrefix, nsUri, xDef.getName(), INITIALIZATION);
            } else {
                adapterCtx.getReportWriter().warning(XSD.XSD029, nsPrefix, nsUri);
                LOG.warn("{}Namespace has been already defined! nsPrefix='{}', nsUri='{}'",
                        logHeader(INITIALIZATION, xDef), nsPrefix, nsUri);
            }
        }

        xmlSchema.setNamespaceContext(namespaceCtx);
    }

    /**
     * Sets attributeFormDefault and elementFormDefault to XML Schema document
     * @param xmlSchema         XML Schema document to be initialized
     * @param xDef              source X-Definition
     * @param targetNamespace   XML Schema document target namespace (prefix, URI)
     */
    private void initSchemaFormDefault(final XmlSchema xmlSchema,
                                       final XDefinition xDef,
                                       final Pair<String, String> targetNamespace) {
        final XmlSchemaForm elemSchemaForm = getElemDefaultForm(xDef, targetNamespace.getKey());
        xmlSchema.setElementFormDefault(elemSchemaForm);

        final XmlSchemaForm attrSchemaForm = getAttrDefaultForm(xDef, targetNamespace.getKey());
        xmlSchema.setAttributeFormDefault(attrSchemaForm);

        LOG.debug("{}Setting element default schema form. form='{}'", logHeader(INITIALIZATION, xDef), elemSchemaForm);
        LOG.debug("{}Setting attribute default schema form. form='{}'", logHeader(INITIALIZATION, xDef), attrSchemaForm);
    }

    /**
     * Determines default element schema form for XML Schema document
     * @param xDef              input X-Definition
     * @param targetNsPrefix    X-Definition target namespace prefix
     * @return schema form
     */
    private XmlSchemaForm getElemDefaultForm(final XDefinition xDef, final String targetNsPrefix) {
        if (targetNsPrefix != null && targetNsPrefix.trim().isEmpty()) {
            LOG.debug("{}Target namespace prefix is empty. Element default form will be Qualified.",
                    logHeader(INITIALIZATION, xDef));

            return XmlSchemaForm.QUALIFIED;
        }

        if (xDef._rootSelection != null && xDef._rootSelection.size() > 0) {
            for (XNode xn : xDef._rootSelection.values()) {
                if (xn.getKind() == XNode.XMELEMENT) {
                    final XElement defEl = (XElement)xn;
                    final String tmpNs = XsdNamespaceUtils.getNamespacePrefix(defEl.getName()).orElse(null);
                    if (tmpNs != null && tmpNs.equals(targetNsPrefix)) {
                        LOG.debug("{}One of the root elements uses different namespace prefix. " +
                                "Element default form will be Qualified. expectedPrefix='{}'",
                                logHeader(INITIALIZATION, xDef), targetNsPrefix);

                        return XmlSchemaForm.QUALIFIED;
                    }
                }
            }
        }

        LOG.debug("{}All of the root root elements use same namespace prefix. Element default form will be Unqualified.",
                logHeader(INITIALIZATION, xDef));
        return XmlSchemaForm.UNQUALIFIED;
    }

    /**
     * Determines default attribute schema form for XML Schema document
     * @param xDef              input X-Definition
     * @param targetNsPrefix    X-Definition target namespace prefix
     * @return schema form
     */
    private XmlSchemaForm getAttrDefaultForm(final XDefinition xDef, final String targetNsPrefix) {
        if (xDef._rootSelection != null && xDef._rootSelection.size() > 0) {
            for (XNode xn : xDef._rootSelection.values()) {
                if (xn.getKind() == XNode.XMELEMENT) {
                    XElement defEl = (XElement)xn;
                    for (XMNode attr : defEl.getAttrs()) {
                        String tmpNs = XsdNamespaceUtils.getNamespacePrefix(attr.getName()).orElse(null);
                        if (tmpNs != null && tmpNs.equals(targetNsPrefix)) {
                            LOG.debug("{}One of the root attributes uses different namespace prefix. " +
                                    "Attribute default form will be Qualified. expectedPrefix='{}'",
                                    logHeader(INITIALIZATION, xDef), targetNsPrefix);

                            return XmlSchemaForm.QUALIFIED;
                        }
                    }
                }
            }
        }

        LOG.debug("{}All of the root attributes use same namespace prefix. Attribute default form will be Unqualified",
                logHeader(INITIALIZATION, xDef));
        return XmlSchemaForm.UNQUALIFIED;
    }
}
