package org.xdef.transform.xsd.schema2xd.model;

import org.xdef.transform.xsd.model.Namespace;

import java.util.List;
import java.util.Optional;

/**
 * Namespace map.
 *
 * Key:     namespace prefix
 * Value:   namespace URI
 *
 * @author smid
 * @since 2021-05-21
 */
public interface NamespaceMap {

    /**
     * Stores namespace if given prefix does not already exist
     * @param nsPrefix
     * @param nsUri
     * @param xDefName
     * @return  true if namespace has been added
     *          false otherwise
     */
    boolean add(String nsPrefix, String nsUri, String xDefName);

    /**
     * Finds namespace by given URI
     * @param nsUri
     * @return
     */
    Optional<String> findByUri(String nsUri);

    /**
     * Returns list of namespaces
     * @return
     */
    List<Namespace> getNamespaces();
}
