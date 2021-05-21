package org.xdef.transform.xsd.xd2schema.model;

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
     * @param schemaFileName
     * @return
     */
    Optional<XsdSchemaImportLocation> findSchemaLocation(String schemaFileName);

    /**
     * Inserts schema location
     * @param schemaFileName
     * @param schemaImportLocation
     * @return
     */
    void addSchema(String schemaFileName, XsdSchemaImportLocation schemaImportLocation);

    /**
     * Returns XML schema file names
     * @return
     */
    Set<String> getSchemaFileNames();

    /**
     * Returns XML schema file locations
     * @return
     */
    Collection<XsdSchemaImportLocation> getSchemaLocations();

    int size();

    boolean isEmpty();

}
