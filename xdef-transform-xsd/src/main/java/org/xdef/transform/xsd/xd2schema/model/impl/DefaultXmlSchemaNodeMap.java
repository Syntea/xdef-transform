package org.xdef.transform.xsd.xd2schema.model.impl;

import org.xdef.transform.xsd.xd2schema.model.XmlSchemaNodeMap;

import java.util.HashMap;
import java.util.Optional;

/**
 * @author smid
 * @since 2021-05-27
 */
public class DefaultXmlSchemaNodeMap extends HashMap<String, XmlSchemaNodeMap.SchemaNodeMap> implements XmlSchemaNodeMap {

    @Override
    public Optional<SchemaNodeMap> findByXmlSchema(String schemaName) {
        return Optional.ofNullable(super.get(schemaName));
    }

    public static class DefaultSchemaNodeMap extends HashMap<String, SchemaNode> implements SchemaNodeMap {

        @Override
        public Optional<SchemaNode> findSchemaNode(String nodePath) {
            return Optional.ofNullable(get(nodePath));
        }

        @Override
        public void addNode(String nodePath, SchemaNode schemaNode) {
            put(nodePath, schemaNode);
        }

        @Override
        public Optional<SchemaNode> removeNode(String nodePath) {
            return Optional.ofNullable(remove(nodePath));
        }

    }
}
