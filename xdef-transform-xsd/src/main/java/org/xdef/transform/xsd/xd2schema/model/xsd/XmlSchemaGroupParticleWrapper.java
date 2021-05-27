package org.xdef.transform.xsd.xd2schema.model.xsd;

import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;

import java.util.List;

/**
 * XML schema group particle wrapper
 *
 * @param <T>
 */
public interface XmlSchemaGroupParticleWrapper<T extends XmlSchemaObjectBase> {

    /**
     * @return particle XML schema items
     */
    List<T> getItems();

    /**
     * Add XML schema item into particle
     * @param item      XML schema item
     */
    void addItem(final XmlSchemaObjectBase item);

    /**
     * Add XML schema items into particle
     * @param items     list of XML schema items
     */
    void addItems(final List<XmlSchemaObjectBase> items);

}
