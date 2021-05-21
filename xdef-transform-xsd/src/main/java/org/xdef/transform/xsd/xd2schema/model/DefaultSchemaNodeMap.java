package org.xdef.transform.xsd.xd2schema.model;

import java.util.HashMap;
import java.util.Optional;

/**
 * @author smid
 * @since 2021-05-21
 */
public class DefaultSchemaNodeMap extends HashMap<String, SchemaNode> implements SchemaNodeMap {

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
