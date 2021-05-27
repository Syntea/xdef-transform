package org.xdef.transform.xsd.schema2xd.util;

import org.apache.ws.commons.schema.constants.Constants;
import org.xdef.transform.xsd.schema2xd.factory.declaration.DecimalTypeFactory;
import org.xdef.transform.xsd.schema2xd.factory.declaration.DefaultTypeFactory;
import org.xdef.transform.xsd.schema2xd.factory.declaration.IDeclarationTypeFactory;
import org.xdef.transform.xsd.schema2xd.factory.declaration.IntegerTypeFactory;
import org.xdef.transform.xsd.schema2xd.factory.declaration.TextTypeFactory;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.xdef.transform.xsd.schema2xd.factory.declaration.IDeclarationTypeFactory.FACET_PATTERN;

/**
 * Definition of transformation XML Schema data types to X-Definition data types
 */
public class Xsd2XdTypeMapping {

    /**
     * Transformation map of XML Schema data types to X-Definition data type
     */
    private static final Map<QName, IDeclarationTypeFactory> defaultQNameMap = new HashMap<>();

    static {
        defaultQNameMap.put(Constants.XSD_ANYURI, new TextTypeFactory("anyURI"));
        defaultQNameMap.put(Constants.XSD_BASE64, new TextTypeFactory("base64Binary"));
        defaultQNameMap.put(Constants.XSD_BOOLEAN, new DefaultTypeFactory("boolean"));
        defaultQNameMap.put(Constants.XSD_ENTITIES, new DefaultTypeFactory("ENTITIES"));
        defaultQNameMap.put(Constants.XSD_ENTITY, new DefaultTypeFactory("ENTITY"));
        defaultQNameMap.put(Constants.XSD_HEXBIN, new TextTypeFactory("hexBinary"));
        defaultQNameMap.put(Constants.XSD_ID, new DefaultTypeFactory("ID"));
        defaultQNameMap.put(Constants.XSD_IDREF, new DefaultTypeFactory("IDREF"));
        defaultQNameMap.put(Constants.XSD_DAY, new DefaultTypeFactory("gDay"));
        defaultQNameMap.put(Constants.XSD_MONTH, new DefaultTypeFactory("gMonth"));
        defaultQNameMap.put(Constants.XSD_MONTHDAY, new DefaultTypeFactory("gMonthDay"));
        defaultQNameMap.put(Constants.XSD_NCNAME, new DefaultTypeFactory("NCName").removeFacet(FACET_PATTERN));
        defaultQNameMap.put(Constants.XSD_NMTOKEN, new DefaultTypeFactory("NMTOKEN").removeFacet(FACET_PATTERN));
        defaultQNameMap.put(Constants.XSD_YEAR, new DefaultTypeFactory("gYear"));
        defaultQNameMap.put(Constants.XSD_YEARMONTH, new DefaultTypeFactory("gYearMonth"));
        defaultQNameMap.put(Constants.XSD_NORMALIZEDSTRING, new DefaultTypeFactory("normalizedString"));
        defaultQNameMap.put(Constants.XSD_QNAME, new DefaultTypeFactory("QName"));

        defaultQNameMap.put(Constants.XSD_DATE, new DefaultTypeFactory("ISOdate"));
        defaultQNameMap.put(Constants.XSD_DATETIME, new DefaultTypeFactory("ISOdateTime"));
        defaultQNameMap.put(Constants.XSD_TIME, new DefaultTypeFactory("time"));
        defaultQNameMap.put(Constants.XSD_LANGUAGE, new DefaultTypeFactory("language").removeFacet(FACET_PATTERN));
        defaultQNameMap.put(Constants.XSD_DURATION, new DefaultTypeFactory("duration"));

        defaultQNameMap.put(Constants.XSD_STRING, new TextTypeFactory("string"));
        defaultQNameMap.put(Constants.XSD_FLOAT, new DecimalTypeFactory("float"));
        defaultQNameMap.put(Constants.XSD_DOUBLE, new DecimalTypeFactory("double"));
        defaultQNameMap.put(Constants.XSD_DECIMAL, new DecimalTypeFactory("dec"));
        defaultQNameMap.put(Constants.XSD_SHORT, new IntegerTypeFactory("short"));
        defaultQNameMap.put(Constants.XSD_INT, new IntegerTypeFactory("int"));
        defaultQNameMap.put(Constants.XSD_INTEGER, new IntegerTypeFactory("int"));
        defaultQNameMap.put(Constants.XSD_LONG, new IntegerTypeFactory("long"));
    }

    /**
     * Converts given XML Schema qualified name to X-Definition data type
     * @param xsdType   XML Schema qualified name
     * @return  X-Definition data type if exists,
     *          otherwise {@link Optional#empty()}
     */
    public static Optional<IDeclarationTypeFactory> findDefaultDataTypeFactory(final QName xsdType) {
        return Optional.ofNullable(defaultQNameMap.get(xsdType));
    }
}
