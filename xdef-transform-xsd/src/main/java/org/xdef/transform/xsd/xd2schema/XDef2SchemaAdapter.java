package org.xdef.transform.xsd.xd2schema;

import org.xdef.XDPool;
import org.xdef.model.XMDefinition;

/**
 * Transforms single x-definition to XSD schema
 * @param <T> type of returned schema
 */
public interface XDef2SchemaAdapter<T> {

    /**
     * Transforms given x-definition pool to XSD schema
     * @param xdPool    source x-definition pool
     * @return  XSD schema
     */
    T createSchema(final XDPool xdPool);

    /**
     * Transforms given x-definition to XSD schema
     * @param xDef  source x-definition
     * @return  XSD schema
     */
    T createSchema(final XMDefinition xDef);

}
