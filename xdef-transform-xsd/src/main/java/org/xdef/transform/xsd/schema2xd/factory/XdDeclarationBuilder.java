package org.xdef.transform.xsd.schema2xd.factory;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeContent;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeList;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeUnion;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.xdef.sys.ReportWriter;
import org.xdef.transform.xsd.msg.XSD;
import org.xdef.transform.xsd.schema2xd.factory.declaration.impl.DefaultTypeFactory;
import org.xdef.transform.xsd.schema2xd.factory.declaration.IDeclarationTypeFactory;
import org.xdef.transform.xsd.schema2xd.factory.declaration.impl.ListTypeFactory;
import org.xdef.transform.xsd.schema2xd.factory.declaration.impl.UnionTypeFactory;
import org.xdef.transform.xsd.schema2xd.util.Xsd2XdTypeMapping;
import org.xdef.transform.xsd.schema2xd.util.Xsd2XdUtils;

import javax.xml.namespace.QName;
import java.util.LinkedList;
import java.util.List;

import static org.xdef.transform.xsd.util.LoggingUtil.logHeader;
import static org.xdef.transform.xsd.xd2schema.def.AlgPhase.TRANSFORMATION;

/**
 * Creates X-Definition declaration and declaration content for X-Definition declarations
 */
public class XdDeclarationBuilder implements Cloneable {

    private static final Logger LOG = LoggerFactory.getLogger(XdDeclarationBuilder.class);

    /**
     * Input schema used for transformation
     */
    private XmlSchema schema;

    /**
     * X-definition declarations factory
     */
    private XdDeclarationFactory xdDeclarationFactory;

    /**
     * XML Schema type node to be transformed
     */
    XmlSchemaSimpleType simpleType;

    /**
     * Parent X-Definition node where should be declaration
     */
    Element parentNode;

    /**
     * Type of X-Definition declaration
     */
    private IDeclarationTypeFactory.Type type;

    /**
     * Declaration variable name
     */
    private String name;

    /**
     * Declaration qualified name
     */
    private QName baseType;

    /**
     * Output report writer
     */
    private ReportWriter reportWriter;

    XdDeclarationBuilder() { }

    /**
     * Initialize X-Definition declaration builder with default values
     * @param schema                    input XML Schema
     * @param xdDeclarationFactory      X-Definition declaration factory
     * @return X-Definition declaration builder for give XML Schema
     */
    XdDeclarationBuilder init(XmlSchema schema, XdDeclarationFactory xdDeclarationFactory, ReportWriter reportWriter) {
        this.schema = schema;
        this.xdDeclarationFactory = xdDeclarationFactory;
        this.reportWriter = reportWriter;
        return this;
    }

    /**
     * Sets XML Schema type
     * @param simpleType XML Schema type
     * @return current instance
     */
    public XdDeclarationBuilder setSimpleType(XmlSchemaSimpleType simpleType) {
        this.simpleType = simpleType;
        return this;
    }

    /**
     * Sets Parent X-Definition node
     * @param parentNode Parent X-Definition node
     * @return current instance
     */
    public XdDeclarationBuilder setParentNode(Element parentNode) {
        this.parentNode = parentNode;
        return this;
    }

    /**
     * Sets X-Definition declaration
     * @param type X-Definition declaration
     * @return current instance
     */
    public XdDeclarationBuilder setType(IDeclarationTypeFactory.Type type) {
        this.type = type;
        return this;
    }

    /**
     * Sets variable name
     * @param name variable name
     * @return current instance
     */
    public XdDeclarationBuilder setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets declaration qualified name
     * @param baseType Declaration qualified name
     * @return current instance
     */
    public XdDeclarationBuilder setBaseType(QName baseType) {
        this.baseType = baseType;
        return this;
    }

    @Override
    protected XdDeclarationBuilder clone() throws CloneNotSupportedException {
        super.clone();

        final XdDeclarationBuilder o = xdDeclarationFactory.createBuilder();
        o.simpleType = this.simpleType;
        o.parentNode = this.parentNode;
        o.type = this.type;
        o.name = this.name;
        o.baseType = this.baseType;
        return o;
    }

    /**
     * Creates X-Definition declaration/declaration content based on internal state
     * @return X-Definition declaration/declaration content
     */
    String build() {
        if (type == null) {
            reportWriter.error(XSD.XSD204);
            LOG.error("{}Declaration type is not set!", logHeader(TRANSFORMATION, simpleType));
        }

        LOG.info("{}Building declaration. type='{}'", logHeader(TRANSFORMATION, simpleType), type);

        if (IDeclarationTypeFactory.Type.TOP_DECL.equals(type)) {
            return createTopDeclaration();
        }

        return createDeclaration(simpleType.getContent());
    }

    /**
     * Creates X-Definition root declaration based on internal state
     * @return X-Definition declaration
     */
    private String createTopDeclaration() {
        LOG.info("{}Building top declaration content ...", logHeader(TRANSFORMATION, simpleType));
        name = simpleType.getName();
        if (simpleType.getContent() instanceof XmlSchemaSimpleTypeRestriction) {
            return createTop((XmlSchemaSimpleTypeRestriction) simpleType.getContent());
        } else if (simpleType.getContent() instanceof XmlSchemaSimpleTypeList) {
            return create((XmlSchemaSimpleTypeList) simpleType.getContent(), null);
        }

        // TODO: union?
        reportWriter.warning(XSD.XSD205);
        LOG.warn("{}Empty top declaration has been created!", logHeader(TRANSFORMATION, simpleType));
        return "";
    }

    /**
     * Creates X-Definition root declaration based on internal state and input XML Schema restriction node
     * @param simpleTypeRestriction     XML Schema restriction node
     * @return X-Definition declaration
     */
    private String createTop(final XmlSchemaSimpleTypeRestriction simpleTypeRestriction) {
        LOG.info("{}Building declaration content. name='{}', type='{}'",
                logHeader(TRANSFORMATION, simpleTypeRestriction), name, type);

        final QName baseType = simpleTypeRestriction.getBaseTypeName();
        IDeclarationTypeFactory xdDeclarationTypeFactory = Xsd2XdTypeMapping.findDefaultDataTypeFactory(baseType)
                .orElse(null);

        if (xdDeclarationTypeFactory == null) {
            final XmlSchemaType itemSchemaType = Xsd2XdUtils.findSchemaTypeByQName(schema, baseType).orElse(null);
            if (itemSchemaType instanceof XmlSchemaSimpleType) {
                final XmlSchemaSimpleType schemaSimpleType = (XmlSchemaSimpleType)itemSchemaType;
                if (IDeclarationTypeFactory.Type.TOP_DECL.equals(type)) {
                    try {
                        xdDeclarationFactory.createDeclaration(clone().setSimpleType(schemaSimpleType));
                    } catch (CloneNotSupportedException ex) {
                        throw new RuntimeException(ex);
                    }
                } else if (IDeclarationTypeFactory.Type.TEXT_DECL.equals(type)) {
                    return createDeclaration(schemaSimpleType.getContent());
                }
            }

            xdDeclarationTypeFactory = new DefaultTypeFactory(baseType.getLocalPart());
            xdDeclarationTypeFactory.setName(name);
            xdDeclarationTypeFactory.setType(IDeclarationTypeFactory.Type.TOP_DECL);
            return xdDeclarationTypeFactory.build("", reportWriter);
        }

        if (IDeclarationTypeFactory.Type.TOP_DECL.equals(type) && !xdDeclarationFactory.canBeProcessed(name)) {
            LOG.debug("{}Declaration has been already created. name='{}'",
                    logHeader(TRANSFORMATION, simpleTypeRestriction), name);
            return null;
        }

        xdDeclarationTypeFactory.setType(type);
        xdDeclarationTypeFactory.setName(name);
        return xdDeclarationTypeFactory.build(simpleTypeRestriction.getFacets(), reportWriter);
    }

    /**
     * Creates X-Definition declaration content based on internal state and input XML Schema simple content node
     * @param simpleTypeContent     XML Schema simple content node
     * @return X-Definition declaration content
     */
    private String createDeclaration(final XmlSchemaSimpleTypeContent simpleTypeContent) {
        if (simpleTypeContent instanceof XmlSchemaSimpleTypeRestriction) {
            return createDeclaration((XmlSchemaSimpleTypeRestriction)simpleTypeContent);
        }

        return createSetDeclaration(simpleTypeContent, null);
    }

    /**
     * Creates X-Definition declaration of X-Definition list/union based on internal state and input XML Schema union/list node
     * @param simpleTypeContent     XML Schema union/list node
     * @param extraFacets           additional list of XML Schema facet nodes, which should be applied
     * @return X-Definition declaration
     */
    private String createSetDeclaration(final XmlSchemaSimpleTypeContent simpleTypeContent, final List<XmlSchemaFacet> extraFacets) {
        if (simpleTypeContent instanceof XmlSchemaSimpleTypeList) {
            return create((XmlSchemaSimpleTypeList)simpleTypeContent, extraFacets);
        } else if (simpleTypeContent instanceof XmlSchemaSimpleTypeUnion) {
            return create((XmlSchemaSimpleTypeUnion)simpleTypeContent, extraFacets);
        }

        reportWriter.warning(XSD.XSD206);
        LOG.warn("{}Empty set text declaration has been created!", logHeader(TRANSFORMATION, simpleType));
        return "";
    }

    /**
     * Creates X-Definition declaration/declaration content based on internal state and input XML Schema restriction node
     * @param simpleTypeRestriction     XML Schema restriction node
     * @return X-Definition declaration/declaration content
     */
    private String createDeclaration(final XmlSchemaSimpleTypeRestriction simpleTypeRestriction) {
        if (baseType == null) {
            baseType = simpleTypeRestriction.getBaseTypeName();
        }

        if (baseType != null) {
            IDeclarationTypeFactory xdDeclarationFactory = Xsd2XdTypeMapping.findDefaultDataTypeFactory(baseType)
                    .orElse(null);
            if (xdDeclarationFactory != null) {
                xdDeclarationFactory.setType(type);
                return xdDeclarationFactory.build(simpleTypeRestriction.getFacets(), reportWriter);
            }

            xdDeclarationFactory = new DefaultTypeFactory(baseType.getLocalPart());
            xdDeclarationFactory.setType(type);
            return xdDeclarationFactory.build("", reportWriter);
        } else if (simpleTypeRestriction.getBaseType() != null) {
            return createSetDeclaration(simpleTypeRestriction.getBaseType().getContent(), simpleTypeRestriction.getFacets());
        }

        reportWriter.warning(XSD.XSD207);
        LOG.warn("{}Empty restriction declaration has been created!", logHeader(TRANSFORMATION, simpleTypeRestriction));
        return "";
    }

    /**
     * Creates X-Definition declaration/declaration content based on internal state and input XML Schema union node
     * @param simpleTypeUnion       XML Schema union node
     * @param extraFacets           additional list of XML Schema facet nodes, which should be applied
     * @return X-Definition declaration/declaration content
     */
    private String create(final XmlSchemaSimpleTypeUnion simpleTypeUnion, final List<XmlSchemaFacet> extraFacets) {
        final QName[] qNames = simpleTypeUnion.getMemberTypesQNames();
        if (qNames != null && qNames.length > 0) {
            if (qNames.length == 1) {
                final IDeclarationTypeFactory xdDeclarationFactory = Xsd2XdTypeMapping.findDefaultDataTypeFactory(qNames[0])
                        .orElse(null);
                if (xdDeclarationFactory != null) {
                    final XmlSchemaSimpleTypeContent unionSimpleContent = simpleTypeUnion.getBaseTypes().get(0).getContent();
                    if (unionSimpleContent instanceof XmlSchemaSimpleTypeRestriction) {
                        xdDeclarationFactory.setType(type);
                        return xdDeclarationFactory.build(((XmlSchemaSimpleTypeRestriction)unionSimpleContent).getFacets(), reportWriter);
                    }
                }
            }

            final StringBuilder facetStringBuilder = new StringBuilder();
            for (QName qName : qNames) {
                final XmlSchemaType itemSchemaType = Xsd2XdUtils.findSchemaTypeByQName(schema, qName).orElse(null);
                if (itemSchemaType instanceof XmlSchemaSimpleType) {
                    final XmlSchemaSimpleType itemSimpleSchemaType = (XmlSchemaSimpleType) itemSchemaType;
                    if (itemSimpleSchemaType.getContent() instanceof XmlSchemaSimpleTypeRestriction) {
                        if (facetStringBuilder.length() > 0) {
                            facetStringBuilder.append(", ");
                        }

                        facetStringBuilder.append(qName.getLocalPart());
                    }
                }
            }

            final UnionTypeFactory unionTypeFactory = new UnionTypeFactory();
            unionTypeFactory.setType(type);
            return unionTypeFactory.build(facetStringBuilder.toString(), reportWriter);
        } else {
            final List<XmlSchemaSimpleType> baseTypes = simpleTypeUnion.getBaseTypes();
            if (baseTypes != null && !baseTypes.isEmpty()) {
                IDeclarationTypeFactory xdDeclarationFactory = null;
                final List<XmlSchemaFacet> facets = new LinkedList<>();

                for (XmlSchemaSimpleType baseType : baseTypes) {
                    if (baseType.getContent() instanceof XmlSchemaSimpleTypeRestriction) {
                        facets.addAll(((XmlSchemaSimpleTypeRestriction)baseType.getContent()).getFacets());
                        if (xdDeclarationFactory == null) {
                            xdDeclarationFactory = Xsd2XdTypeMapping.findDefaultDataTypeFactory(
                                    ((XmlSchemaSimpleTypeRestriction) baseType.getContent()).getBaseTypeName()
                            ).orElse(null);
                        }
                    }
                }

                if (xdDeclarationFactory == null) {
                    reportWriter.warning(XSD.XSD208);
                    LOG.warn("{}Unknown XML Schema union base type!", logHeader(TRANSFORMATION, simpleTypeUnion));
                    return null;
                }

                xdDeclarationFactory.setType(type);
                return xdDeclarationFactory.build(facets, reportWriter);
            }
        }

        reportWriter.warning(XSD.XSD209);
        LOG.warn("{}Empty union declaration has been created!", logHeader(TRANSFORMATION, simpleTypeUnion));
        return "";
    }

    /**
     * Creates X-Definition declaration/declaration content based on internal state and input XML Schema list node
     * @param simpleTypeList        XML Schema list node
     * @param extraFacets           additional list of XML Schema facet nodes, which should be applied
     * @return X-Definition declaration/declaration content
     */
    private String create(final XmlSchemaSimpleTypeList simpleTypeList, final List<XmlSchemaFacet> extraFacets) {
        LOG.info("{}Creating list declaration content. name='{}', type='{}'",
                logHeader(TRANSFORMATION, simpleTypeList), name, type);

        String facetString = "";
        final QName baseType = simpleTypeList.getItemTypeName();
        if (baseType != null) {
            final XmlSchemaType itemSchemaType = Xsd2XdUtils.findSchemaTypeByQName(schema, baseType).orElse(null);
            if (itemSchemaType instanceof XmlSchemaSimpleType) {
                final XmlSchemaSimpleType itemSimpleSchemaType = (XmlSchemaSimpleType) itemSchemaType;
                if (itemSimpleSchemaType.getContent() instanceof XmlSchemaSimpleTypeRestriction) {
                    facetString = baseType.getLocalPart();
                }
            }
        } else if (simpleTypeList.getItemType() != null) {
            if (simpleTypeList.getItemType().getContent() instanceof XmlSchemaSimpleTypeRestriction) {
                try {
                    final XdDeclarationBuilder b = clone()
                            .setSimpleType(simpleTypeList.getItemType())
                            .setType(IDeclarationTypeFactory.Type.DATATYPE_DECL);
                    facetString = b.build();
                } catch (CloneNotSupportedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        if (facetString.isEmpty()) {
            LOG.debug("{}List declaration content empty.", logHeader(TRANSFORMATION, simpleTypeList));
        }

        if (extraFacets != null && !extraFacets.isEmpty()) {
            final DefaultTypeFactory defaultTypeFactory = new DefaultTypeFactory("");
            defaultTypeFactory.setType(IDeclarationTypeFactory.Type.DATATYPE_DECL);
            String extraFacetsString = defaultTypeFactory.build(extraFacets, reportWriter);
            if (!extraFacetsString.isEmpty()) {
                extraFacetsString = extraFacetsString.substring(1).substring(0, extraFacetsString.length() - 2);
                facetString += ", " + extraFacetsString;
            }
        }

        if (IDeclarationTypeFactory.Type.TOP_DECL.equals(type) && !xdDeclarationFactory.canBeProcessed(name)) {
            LOG.debug("{}Declaration has been already created. name='{}'",
                    logHeader(TRANSFORMATION, simpleTypeList), name);
            return null;
        }

        final ListTypeFactory listTypeFactory = new ListTypeFactory();
        listTypeFactory.setType(type);
        listTypeFactory.setName(name);
        return listTypeFactory.build(facetString, reportWriter);
    }

}
