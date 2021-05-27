package org.xdef.transform.xsd.model.impl;

import org.apache.commons.lang3.tuple.MutablePair;
import org.xdef.transform.xsd.model.Namespace;

import static org.xdef.transform.xsd.def.NamespaceConst.NAMESPACE_PREFIX_EMPTY;
import static org.xdef.transform.xsd.def.NamespaceConst.NAMESPACE_URI_EMPTY;

/**
 * @author smid
 * @since 2021-05-20
 */
public class DefaultNamespace extends MutablePair<String, String> implements Namespace {

    public DefaultNamespace() {
    }

    public DefaultNamespace(String prefix, String uri) {
        super(prefix, uri);
    }

    @Override
    public String getPrefix() {
        return super.getLeft();
    }

    @Override
    public String getUri() {
        return super.getRight();
    }

    @Override
    public boolean isEmptyPrefix() {
        return NAMESPACE_PREFIX_EMPTY.equals(getPrefix());
    }

    @Override
    public boolean isEmptyUri() {
        return NAMESPACE_URI_EMPTY.equals(getUri());
    }

    public static DefaultNamespace createEmptyNamespace() {
        return new DefaultNamespace(NAMESPACE_PREFIX_EMPTY, NAMESPACE_URI_EMPTY);
    }

    @Override
    public String toString() {
        return "DefaultNamespace{" +
                "prefix=" + left +
                ", uri=" + right +
                '}';
    }
}
