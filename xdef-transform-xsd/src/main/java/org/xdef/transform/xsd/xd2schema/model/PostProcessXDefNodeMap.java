package org.xdef.transform.xsd.xd2schema.model;

import org.xdef.impl.XNode;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

/**
 * Namespace node map
 *
 * Key:     namespace URI
 * Value:   X-Definition node map
 *
 * @author smid
 * @since 2021-05-27
 */
public interface PostProcessXDefNodeMap {

    /**
     * Finds X-Definition node map by given namespace URI
     * @param namespaceUri      X-Definition namespace
     * @return  X-Definition node map if found
     *          otherwise {@link Optional#empty()}
     */
    Optional<XDefNodeMap> findByNamespaceUri(String namespaceUri);

    // ====================
    // Basic Map interface
    // ====================

    XDefNodeMap computeIfAbsent(String key, Function<? super String, ? extends XDefNodeMap> mappingFunction);

    boolean isEmpty();

    /**
     * X-Definition node map
     *
     * Key:     node name
     * Value:   X-Definition node
     */
    interface XDefNodeMap {

        /**
         * @param nodeName      X-Definition node name
         * @return  true if contains given node name
         */
        boolean containsNodeName(String nodeName);

        /**
         * Put XDefinition node into the map if X-Definition node with that name has not yet been inserted
         * @param xNode         X-Definition node
         */
        void addNode(XNode xNode);

        /**
         * @return  all X-Definition nodes
         */
        Collection<XNode> values();

    }

}
