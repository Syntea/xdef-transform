package org.xdef.transform.xsd.schema2xd;

/**
 * Transforms XML Schema to X-Definition or X-Definition pool
 * @param <T> type of input schema
 */
public interface Xsd2XDefAdapter<T> {

    /**
     * Transforms given XML Schema to X-Definition or x-definiton pool XML
     * @param schema        input schema to be transformed
     * @param xDefName      output X-Definition name
     * @return X-Definition or x-definiton pool
     */
    String createXDefinition(final T schema, final String xDefName);

}
