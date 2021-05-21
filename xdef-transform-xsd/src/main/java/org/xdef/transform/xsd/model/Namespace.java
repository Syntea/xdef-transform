package org.xdef.transform.xsd.model;

/**
 * Basic namespace description model
 *
 * @author smid
 * @since 2021-05-20
 */
public interface Namespace {

    /**
     * Returns namespace prefix
     * @return
     */
    String getPrefix();

    /**
     * Return namespace uri
     * @return
     */
    String getUri();

    /**
     * Returns true if prefix is empty
     * @return
     */
    boolean isEmptyPrefix();

    /**
     * Return true if uri is empty
     * @return
     */
    boolean isEmptyUri();

}
