package org.xdef.transform.xsd.xd2schema.model;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Schema namespace location map
 *
 * @author smid
 * @since 2021-05-21
 */
public interface SchemaNamespaceLocationMap {

    /**
     * Add XSD document location into map
     * @param nsUri             XML schema document namespace URI
     * @param importLocation    XML schema document location definition
     */
    XsdSchemaImportLocation addSchemaLocation(final String nsUri, final XsdSchemaImportLocation importLocation);

    /**
     * Add XSD document into map. Document location is created internally.
     * @param nsPrefix          XML schema document namespace prefix
     * @param nsUri             XML schema document namespace URI
     */
    XsdSchemaImportLocation addSchemaLocation(final String nsPrefix, final String nsUri);

    /**
     * Finds XSD document location if exists by given namespace URI
     * @param nsUri             XML schema document namespace URI
     * @return  XML schema document location if exists
     *          otherwise {@link Optional#empty()}
     */
    Optional<XsdSchemaImportLocation> findSchemaImport(final String nsUri, final String schemaName);

    /**
     * Finds XSD document locations by given namespace URI
     * @param nsUri             XML schema document namespace URI
     * @return  XML schema document location if exists
     *          otherwise empty list
     */
    List<XsdSchemaImportLocation> findSchemaImports(final String nsUri);

    /**
     * Finds XML schema location map
     * @param nsUri             XML schema namespace URI
     * @return  XML schema location map if exists
     *          otherwise {@link Optional#empty()}
     */
    Optional<SchemaFileNameLocationMap> findSchemaFileLocationMap(final String nsUri);

    /**
     * Returns true if map contains location map for given namespace URI
     * @param nsUri             XML schema namespace URI
     */
    boolean containsSchemaFileLocationMap(final String nsUri);

    // ====================
    // Basic Map interface
    // ====================

    boolean isEmpty();

    int size();

    Set<Map.Entry<String, SchemaFileNameLocationMap>> entrySet();

    Object clone();

}
