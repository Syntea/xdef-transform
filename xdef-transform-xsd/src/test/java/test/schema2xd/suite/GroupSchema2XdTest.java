package test.schema2xd.suite;

import org.junit.jupiter.api.Test;
import test.schema2xd.AbstractSchema2XdTransformSuite;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author smid
 * @since 2021-05-28
 */
public class GroupSchema2XdTest extends AbstractSchema2XdTransformSuite {

    public static final String SUITE_NAME = "group";

    @Test
    public void group_1() {
        initTestCaseDirs(SUITE_NAME, "testGroup1");
        convertXsd2XDefNoRef(
                "testGroup1",
                Arrays.asList("testGroup1_valid_1", "testGroup1_valid_2"),
                null);
    }

    @Test
    public void group_2() {
        initTestCaseDirs(SUITE_NAME, "testGroup2");
        convertXsd2XDefNoRef("testGroup2", Collections.singletonList("testGroup2_valid_1"), null);
    }

    @Test
    public void group_3() {
        initTestCaseDirs(SUITE_NAME, "testGroup3");
        convertXsd2XDefNoRef("testGroup3", Collections.singletonList("testGroup3_valid_1"), null);
    }

    @Test
    public void all_1() {
        initTestCaseDirs(SUITE_NAME, "groupMixed4");
        convertXsd2XDefNoRef(
                "groupMixed4",
                Collections.singletonList("groupMixed4_valid_1"),
                Arrays.asList("groupMixed4_invalid_1", "groupMixed4_invalid_2"));
    }

    @Test
    public void all_2() {
        initTestCaseDirs(SUITE_NAME, "groupMixed5");
        convertXsd2XDefNoRef(
                "groupMixed5",
                Arrays.asList("groupMixed5_valid_1", "groupMixed5_valid_2", "groupMixed5_valid_3"),
                null);
    }

    @Test
    public void all_3() {
        initTestCaseDirs(SUITE_NAME, "groupMixed6");
        convertXsd2XDefNoRef(
                "groupMixed6",
                Arrays.asList("groupMixed6_valid_1", "groupMixed6_valid_2", "groupMixed6_valid_3", "groupMixed6_valid_4"),
                Collections.singletonList("groupMixed6_invalid_2"));
    }

}
