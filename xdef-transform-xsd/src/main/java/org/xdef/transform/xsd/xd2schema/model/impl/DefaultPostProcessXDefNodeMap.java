package org.xdef.transform.xsd.xd2schema.model.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdef.impl.XNode;
import org.xdef.transform.xsd.xd2schema.model.PostProcessXDefNodeMap;

import java.util.HashMap;
import java.util.Optional;

import static org.xdef.transform.xsd.util.LoggingUtil.logHeader;
import static org.xdef.transform.xsd.xd2schema.def.AlgPhase.TRANSFORMATION;

/**
 * @author smid
 * @since 2021-05-27
 */
public class DefaultPostProcessXDefNodeMap extends HashMap<String, PostProcessXDefNodeMap.XDefNodeMap> implements PostProcessXDefNodeMap {

    @Override
    public Optional<XDefNodeMap> findByNamespaceUri(String namespaceUri) {
        return Optional.ofNullable(super.get(namespaceUri));
    }

    public static class DefaultXDefNodeMap extends HashMap<String, XNode> implements XDefNodeMap {

        private static final Logger LOG = LoggerFactory.getLogger(DefaultXDefNodeMap.class);

        @Override
        public boolean containsNodeName(String nodeName) {
            return super.containsKey(nodeName);
        }

        @Override
        public void addNode(XNode xNode) {
            final String nodeName = xNode.getName();
            if (containsNodeName(nodeName)) {
                LOG.info("{}Node is already marked for post-processing.", logHeader(TRANSFORMATION, xNode));
                return;
            }

            super.put(nodeName, xNode);
        }

    }
}
