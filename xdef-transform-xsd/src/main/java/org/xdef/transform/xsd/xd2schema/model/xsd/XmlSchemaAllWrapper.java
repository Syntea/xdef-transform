package org.xdef.transform.xsd.xd2schema.model.xsd;

import org.apache.ws.commons.schema.XmlSchemaAll;
import org.apache.ws.commons.schema.XmlSchemaAllMember;
import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;

import java.util.List;

public class XmlSchemaAllWrapper extends AbstractXmlSchemaGroupParticleWrapper<XmlSchemaAll, XmlSchemaAllMember> {

    public XmlSchemaAllWrapper(XmlSchemaAll xsdGroupElem) {
        super(xsdGroupElem);
    }

    @Override
    public List<XmlSchemaAllMember> getItems() {
        return xsdNode.getItems();
    }

    @Override
    public void addItem(XmlSchemaObjectBase item) {
        xsdNode.getItems().add((XmlSchemaAllMember) item);
    }

    @Override
    public void addItems(final List<XmlSchemaObjectBase> items) {
        for (XmlSchemaObjectBase item : items) {
            xsdNode.getItems().add((XmlSchemaAllMember) item);
        }
    }
}
