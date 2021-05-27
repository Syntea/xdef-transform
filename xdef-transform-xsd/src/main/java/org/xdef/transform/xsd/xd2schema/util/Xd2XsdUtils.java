package org.xdef.transform.xsd.xd2schema.util;


import org.apache.commons.lang3.tuple.Pair;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAll;
import org.apache.ws.commons.schema.XmlSchemaChoice;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;
import org.xdef.impl.XElement;
import org.xdef.model.XMElement;
import org.xdef.transform.xsd.xd2schema.def.Xd2XsdFeature;
import org.xdef.transform.xsd.xd2schema.model.impl.XsdAdapterCtx;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.xdef.model.XMNode.XMCHOICE;
import static org.xdef.model.XMNode.XMMIXED;
import static org.xdef.model.XMNode.XMSEQUENCE;

/**
 * Basic utils used in transformation X-Definition -> XSD
 */
public class Xd2XsdUtils {

    static private final Pattern ciPattern = Pattern.compile("[a-zA-Z]");

    /**
     * Add XML Schema document type node to top level of given XML Schema document
     * @param schema        XML Schema document
     * @param schemaType    XML Schema document type node
     */
    public static void addSchemaTypeNode2TopLevel(final XmlSchema schema, final XmlSchemaType schemaType) {
        schema.getItems().add(schemaType);
    }

    /**
     * Removes node from given XML Schema document
     * @param schema        XML Schema document
     * @param xmlObj        XML Schema node
     */
    public static void removeNode(final XmlSchema schema, final XmlSchemaObject xmlObj) {
        schema.getItems().remove(xmlObj);
    }

    /**
     * Convert X-Definition particle kind to string
     * @param kind  X-Definition particle kind
     * @return name of X-Definition particle kind
     */
    public static String particleXKindToString(short kind) {
        String result = "unknown";
        switch (kind) {
            case XMSEQUENCE:    result = "sequence"; break;
            case XMMIXED:       result = "mixed"; break;
            case XMCHOICE:      result = "choice"; break;
        }

        return result;
    }

    /**
     * Transform given regular expression to case insensitive regular expression
     * @param regex     sensitive case regular expression
     * @return case insensitive regular expression
     */
    public static String regex2CaseInsensitive(final String regex) {
        final Matcher matcher = ciPattern.matcher(regex);
        final StringBuilder stringBuilder = new StringBuilder();
        int lastMatchPos = 0;
        while (matcher.find()) {
            stringBuilder.append(regex, lastMatchPos, matcher.start())
                    .append("[")
                    .append(matcher.group(0).toLowerCase())
                    .append(matcher.group(0).toUpperCase())
                    .append("]");

            lastMatchPos = matcher.end();
        }

        return stringBuilder.toString();
    }

    /**
     * Transform given collection of regular expressions to single regular expression (joined by '|')
     * @param regex     collection of regular expressions
     * @return single regular expression
     */
    public static String regexCollectionToSingle(Collection<String> regex) {
        return String.join("|", regex);
    }

    /**
     * Parse given X-Definition pos without attribute at the end
     * @param xdPos     X-Definition position
     * @return X-Definition position without attribute
     */
    public static String xPathWithoutAttr(final String xdPos) {
        final int paramPos = xdPos.lastIndexOf("/@");
        if (paramPos != -1) {
            return xdPos.substring(0, paramPos);
        }

        return xdPos;
    }

    /**
     * Creates relative XPath from given absolute XPath
     * @param xPath         absolute full XPath
     * @param xPathNode     absolute current XPath (part of {@code xPath})
     * @return relative XPath
     */
    public static String relativeXPath(final String xPath, final String xPathNode) {
        final int pos = xPath.indexOf(xPathNode);
        if (pos != -1) {
            return xPath.substring(pos + xPathNode.length() + 1);
        }

        return xPath;
    }

    /**
     * Checks if given X-Definition element is any type
     * @param xElem     X-Definition element node
     * @return  true if X-Definition element node is any
     */
    public static boolean isAnyElement(final XElement xElem) {
        return "$any".equals(xElem.getName());
    }

    /**
     * Checks if given X-Definition element contains any node
     * @param xElem     X-Definition element node
     * @return  true if X-Definition element node contains any node
     */
    public static boolean containsAnyElement(final XElement xElem) {
        return xElem.getName().endsWith("$any");
    }

    /**
     * Checks if given X-Definition element contains mixed node
     * @param xElem     X-Definition element node
     * @return  true if X-Definition element node contains mixed node
     */
    public static boolean containsMixedElement(final XMElement xElem) {
        return xElem.getName().endsWith("$mixed");
    }

    /**
     * Calculates total occurrence of member nodes inside XML Schema all node
     * @param groupParticleAll  XML Schema all node
     * @param adapterCtx        XML Schema adapter context
     * @return total occurrence of nodes
     */
    public static Pair<Long, Long> calculateGroupAllMembersOccurrence(final XmlSchemaAll groupParticleAll,
                                                                      final XsdAdapterCtx adapterCtx) {
        final XmlSchemaObjectBase[] members = new XmlSchemaObjectBase[groupParticleAll.getItems().size()];
        groupParticleAll.getItems().toArray(members);
        return calculateGroupParticleMembersOccurrence(members, adapterCtx);
    }

    /**
     * Calculates total occurrence of member nodes inside XML Schema choice node
     * @param groupParticleChoice   XML Schema choice node
     * @param adapterCtx            XML Schema adapter context
     * @return total occurrence of nodes
     */
    public static Pair<Long, Long> calculateGroupChoiceMembersOccurrence(final XmlSchemaChoice groupParticleChoice,
                                                                         final XsdAdapterCtx adapterCtx) {
        final XmlSchemaObjectBase[] members = new XmlSchemaObjectBase[groupParticleChoice.getItems().size()];
        groupParticleChoice.getItems().toArray(members);
        return calculateGroupParticleMembersOccurrence(members, adapterCtx);
    }

    private static Pair<Long, Long> calculateGroupParticleMembersOccurrence(final XmlSchemaObjectBase[] members,
                                                                            final XsdAdapterCtx adapterCtx) {
        long elementMaxOccursSum = 0;
        long elementMinOccursSum = 0;

        for (XmlSchemaObjectBase member : members) {
            if (member instanceof XmlSchemaParticle) {
                final XmlSchemaParticle memberParticle = (XmlSchemaParticle) member;
                if (memberParticle.getMaxOccurs() < Long.MAX_VALUE) {
                    elementMaxOccursSum += memberParticle.getMaxOccurs();
                } else {
                    elementMaxOccursSum = Long.MAX_VALUE;
                    break;
                }

                if (memberParticle.getMinOccurs() < Long.MAX_VALUE) {
                    elementMinOccursSum += memberParticle.getMinOccurs();
                } else {
                    elementMinOccursSum = Long.MAX_VALUE;
                    break;
                }
            }
        }

        if (adapterCtx.hasEnableFeature(Xd2XsdFeature.XSD_ALL_UNBOUNDED)) {
            elementMaxOccursSum = Long.MAX_VALUE;
        }

        return Pair.of(elementMinOccursSum, elementMaxOccursSum);
    }

    /**
     * Features which should be enabled by default for transformation algorithm
     * @return default algorithm features
     */
    public static Set<Xd2XsdFeature> defaultFeatures() {
        return new HashSet<>(Xd2XsdFeature.DEFAULT_POSTPROCESSING_FEATURES);
    }
}
