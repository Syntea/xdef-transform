package org.xdef.transform.xsd.schema2xd.factory.declaration;

import org.apache.ws.commons.schema.XmlSchemaEnumerationFacet;
import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.apache.ws.commons.schema.XmlSchemaFractionDigitsFacet;
import org.apache.ws.commons.schema.XmlSchemaLengthFacet;
import org.apache.ws.commons.schema.XmlSchemaMaxExclusiveFacet;
import org.apache.ws.commons.schema.XmlSchemaMaxInclusiveFacet;
import org.apache.ws.commons.schema.XmlSchemaMaxLengthFacet;
import org.apache.ws.commons.schema.XmlSchemaMinExclusiveFacet;
import org.apache.ws.commons.schema.XmlSchemaMinInclusiveFacet;
import org.apache.ws.commons.schema.XmlSchemaMinLengthFacet;
import org.apache.ws.commons.schema.XmlSchemaPatternFacet;
import org.apache.ws.commons.schema.XmlSchemaTotalDigitsFacet;
import org.apache.ws.commons.schema.XmlSchemaWhiteSpaceFacet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdef.sys.ReportWriter;
import org.xdef.transform.xsd.msg.XSD;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.xdef.transform.xsd.util.LoggingUtil.logHeader;
import static org.xdef.transform.xsd.xd2schema.definition.AlgPhase.TRANSFORMATION;

/**
 * Base class for transformation of facets
 */
public abstract class AbstractDeclarationTypeFactory implements IDeclarationTypeFactory {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractDeclarationTypeFactory.class);

    /**
     * X-definition output declaration type
     */
    private Type type;

    /**
     * X-definition variable declaration name.
     * Used only with type={@link Type.TOP_DECL}
     */
    protected String typeName = null;

    /**
     * Map of facets with single value, which should be transformed.
     * Content is filled by {@link #parseFacets}
     */
    private final Map<String, Object> facetSingleValues = new HashMap<>();

    /**
     * Map of facets with possible multiple values, which should be transformed.
     * Content is filled by {@link #parseFacets}
     */
    private final Map<String, List<Object>> facetMultipleValues = new HashMap<>();

    /**
     * Flag of x-definition declaration builder, if first XSD facet has been converted
     */
    private boolean builderFirstFacet;

    /**
     * Set of marked facet names, which should be removed or have been already transformed
     */
    private Set<String> facetsToRemove = null;

    @Override
    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public void setName(final String typeName) {
        this.typeName = typeName;
    }

    @Override
    public String build(final List<XmlSchemaFacet> facets, final ReportWriter reportWriter) {
        parseFacets(facets, reportWriter);

        if (facetsToRemove != null) {
            for (String facet : facetsToRemove) {
                facetSingleValues.remove(facet);
                facetMultipleValues.remove(facet);
            }
        }

        final String type = hasMultipleFacet(FACET_ENUMERATION) ? XD_FACET_ENUMERATION_TYPE : getDataType();

        final StringBuilder facetStringBuilder = new StringBuilder();
        buildFacets(facetStringBuilder);
        defaultBuildFacets(facetStringBuilder);
        return build(type, facetStringBuilder.toString());
    }

    @Override
    public String build(String facets, final ReportWriter reportWriter) {
        return build(getDataType(), facets);
    }

    /**
     * Creates x-definition declaration type restrictions based on given type and facet string
     * @param type      X-definition output declaration type
     * @param facets    X-definition facet string
     * @return x-definition declaration type
     */
    private String build(final String type, final String facets) {
        StringBuilder sb = new StringBuilder();

        if (Type.TOP_DECL.equals(this.type)) {
            LOG.info("{}Building top declaration. type='{}'", logHeader(TRANSFORMATION), type);
            sb.append("type ")
                    .append(typeName)
                    .append(" ")
                    .append(type);
        } else if (Type.TEXT_DECL.equals(this.type)) {
            LOG.info("{}Building text declaration. type='{}'", logHeader(TRANSFORMATION), type);
            sb.append("required ")
                    .append(type);
        } else if (Type.DATATYPE_DECL.equals(this.type)) {
            LOG.info("{}Building text declaration. type='{}'", logHeader(TRANSFORMATION), type);
            sb.append(type);
        }

        boolean useItemSyntax = facets != null
                && !facets.isEmpty()
                && (this instanceof ListTypeFactory || this instanceof UnionTypeFactory);
        boolean itemArraySyntax = this instanceof UnionTypeFactory;

        sb.append("(");
        if (useItemSyntax) {
            sb.append("%item=");
            if (itemArraySyntax) {
                sb.append("[");
            }
        }

        sb.append(facets);
        if (useItemSyntax && itemArraySyntax) {
            sb.append("]");
        }

        sb.append(")");
        if (!Type.DATATYPE_DECL.equals(this.type)) {
            sb.append(";");
        }

        reset();

        return sb.toString();
    }

    /**
     * Marks facet as resolved
     * @param facetToRemove     facet to be removed
     * @return current instance
     */
    public AbstractDeclarationTypeFactory removeFacet(final String facetToRemove) {
        if (this.facetsToRemove == null) {
            this.facetsToRemove = new HashSet<>();
        }

        this.facetsToRemove.add(facetToRemove);
        return this;
    }

    /**
     * Marks multiple facets as resolved
     * @param facetsToRemove    facets to be removed
     * @return current instance
     */
    public AbstractDeclarationTypeFactory removeFacets(final Set<String> facetsToRemove) {
        if (this.facetsToRemove == null) {
            this.facetsToRemove = new HashSet<>();
        }

        this.facetsToRemove.addAll(facetsToRemove);
        return this;
    }

    /**
     * Parse given XSD facet nodes into internal state
     * @param facets    XSD facet nodes to be transformed
     */
    private void parseFacets(final List<XmlSchemaFacet> facets, final ReportWriter reportWriter) {
        reset();

        if (facets != null && !facets.isEmpty()) {
            for (XmlSchemaFacet facet : facets) {
                if (facet instanceof XmlSchemaFractionDigitsFacet) {
                    facetSingleValues.put(FACET_FRACTIONS_DIGITS, (facet).getValue());
                    LOG.debug("{}Declaration - Add fraction digits. value='{}'",
                            logHeader(TRANSFORMATION, typeName), facet.getValue());
                } else if (facet instanceof XmlSchemaLengthFacet) {
                    facetSingleValues.put(FACET_LENGTH, facet.getValue());
                    LOG.debug("{}Declaration - Add length. value='{}'",
                            logHeader(TRANSFORMATION, typeName), facet.getValue());
                } else if (facet instanceof XmlSchemaMaxExclusiveFacet) {
                    facetSingleValues.put(FACET_MAX_EXCLUSIVE, (facet).getValue());
                    LOG.debug("{}Declaration - Add max exclusive. value='{}'",
                            logHeader(TRANSFORMATION, typeName), facet.getValue());
                } else if (facet instanceof XmlSchemaMaxInclusiveFacet) {
                    facetSingleValues.put(FACET_MAX_INCLUSIVE, (facet).getValue());
                    LOG.debug("{}Declaration - Add max inclusive. value='{}'",
                            logHeader(TRANSFORMATION, typeName), facet.getValue());
                } else if (facet instanceof XmlSchemaMaxLengthFacet) {
                    facetSingleValues.put(FACET_MAX_LENGTH, (facet).getValue());
                    LOG.debug("{}Declaration - Add max length. value='{}'",
                            logHeader(TRANSFORMATION, typeName), facet.getValue());
                } else if (facet instanceof XmlSchemaMinLengthFacet) {
                    facetSingleValues.put(FACET_MIN_LENGTH, (facet).getValue());
                    LOG.debug("{}Declaration - Add min length. value='{}'",
                            logHeader(TRANSFORMATION, typeName), facet.getValue());
                } else if (facet instanceof XmlSchemaMinExclusiveFacet) {
                    facetSingleValues.put(FACET_MIN_EXCLUSIVE, (facet).getValue());
                    LOG.debug("{}Declaration - Add min exclusive. value='{}'",
                            logHeader(TRANSFORMATION, typeName), facet.getValue());
                } else if (facet instanceof XmlSchemaMinInclusiveFacet) {
                    facetSingleValues.put(FACET_MIN_INCLUSIVE, (facet).getValue());
                    LOG.debug("{}Declaration - Add min inclusive. value='{}'",
                            logHeader(TRANSFORMATION, typeName), facet.getValue());
                } else if (facet instanceof XmlSchemaPatternFacet) {
                    final List<Object> patterns = getOrCreateValueList(FACET_PATTERN);
                    patterns.add((facet).getValue());
                    LOG.debug("{}Declaration - Add pattern. value='{}'",
                            logHeader(TRANSFORMATION, typeName), facet.getValue());
                } else if (facet instanceof XmlSchemaTotalDigitsFacet) {
                    facetSingleValues.put(FACET_TOTAL_DIGITS, (facet).getValue());
                    LOG.debug("{}Declaration - Add total digits. value='{}'",
                            logHeader(TRANSFORMATION, typeName), facet.getValue());
                } else if (facet instanceof XmlSchemaWhiteSpaceFacet) {
                    facetSingleValues.put(FACET_WHITESPACE, (facet).getValue());
                    LOG.debug("{}Declaration - Add whitespace. value='{}'",
                            logHeader(TRANSFORMATION, typeName), facet.getValue());
                } else if (facet instanceof XmlSchemaEnumerationFacet) {
                    final List<Object> enumeration = getOrCreateValueList(FACET_ENUMERATION);
                    enumeration.add((facet).getValue());
                    LOG.debug("{}Declaration - Add enumeration. value='{}'",
                            logHeader(TRANSFORMATION, typeName), facet.getValue());
                } else {
                    reportWriter.warning(XSD.XSD216, facet.getClass().getSimpleName());
                    LOG.warn("{}Declaration - Unsupported XSD facet! clazz='{}'",
                            logHeader(TRANSFORMATION, typeName), facet.getClass().getSimpleName());
                }
            }
        }
    }

    /**
     * Check if facet of given name is stored internally
     * @param facetName     facet to be found
     * @return true, if facet is stored internally
     */
    protected boolean hasFacet(final String facetName) {
        return facetSingleValues.containsKey(facetName);
    }

    /**
     * Remove facet of given name from internal storage and return value of facet
     * @param facetName     facet to be removed
     * @return value of removed facet, null if facet is not stored
     */
    protected Object useFacet(final String facetName) {
        return facetSingleValues.remove(facetName);
    }

    /**
     * Get value of internally stored facet
     * @param facetName     facet's value to be returned
     * @return value of facet, null if facet is not stored
     */
    protected Object getFacet(final String facetName) {
        return facetSingleValues.get(facetName);
    }

    /**
     * Check if facet using multiple values of given name is stored internally
     * @param facetName     facet to be found
     * @return true, if facet is stored internally
     */
    protected boolean hasMultipleFacet(final String facetName) {
        return facetMultipleValues.containsKey(facetName);
    }

    /**
     * Check if facet is using any multiple value
     * @return true, if any facet is stored internally
     */
    protected boolean hasNoMultipleFacet() {
        return facetMultipleValues.isEmpty();
    }

    /**
     * Remove facet using multiple values of given name from internal storage and return value of facet
     * @param facetName     facet to be removed
     * @return value of removed facet, null if facet is not stored
     */
    protected List<Object> useMultipleFacet(final String facetName) {
        return facetMultipleValues.remove(facetName);
    }

    /**
     * Custom implementation of converting XSD facet into x-definition declaration.
     * Called before {@link #defaultBuildFacets}
     * @param sb string builder where should be custom string appended
     */
    protected void buildFacets(final StringBuilder sb) {
    }

    /**
     * X-definition declaration builder helper for concatenating facets values
     * @param sb        facet string builder
     * @param value     facet value to be appended
     */
    protected void facetBuilder(final StringBuilder sb, final Object value) {
        if (!builderFirstFacet) {
            sb.append(", ").append(value);
        } else {
            sb.append(value);
            builderFirstFacet = false;
        }
    }

    /**
     * Default implementation of transforming XSD facets to x-definition declaration
     * @param sb
     */
    protected void defaultBuildFacets(final StringBuilder sb) {
        if (hasFacet(FACET_FRACTIONS_DIGITS)) {
            facetBuilder(sb, "%fractionDigits='" + useFacet(FACET_FRACTIONS_DIGITS) + "'");
        }
        if (hasFacet(FACET_LENGTH)) {
            facetBuilder(sb, "%length='" + useFacet(FACET_LENGTH) + "'");
        }
        if (hasFacet(FACET_MAX_EXCLUSIVE)) {
            facetBuilder(sb, "%maxExclusive='" + useFacet(FACET_MAX_EXCLUSIVE) + "'");
        }
        if (hasFacet(FACET_MAX_INCLUSIVE)) {
            facetBuilder(sb, "%maxInclusive='" + useFacet(FACET_MAX_INCLUSIVE) + "'");
        }
        if (hasFacet(FACET_MAX_LENGTH)) {
            facetBuilder(sb, "%maxLength='" + useFacet(FACET_MAX_LENGTH) + "'");
        }
        if (hasFacet(FACET_MIN_LENGTH)) {
            facetBuilder(sb, "%minLength='" + useFacet(FACET_MIN_LENGTH) + "'");
        }
        if (hasFacet(FACET_MIN_EXCLUSIVE)) {
            facetBuilder(sb, "%minExclusive='" + useFacet(FACET_MIN_EXCLUSIVE) + "'");
        }
        if (hasFacet(FACET_MIN_INCLUSIVE)) {
            facetBuilder(sb, "%minInclusive='" + useFacet(FACET_MIN_INCLUSIVE) + "'");
        }
        if (hasMultipleFacet(FACET_PATTERN)) {
            final List<Object> patterns = useMultipleFacet(FACET_PATTERN);
            if (patterns != null && !patterns.isEmpty()) {
                facetBuilder(sb, "%pattern=['");

                if (patterns.size() == 1) {
                    sb.append(patterns.get(0).toString().replace("\\", "\\\\"));
                } else {
                    Iterator<Object> patternItr = patterns.iterator();
                    sb.append('(');
                    sb.append(patternItr.next().toString().replace("\\", "\\\\"));
                    sb.append(')');
                    while (patternItr.hasNext()) {
                        sb.append("|(");
                        sb.append(patternItr.next().toString().replace("\\", "\\\\"));
                        sb.append(')');
                    }
                }

                sb.append("']");
            }
        }
        if (hasFacet(FACET_TOTAL_DIGITS)) {
            facetBuilder(sb, "%totalDigits='" + useFacet(FACET_TOTAL_DIGITS) + "'");
        }
        if (hasFacet(FACET_WHITESPACE)) {
            facetBuilder(sb, "%whiteSpace='" + useFacet(FACET_WHITESPACE) + "'");
        }
        if (hasMultipleFacet(FACET_ENUMERATION)) {
            final List<Object> enumeration = useMultipleFacet(FACET_ENUMERATION);
            if (enumeration != null && !enumeration.isEmpty()) {
                Iterator<Object> enumItr = enumeration.iterator();
                sb.append("\"" + enumItr.next() + "\"");
                while (enumItr.hasNext()) {
                    sb.append(", \"" + enumItr.next() + "\"");
                }
            }
        }
    }

    /**
     * Get or create new list for facet using multiple values
     * @param facet     facet name using multiple values
     * @return list of facet's values
     */
    protected List<Object> getOrCreateValueList(final String facet) {
        final List<Object> list = facetMultipleValues.computeIfAbsent(facet, key -> new LinkedList<>());
        return list;
    }

    /**
     * Resets internal state of factory
     */
    private void reset() {
        facetSingleValues.clear();
        facetMultipleValues.clear();
        builderFirstFacet = true;
    }
}
