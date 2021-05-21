package org.xdef.transform.xsd.xd2schema;

import org.xdef.XDPool;

/**
 * Transforms x-definition pool to XSD schema
 * @param <T> type of returned schema
 */
public interface XdPool2SchemaAdapter<T> {

    /**
     * Transforms given x-definition pool to XSD schema
     * @param xdPool    source x-definition pool
     * @return  XSD schema
     */
    T createSchemas(final XDPool xdPool);

}
