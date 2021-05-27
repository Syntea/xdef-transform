package org.xdef.transform.xsd.xd2schema.model;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * @author smid
 * @since 2021-05-27
 */
public interface XmlSchemaNodeMap {

    /**
     * Finds XDefinition node map by given schema name
     * @param schemaName        XML schema name
     * @return  XDefinition node map if found
     *          otherwise {@link Optional#empty()}
     */
    Optional<SchemaNodeMap> findByXmlSchema(String schemaName);

    // ====================
    // Basic Map interface
    // ====================

    SchemaNodeMap computeIfAbsent(String key, Function<? super String, ? extends SchemaNodeMap> mappingFunction);

    Set<Map.Entry<String, SchemaNodeMap>> entrySet();

    /**
     * Key:     node path
     * Value:   schema node
     *
     * @author smid
     * @since 2021-05-21
     */
    interface SchemaNodeMap {

        /**
         * Finds schema node in map
         * @param nodePath      schema node path
         * @return  schema node if exist
         *          otherwise {@link Optional#empty()}
         */
        Optional<SchemaNode> findSchemaNode(String nodePath);

        /**
         * Insert schema node into map
         * @param nodePath      schema node path
         * @param schemaNode    schema node
         */
        void addNode(String nodePath, SchemaNode schemaNode);

        /**
         * Removes node from map if exist
         * @param nodePath      schema node path
         * @return  removed node if existed
         *          otherwise {@link Optional#empty()}
         */
        Optional<SchemaNode> removeNode(String nodePath);

        // ====================
        // Basic Map interface
        // ====================

        boolean isEmpty();

        Set<Map.Entry<String, SchemaNode>> entrySet();

        Collection<SchemaNode> values();

    }

}
