package org.xdef.transform.xsd.xd2schema;

import org.xdef.XDPool;

/**
 * Transforms X-Definition pool to XML Schema
 * @param <T> type of returned schema
 */
public interface XdPool2SchemaAdapter<T> {

    /**
     * Transforms given X-Definition pool to XML Schema
     * @param xdPool    source X-Definition pool
     * @return  XML Schema
     */
    T createSchemas(final XDPool xdPool);

}
