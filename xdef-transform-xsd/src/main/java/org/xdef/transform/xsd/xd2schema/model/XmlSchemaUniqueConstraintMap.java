package org.xdef.transform.xsd.xd2schema.model;

import org.xdef.transform.xsd.xd2schema.model.uc.UniqueConstraint;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * Unique constraint nodes which will be created in post-procession
 *
 * Key:     schema name
 * Value:   X-Definition uniqueSet map
 *
 * @author smid
 * @since 2021-05-27
 */
public interface XmlSchemaUniqueConstraintMap {

    /**
     * Finds X-Definition uniqueSet map by given schema name
     * @param schemaName        XML Schema name
     * @return  X-Definition uniqueSet map if found
     *          otherwise {@link Optional#empty()}
     */
    Optional<XDefUniqueSetMap> findByXmlSchema(String schemaName);

    // ====================
    // Basic Map interface
    // ====================

    XDefUniqueSetMap computeIfAbsent(String key, Function<? super String, ? extends XDefUniqueSetMap> mappingFunction);

    /**
     * Key:     xpath to uniqueSet
     * Value:   unique info
     */
    interface XDefUniqueSetMap {

        /**
         * Finds list of unique constraints by given xpath
         * @param xpath         required unique constraint xpath
         * @return  X-Definition uniqueSet map if found
         *          otherwise {@link Collections#emptyList()}
         */
        List<UniqueConstraint> findByXPath(String xpath);

        // ====================
        // Basic Map interface
        // ====================

        List<UniqueConstraint> computeIfAbsent(String key, Function<? super String, ? extends List<UniqueConstraint>> mappingFunction);

        Set<Map.Entry<String, List<UniqueConstraint>>> entrySet();

        boolean isEmpty();
    }

}
