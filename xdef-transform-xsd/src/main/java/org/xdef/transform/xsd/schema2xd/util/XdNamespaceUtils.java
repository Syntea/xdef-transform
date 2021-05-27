package org.xdef.transform.xsd.schema2xd.util;

import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaGroup;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.apache.ws.commons.schema.constants.Constants;
import org.xdef.transform.xsd.schema2xd.model.XdAdapterCtx;

import javax.xml.namespace.QName;
import java.util.Optional;
import java.util.Set;

import static org.xdef.transform.xsd.NamespaceConst.XML_SCHEMA_DEFAULT_NAMESPACE_PREFIX;

public class XdNamespaceUtils {

    /**
     * Checks if given namespace prefix is default for X-Definition
     * @param prefix    namespace prefix
     * @return  return true if if given namespace prefix is default
     */
    public static boolean isDefaultNamespacePrefix(final String prefix) {
        return Constants.XML_NS_PREFIX.equals(prefix)
                || Constants.XMLNS_ATTRIBUTE.equals(prefix)
                || XML_SCHEMA_DEFAULT_NAMESPACE_PREFIX.equals(prefix);
    }

    /**
     * Finds reference schema name from given parameters.
     *
     * If multiple schemas using target namespace equals to reference namespace URI have been found,
     * then we try to find proper schema name by searching node by reference qualified name in particular schema.
     *
     * @param schemaCollection      XSD document collection
     * @param refQName              reference qualified name
     * @param xdAdapterCtx          X-Definition adapter context
     * @param simple                flag, if only reference should be XSD simple type or XSD attribute node
     * @return  XML schema name if found
     *          otherwise {@link Optional#empty()}
     */
    public static Optional<String> findReferenceSchemaName(final XmlSchemaCollection schemaCollection,
                                                           final QName refQName,
                                                           final XdAdapterCtx xdAdapterCtx,
                                                           boolean simple) {
        String schemaName = null;

        final Set<String> refXDefs = xdAdapterCtx.findXDefByNamespace(refQName.getNamespaceURI());
        if (refXDefs.isEmpty()) {
            return Optional.empty();
        }

        if (refXDefs.size() == 1) {
            schemaName = refXDefs.iterator().next();
        } else if (!simple) {
            final XmlSchemaType refSchemaType = schemaCollection.getTypeByQName(refQName);
            if (refSchemaType instanceof XmlSchemaComplexType) {
                schemaName = xdAdapterCtx.findXmlSchemaFileName(refSchemaType.getParent());
            }

            if (schemaName == null) {
                final XmlSchemaGroup refGroup = schemaCollection.getGroupByQName(refQName);
                if (refGroup != null) {
                    schemaName = xdAdapterCtx.findXmlSchemaFileName(refGroup.getParent());
                }
            }

            if (schemaName == null) {
                final XmlSchemaElement refElem = schemaCollection.getElementByQName(refQName);
                if (refElem != null) {
                    schemaName = xdAdapterCtx.findXmlSchemaFileName(refElem.getParent());
                }
            }
        } else {
            final XmlSchemaType refSchemaType = schemaCollection.getTypeByQName(refQName);
            if (refSchemaType instanceof XmlSchemaSimpleType) {
                schemaName = xdAdapterCtx.findXmlSchemaFileName(refSchemaType.getParent());
            }

            final XmlSchemaAttribute refAttr = schemaCollection.getAttributeByQName(refQName);
            if (refAttr != null) {
                schemaName = xdAdapterCtx.findXmlSchemaFileName(refAttr.getParent());
            }
        }

        return Optional.ofNullable(schemaName);
    }

}
