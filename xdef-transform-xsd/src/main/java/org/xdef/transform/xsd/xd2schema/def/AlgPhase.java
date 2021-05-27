package org.xdef.transform.xsd.xd2schema.def;

/**
 * Enum of algorithm phases describing individual phase
 */
public enum AlgPhase {
    INITIALIZATION("Initialization"),       // Used for preparation process of transformation X-Definition to org.xdef.transform.xsd
    PREPROCESSING("Pre-processing"),        // Used for extracting data from X-Definition tree
    TRANSFORMATION("Transformation"),       // Used for tree transformation of X-Definition elements
    POSTPROCESSING("Post-processing");      // Used for post transformation based on gathered nodes and information

    private final String val;

    AlgPhase(String val) {
        this.val = val;
    }

    public String getVal() {
        return val;
    }
}
