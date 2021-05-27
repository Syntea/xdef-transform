package org.xdef.transform.xsd.xd2schema.model.xsd;

import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;

import java.util.List;

/**
 * XML Schema group particle wrapper
 *
 * @param <T>
 */
public interface XmlSchemaGroupParticleWrapper<T extends XmlSchemaObjectBase> {

    /**
     * @return particle XML Schema items
     */
    List<T> getItems();

    /**
     * Add XML Schema item into particle
     * @param item      XML Schema item
     */
    void addItem(final XmlSchemaObjectBase item);

    /**
     * Add XML Schema items into particle
     * @param items     list of XML Schema items
     */
    void addItems(final List<XmlSchemaObjectBase> items);

}
