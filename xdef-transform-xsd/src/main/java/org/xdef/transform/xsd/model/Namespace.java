package org.xdef.transform.xsd.model;

/**
 * Basic namespace description model
 *
 * @author smid
 * @since 2021-05-20
 */
public interface Namespace {

    /**
     * @return  namespace prefix
     */
    String getPrefix();

    /**
     * @return  namespace uri
     */
    String getUri();

    /**
     * @return  true if prefix is empty
     */
    boolean isEmptyPrefix();

    /**
     * @return  true if uri is empty
     */
    boolean isEmptyUri();

}
