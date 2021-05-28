package test.xd2schema.suite;

import org.junit.jupiter.api.Test;
import test.xd2schema.AbstractXd2SchemaTransformSuite;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author smid
 * @since 2021-05-28
 */
public class GroupXd2SchemaTest extends AbstractXd2SchemaTransformSuite {

    public static final String SUITE_NAME = "group";

    @Test
    public void group_choice_1() {
        initCaseDirs(SUITE_NAME, "groupChoice1");
        convertXd2SchemaNoRef(
                "groupChoice1",
                Arrays.asList("groupChoice1_valid_1", "groupChoice1_valid_2"),
                Arrays.asList("groupChoice1_invalid_1", "groupChoice1_invalid_2"));
    }

    @Test
    public void group_choice_2() {
        initCaseDirs(SUITE_NAME, "groupChoice2");
        convertXd2SchemaNoRef(
                "groupChoice2",
                Arrays.asList("groupChoice2_valid_1", "groupChoice2_valid_2"),
                Collections.singletonList("groupChoice2_invalid_1"));
    }

    @Test
    public void group_choice_3() {
        initCaseDirs(SUITE_NAME, "groupChoice3");
        convertXd2SchemaNoRef(
                "groupChoice3",
                Arrays.asList("groupChoice3_valid_1", "groupChoice3_valid_2", "groupChoice3_valid_3", "groupChoice3_valid_4", "groupChoice3_valid_5"),
                Arrays.asList("groupChoice3_invalid_1", "groupChoice3_invalid_2", "groupChoice3_invalid_3"));
    }

    @Test
    public void group_choice_4() {
        initCaseDirs(SUITE_NAME, "groupChoice4");
        convertXd2SchemaNoRef(
                "groupChoice4",
                Arrays.asList("groupChoice4_valid_1", "groupChoice4_valid_2", "groupChoice4_valid_3"),
                Arrays.asList("groupChoice4_invalid_1", "groupChoice4_invalid_2"));
    }

    @Test
    public void group_mixed_1() {
        initCaseDirs(SUITE_NAME, "groupMixed1");
        convertXd2SchemaNoRef(
                "groupMixed1",
                Arrays.asList("groupMixed1_valid_1", "groupMixed1_valid_2"),
                Collections.singletonList("groupMixed1_invalid_1"));
    }

    @Test
    public void group_mixed_2() {
        initCaseDirs(SUITE_NAME, "groupMixed2");
        convertXd2SchemaNoRef(
                "groupMixed2",
                Arrays.asList("groupMixed2_valid_1", "groupMixed2_valid_2", "groupMixed2_valid_3", "groupMixed2_valid_4"),
                Collections.singletonList("groupMixed2_invalid_1"));
    }

    @Test
    public void group_mixed_3() {
        initCaseDirs(SUITE_NAME, "groupMixed3");
        convertXd2SchemaNoRef(
                "groupMixed3",
                Arrays.asList("groupMixed3_valid_1", "groupMixed3_valid_2"),
                Arrays.asList("groupMixed3_invalid_1", "groupMixed3_invalid_2"));
    }

    @Test
    public void group_mixed_4() {
        initCaseDirs(SUITE_NAME, "groupMixed4");
        convertXd2SchemaNoRef(
                "groupMixed4",
                Collections.singletonList("groupMixed4_valid_1"),
                Arrays.asList("groupMixed4_invalid_1", "groupMixed4_invalid_2"));
    }

    @Test
    public void group_mixed_5() {
        initCaseDirs(SUITE_NAME, "groupMixed5");
        convertXd2SchemaNoRef(
                "groupMixed5",
                Arrays.asList("groupMixed5_valid_1", "groupMixed5_valid_2", "groupMixed5_valid_3"),
                Collections.singletonList("groupMixed5_invalid_1"));
    }

    @Test
    public void group_mixed_6() {
        initCaseDirs(SUITE_NAME, "groupMixed6");
        convertXd2SchemaNoRef(
                "groupMixed6",
                Arrays.asList("groupMixed6_valid_1", "groupMixed6_valid_2", "groupMixed6_valid_3", "groupMixed6_valid_4"),
                Arrays.asList("groupMixed6_invalid_1", "groupMixed6_invalid_2"));
    }

    @Test
    public void group_1() {
        initCaseDirs(SUITE_NAME, "testGroup1");
        convertXd2SchemaNoRef("testGroup1", Arrays.asList("testGroup1_valid_1", "testGroup1_valid_2", "testGroup1_valid_3"), null);
    }

    @Test
    public void group_2() {
        initCaseDirs(SUITE_NAME, "testGroup2");
        convertXd2SchemaNoRef("testGroup2", Collections.singletonList("testGroup2_valid_1"), null);
    }

    @Test
    public void group_3() {
        initCaseDirs(SUITE_NAME, "testGroup3");
        convertXd2SchemaNoRef("testGroup3", Collections.singletonList("testGroup3_valid_1"), null);
    }

}
