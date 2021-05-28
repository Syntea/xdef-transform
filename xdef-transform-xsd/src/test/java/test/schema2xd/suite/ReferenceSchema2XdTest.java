package test.schema2xd.suite;

import org.junit.jupiter.api.Test;
import test.schema2xd.AbstractSchema2XdTransformSuite;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author smid
 * @since 2021-05-28
 */
public class ReferenceSchema2XdTest extends AbstractSchema2XdTransformSuite {

    public static final String SUITE_NAME = "reference";

    @Test
    public void ref_ChildAttr2Attr() {
        initTestCaseDirs(SUITE_NAME, "ATTR_CHLD_to_ATTR");
        convertXsd2XDefNoRef("ATTR_CHLD_to_ATTR", Collections.singletonList("ATTR_CHLD_to_ATTR_valid_1"), null);
    }

    @Test
    public void ref_ChildAttr2ChildAttr() {
        initTestCaseDirs(SUITE_NAME, "ATTR_CHLD_to_ATTR_CHLD");
        convertXsd2XDefNoRef("ATTR_CHLD_to_ATTR_CHLD", Collections.singletonList("ATTR_CHLD_to_ATTR_CHLD_valid_1"), null);
    }

    @Test
    public void ref_ChildAttr2Child() {
        initTestCaseDirs(SUITE_NAME, "ATTR_CHLD_to_CHLD");
        convertXsd2XDefNoRef("ATTR_CHLD_to_CHLD", Collections.singletonList("ATTR_CHLD_to_CHLD_valid_1"), null);
    }

    @Test
    public void ref_Attr2Attr() {
        initTestCaseDirs(SUITE_NAME, "ATTR_to_ATTR");
        convertXsd2XDefNoRef(
                "ATTR_to_ATTR",
                Arrays.asList("ATTR_to_ATTR_valid_1", "ATTR_to_ATTR_valid_2"),
                Arrays.asList("ATTR_to_ATTR_invalid_1", "ATTR_to_ATTR_invalid_2"));
    }

    @Test
    public void ref_Attr2ChildAttr() {
        initTestCaseDirs(SUITE_NAME, "ATTR_to_ATTR_CHLD");
        convertXsd2XDefNoRef("ATTR_to_ATTR_CHLD", Collections.singletonList("ATTR_to_ATTR_CHLD_valid_1"), null);
    }

    @Test
    public void ref_Attr2Child() {
        initTestCaseDirs(SUITE_NAME, "ATTR_to_CHLD");
        convertXsd2XDefNoRef("ATTR_to_CHLD", Collections.singletonList("ATTR_to_CHLD_valid_1"), null);
    }

    @Test
    public void ref_Child2Attr() {
        initTestCaseDirs(SUITE_NAME, "CHLD_to_ATTR");
        convertXsd2XDefNoRef("CHLD_to_ATTR", Collections.singletonList("CHLD_to_ATTR_valid_1"), null);
    }

    @Test
    public void ref_Child2ChildAttr() {
        initTestCaseDirs(SUITE_NAME, "CHLD_to_ATTR_CHLD");
        convertXsd2XDefNoRef("CHLD_to_ATTR_CHLD", Collections.singletonList("CHLD_to_ATTR_CHLD_valid_1"), null);
    }

    @Test
    public void ref_Child2Child() {
        initTestCaseDirs(SUITE_NAME, "CHLD_to_CHLD");
        convertXsd2XDefNoRef("CHLD_to_CHLD", Collections.singletonList("CHLD_to_CHLD_valid_1"), null);
    }

}
