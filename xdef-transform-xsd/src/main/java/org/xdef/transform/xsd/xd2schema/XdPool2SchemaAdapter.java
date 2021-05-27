package org.xdef.transform.xsd.xd2schema;

import org.xdef.XDPool;

/**
 * Transforms X-Definition pool to XSD schema
 * @param <T> type of returned schema
 */
public interface XdPool2SchemaAdapter<T> {

    /**
     * Transforms given X-Definition pool to XSD schema
     * @param xdPool    source X-Definition pool
     * @return  XSD schema
     */
    T createSchemas(final XDPool xdPool);

}
