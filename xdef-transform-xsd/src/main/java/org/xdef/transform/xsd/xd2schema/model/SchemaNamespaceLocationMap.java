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
     * Add XML Schema document location into map
     * @param nsUri             XML Schema document namespace URI
     * @param importLocation    XML Schema document location definition
     */
    XsdSchemaImportLocation addSchemaLocation(final String nsUri, final XsdSchemaImportLocation importLocation);

    /**
     * Add XML Schema document into map. Document location is created internally.
     * @param nsPrefix          XML Schema document namespace prefix
     * @param nsUri             XML Schema document namespace URI
     */
    XsdSchemaImportLocation addSchemaLocation(final String nsPrefix, final String nsUri);

    /**
     * Finds XML Schema document location if exists by given namespace URI
     * @param nsUri             XML Schema document namespace URI
     * @return  XML Schema document location if exists
     *          otherwise {@link Optional#empty()}
     */
    Optional<XsdSchemaImportLocation> findSchemaImport(final String nsUri, final String schemaName);

    /**
     * Finds XML Schema document locations by given namespace URI
     * @param nsUri             XML Schema document namespace URI
     * @return  XML Schema document location if exists
     *          otherwise empty list
     */
    List<XsdSchemaImportLocation> findSchemaImports(final String nsUri);

    /**
     * Finds XML Schema location map
     * @param nsUri             XML Schema namespace URI
     * @return  XML Schema location map if exists
     *          otherwise {@link Optional#empty()}
     */
    Optional<SchemaFileNameLocationMap> findSchemaFileLocationMap(final String nsUri);

    /**
     * Returns true if map contains location map for given namespace URI
     * @param nsUri             XML Schema namespace URI
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
