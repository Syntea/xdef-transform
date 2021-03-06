package org.xdef.transform.xsd.schema2xd.util;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaGroup;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.xdef.transform.xsd.schema2xd.def.Xsd2XdFeature;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.xdef.transform.xsd.schema2xd.def.Xsd2XdFeature.XD_TEXT_REQUIRED;


/**
 * Basic utils used in transformation XML Schema -> X-Definition
 */
public class Xsd2XdUtils {

    /**
     * Finds XML Schema complex/simple schema type node in given schema by qualified name
     * @param schema    XML Schema document
     * @param qName     XML Schema qualified name to be searched
     * @return  XML Schema complex/simple schema type node if exists in given schema
     *          otherwise {@link Optional#empty()}
     */
    public static Optional<XmlSchemaType> findSchemaTypeByQName(final XmlSchema schema, final QName qName) {
        return Optional.ofNullable(schema.getSchemaTypes()).map(schemaTypeMap -> schemaTypeMap.get(qName));
    }

    /**
     * Finds XML Schema group node in given schema by qualified name
     * @param schema    XML Schema document
     * @param qName     XML Schema qualified name to be searched
     * @return  XML Schema group node if exists in given schema
     *          otherwise {@link Optional#empty()}
     */
    public static Optional<XmlSchemaGroup> findGroupByQName(final XmlSchema schema, final QName qName) {
        return Optional.ofNullable(schema.getGroups()).map(groups -> groups.get(qName));
    }

    /**
     * Features which should be enabled by default for transformation algorithm
     * @return default algorithm features
     */
    public static Set<Xsd2XdFeature> defaultFeatures() {
        final Set<Xsd2XdFeature> features = new HashSet<>();
        features.add(XD_TEXT_REQUIRED);
        return features;
    }

}
