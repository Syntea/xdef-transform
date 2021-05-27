package org.xdef.transform.xsd.xd2schema.model;

import org.xdef.transform.xsd.xd2schema.model.impl.XsdSchemaImportLocation;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * Schema name location map
 *
 * @author smid
 * @since 2021-05-21
 */
public interface SchemaFileNameLocationMap {

    /**
     * Finds schema location based on schema name
     * @param schemaFileName        XML Schema file name
     * @return  schema location if found
     *          otherwise {@link Optional#empty()}
     */
    Optional<XsdSchemaImportLocation> findSchemaLocation(String schemaFileName);

    /**
     * Inserts schema location
     * @param schemaFileName            XML Schema file name
     * @param schemaImportLocation      XML Schema import location
     */
    void addSchema(String schemaFileName, XsdSchemaImportLocation schemaImportLocation);

    /**
     * @return  XML Schema file names
     */
    Set<String> getSchemaFileNames();

    /**
     * @return  XML Schema file locations
     */
    Collection<XsdSchemaImportLocation> getSchemaLocations();

    // ====================
    // Basic Map interface
    // ====================

    int size();

    boolean isEmpty();

}
