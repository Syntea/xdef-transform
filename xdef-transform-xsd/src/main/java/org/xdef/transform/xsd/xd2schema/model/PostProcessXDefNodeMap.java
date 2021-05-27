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
     * @param namespaceUri      XDefinition namespace
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
     * XDefinition node map
     *
     * Key:     node name
     * Value:   XDefinition node
     */
    interface XDefNodeMap {

        /**
         * @param nodeName      XDefinition node name
         * @return  true if contains given node name
         */
        boolean containsNodeName(String nodeName);

        /**
         * Put XDefinition node into the map if XDefinition node with that name has not yet been inserted
         * @param xNode         XDefinition node
         */
        void addNode(XNode xNode);

        /**
         * @return  all XDefinition nodes
         */
        Collection<XNode> values();

    }

}
