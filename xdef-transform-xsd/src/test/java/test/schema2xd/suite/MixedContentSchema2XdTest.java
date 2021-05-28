package test.schema2xd.suite;

import org.junit.jupiter.api.Test;
import test.schema2xd.AbstractSchema2XdTransformSuite;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author smid
 * @since 2021-05-28
 */
public class MixedContentSchema2XdTest extends AbstractSchema2XdTransformSuite {

    public static final String SUITE_NAME = "mixed_content";

    @Test
    public void mixedContent_1() {
        initTestCaseDirs(SUITE_NAME, "t022");
        convertXsd2XdPoolNoRef(
                "t022",
                Arrays.asList("t022", "t022_1", "t022_2", "t022_3"),
                null);
    }

    @Test
    public void mixedContent_2() {
        initTestCaseDirs(SUITE_NAME, "t023");
        convertXsd2XdPoolNoRef(
                "t023",
                Arrays.asList("t023", "t023_1", "t023_2", "t023_3", "t023_4", "t023_5", "t023_6"),
                null);
    }

    @Test
    public void mixedContent_3() {
        initTestCaseDirs(SUITE_NAME, "simpleRefTest");
        convertXsd2XDefNoRef(
                "simpleRefTest",
                Collections.singletonList("simpleRefTest_valid_1"),
                null);
    }

    @Test
    public void mixedContent_4() {
        initTestCaseDirs(SUITE_NAME, "t021a");
        convertXsd2XDefNoRef(
                "t021a",
                Collections.singletonList("t021"),
                null);
    }

}
