package org.xdef.transform.xsd.xd2schema;

import org.xdef.XDPool;
import org.xdef.model.XMDefinition;

/**
 * Transforms single X-Definition to XML Schema
 * @param <T> type of returned schema
 */
public interface XDef2SchemaAdapter<T> {

    /**
     * Transforms given X-Definition pool to XML Schema
     * @param xdPool    source X-Definition pool
     * @return  XML Schema
     */
    T createSchema(final XDPool xdPool);

    /**
     * Transforms given X-Definition to XML Schema
     * @param xDef  source X-Definition
     * @return  XML Schema
     */
    T createSchema(final XMDefinition xDef);

}
