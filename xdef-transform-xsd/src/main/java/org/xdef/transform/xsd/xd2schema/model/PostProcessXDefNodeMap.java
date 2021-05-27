package org.xdef.transform.xsd.xd2schema.model;

import org.xdef.impl.XNode;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

/**
 * Namespace node map
 *
 * Key:     namespace URI
 * Value:   XDefinition node map
 *
 * @author smid
 * @since 2021-05-27
 */
public interface PostProcessXDefNodeMap {

    /**
     * Finds XDefinition node map by given namespace URI
     * @param namespaceUri
     * @return  XDefinition node map if found
     *          otherwise {@link Optional#empty()}
     */
    Optional<XDefNodeMap> findByNamespaceUri(String namespaceUri);

    // ====================
    // Basic Map interface
    // ====================

    XDefNodeMap computeIfAbsent(String key, Function<? super String, ? extends XDefNodeMap> mappingFunction);

    boolean isEmpty();

    /**
     * Xdefinition node map
     *
     * Key:     node name
     * Value:   XDefinition node
     */
    interface XDefNodeMap {

        /**
         * Returns true if contains given node name
         * @param nodeName
         * @return
         */
        boolean containsNodeName(String nodeName);

        /**
         * Put XDefinition node into the map if XDefinition node with that name has not yet been inserted
         * @param xNode
         */
        void addNode(XNode xNode);

        /**
         * Returns all XDefinition nodes
         * @return
         */
        Collection<XNode> values();

    }

}
