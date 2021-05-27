package org.xdef.transform.xsd.xd2schema.model.impl;

import org.xdef.transform.xsd.xd2schema.model.SchemaFileNameLocationMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

/**
 * Key: system name (XML Schema output file name)
 * Value: schema location
 */
public class DefaultSchemaFileNameLocationMap extends HashMap<String, XsdSchemaImportLocation> implements SchemaFileNameLocationMap {

    @Override
    public Optional<XsdSchemaImportLocation> findSchemaLocation(String schemaFileName) {
        return Optional.ofNullable(get(schemaFileName));
    }

    @Override
    public void addSchema(String schemaFileName, XsdSchemaImportLocation schemaImportLocation) {
        put(schemaFileName, schemaImportLocation);
    }

    @Override
    public Set<String> getSchemaFileNames() {
        return keySet();
    }

    @Override
    public Collection<XsdSchemaImportLocation> getSchemaLocations() {
        return values();
    }

}
