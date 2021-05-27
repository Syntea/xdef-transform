package org.xdef.transform.xsd.xd2schema;

import org.xdef.XDPool;
import org.xdef.model.XMDefinition;

/**
 * Transforms single X-Definition to XSD schema
 * @param <T> type of returned schema
 */
public interface XDef2SchemaAdapter<T> {

    /**
     * Transforms given X-Definition pool to XSD schema
     * @param xdPool    source X-Definition pool
     * @return  XSD schema
     */
    T createSchema(final XDPool xdPool);

    /**
     * Transforms given X-Definition to XSD schema
     * @param xDef  source X-Definition
     * @return  XSD schema
     */
    T createSchema(final XMDefinition xDef);

}
