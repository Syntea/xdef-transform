package org.xdef.transform.xsd.xd2schema.model;

import org.xdef.transform.xsd.xd2schema.model.uc.UniqueConstraint;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * @author smid
 * @since 2021-05-27
 */
public class DefaultXmlSchemaUniqueConstraintMap extends HashMap<String, XmlSchemaUniqueConstraintMap.XDefUniqueSetMap>  implements XmlSchemaUniqueConstraintMap {

    @Override
    public Optional<XDefUniqueSetMap> findByXmlSchema(String schemaName) {
        return Optional.ofNullable(super.get(schemaName));
    }

    public static class DefaultXDefUniqueSetMap extends HashMap<String, List<UniqueConstraint>> implements XDefUniqueSetMap {

        @Override
        public List<UniqueConstraint> findByXPath(String xPath) {
            return super.getOrDefault(xPath, Collections.emptyList());
        }

    }
}
