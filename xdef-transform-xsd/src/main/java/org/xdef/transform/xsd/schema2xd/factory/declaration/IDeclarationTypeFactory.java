package org.xdef.transform.xsd.schema2xd.factory.declaration;

import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.xdef.sys.ReportWriter;

import java.util.List;

/**
 * Transform XML Schema restrictions/facets into X-Definition declaration type
 */
public interface IDeclarationTypeFactory {

    String FACET_MIN_INCLUSIVE = "MIN_INCLUSIVE";
    String FACET_MIN_EXCLUSIVE = "MIN_EXCLUSIVE";
    String FACET_MAX_INCLUSIVE = "MAX_INCLUSIVE";
    String FACET_MAX_EXCLUSIVE = "MAX_EXCLUSIVE";
    String FACET_PATTERN = "PATTERN";
    String FACET_LENGTH = "LENGTH";
    String FACET_MIN_LENGTH = "MIN_LENGTH";
    String FACET_MAX_LENGTH = "MAX_LENGTH";
    String FACET_TOTAL_DIGITS = "TOTAL_DIGITS";
    String FACET_FRACTIONS_DIGITS = "FRACTIONS_DIGITS";
    String FACET_WHITESPACE = "WHITESPACE";
    String FACET_ENUMERATION = "ENUMERATION";

    String XD_FACET_ENUMERATION_TYPE = "enum";

    enum Type {
        TOP_DECL,           // Used for top level xd:declaration nodes
        TEXT_DECL,          // Used for definition of text value of element
        DATATYPE_DECL       // Used for building only data type from facets (ie. attribute type, list item type)
    }

    /**
     * Set type of X-Definition declaration
     * @param type      type of X-Definition declaration
     */
    void setType(final Type type);

    /**
     * Set declaration variable name.
     * Use only with mode {@link Type.TOP_DECL}
     * @param typeName      X-Definition declaration variable name
     */
    void setName(final String typeName);

    /**
     * Get variable data type
     * @return X-Definition variable data type
     */
    String getDataType();

    /**
     * Creates X-Definition declaration type restrictions based on given XML Schema facets
     * @param facets        list of XML Schema facets
     * @param reportWriter  output report writer
     * @return X-Definition restriction
     */
    String build(final List<XmlSchemaFacet> facets, ReportWriter reportWriter);

    /**
     * Creates X-Definition declaration type restrictions from given facets string
     * @param facets    list of XML Schema facets
     * @param reportWriter  output report writer
     * @return X-Definition restriction
     */
    String build(final String facets, ReportWriter reportWriter);

}
