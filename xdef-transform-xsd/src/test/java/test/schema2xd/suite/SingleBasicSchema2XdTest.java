package test.schema2xd.suite;

import org.junit.jupiter.api.Test;
import test.schema2xd.AbstractSchema2XdTransformSuite;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author smid
 * @since 2021-05-28
 */
public class SingleBasicSchema2XdTest extends AbstractSchema2XdTransformSuite {

    public static final String SUITE_NAME = "single-basic";

    @Test
    public void basic_t000() {
        initTestCaseDirs(SUITE_NAME, "t000");
        convertXsd2XDef(
                "t000",
                Collections.singletonList("t000"),
                Arrays.asList("t000_1e", "t000_2e", "t000_3e"));
    }

    @Test
    public void basic_t001() {
        initTestCaseDirs(SUITE_NAME, "t001");
        convertXsd2XDefNoRef(
                "t001",
                Collections.singletonList("t001"),
                Arrays.asList("t001_1e", "t001_2e", "t001_3e", "t001_4e", "t001e"));
    }

    @Test
    public void basic_t002() {
        initTestCaseDirs(SUITE_NAME, "t002");
        convertXsd2XDefNoRef(
                "t002",
                Collections.singletonList("t002"),
                Arrays.asList("t002_1e", "t002_2e"));
    }

    @Test
    public void basic_t003() {
        initTestCaseDirs(SUITE_NAME, "t003");
        convertXsd2XDefNoRef(
                "t003",
                Collections.singletonList("t003"),
                Collections.singletonList("t003_1e"));
    }

    @Test
    public void basic_t004() {
        initTestCaseDirs(SUITE_NAME, "t004");
        convertXsd2XDefNoRef(
                "t004",
                Collections.singletonList("t004"),
                Collections.singletonList("t004_1e"));
    }

    @Test
    public void basic_t005() {
        initTestCaseDirs(SUITE_NAME, "t005");
        convertXsd2XDefNoRef("t005", Collections.singletonList("t005"), null);
    }

    @Test
    public void basic_t006() {
        initTestCaseDirs(SUITE_NAME, "t006");
        convertXsd2XDefNoRef(
                "t006",
                Arrays.asList("t006", "t006_1"),
                Arrays.asList("t006_2e", "t006_3e"));
    }

    @Test
    public void basic_t007() {
        initTestCaseDirs(SUITE_NAME, "t007");
        convertXsd2XDefNoRef(
                "t007",
                Collections.singletonList("t007"),
                Collections.singletonList("t007_1e"));
    }

    @Test
    public void basic_t009() {
        initTestCaseDirs(SUITE_NAME, "t009");
        convertXsd2XDefNoRef("t009", Collections.singletonList("t009"), null);
    }

    @Test
    public void basic_t010() {
        initTestCaseDirs(SUITE_NAME, "t010");
        convertXsd2XDefNoRef("t010", Collections.singletonList("t010"), null);
    }

    @Test
    public void basic_t016() {
        initTestCaseDirs(SUITE_NAME, "t016");
        convertXsd2XDefNoRef(
                "t016",
                Collections.singletonList("t016"),
                Collections.singletonList("t016e"));
    }

    @Test
    public void basic_t019() {
        initTestCaseDirs(SUITE_NAME, "t019");
        convertXsd2XDefNoRef("t019", Collections.singletonList("t019"), null);
    }

    @Test
    public void basic_t020() {
        initTestCaseDirs(SUITE_NAME, "t020");
        convertXsd2XDefNoRef("t020", Collections.singletonList("t020"), null);
    }

    @Test
    public void basic_t020_2() {
        initTestCaseDirs(SUITE_NAME, "t020_1");
        convertXsd2XDefNoRef("t020_1", Collections.singletonList("t020_1"), null);
    }

    @Test
    public void basic_t021b() {
        initTestCaseDirs(SUITE_NAME, "t021b");
        convertXsd2XDefNoRef("t021b", Collections.singletonList("t021"), null);
    }

    @Test
    public void basic_t990() {
        initTestCaseDirs(SUITE_NAME, "t990");
        convertXsd2XDefNoRef(
                "t990",
                Arrays.asList("t990", "t990_1"),
                Arrays.asList("t990_1e", "t990_2e", "t990_3e", "t990_4e", "t990_5e"));
    }

    @Test
    public void basic_Inf() {
        initTestCaseDirs(SUITE_NAME, "test_Inf");
        convertXsd2XDefNoRef("test_Inf", Collections.singletonList("test_Inf_valid"), null);
    }

    @Test
    public void basic_BasicSchema_1() {
        initTestCaseDirs(SUITE_NAME, "basicTestSchema");
        convertXsd2XDefNoRef("basicTestSchema", Collections.singletonList("basicTest_valid_1"), null);
    }

    @Test
    public void basic_BasicSchema_2() {
        initTestCaseDirs(SUITE_NAME, "basicTest");
        convertXsd2XDefNoRef(
                "basicTest",
                Arrays.asList("basicTest_valid_1", "basicTest_valid_2", "basicTest_valid_3"),
                Arrays.asList("basicTest_invalid_1", "basicTest_invalid_2", "basicTest_invalid_3", "basicTest_invalid_4"));
    }

    @Test
    public void basic_DateTime() {
        initTestCaseDirs(SUITE_NAME, "dateTimeTest");
        convertXsd2XDefNoRef("dateTimeTest", Collections.singletonList("dateTimeTest_valid_1"), null);
    }

    @Test
    public void basic_Declaration() {
        initTestCaseDirs(SUITE_NAME, "declarationTest");
        convertXsd2XDefNoRef(
                "declarationTest",
                Arrays.asList("declarationTest_valid_1", "declarationTest_valid_2", "declarationTest_valid_3"),
                Arrays.asList("declarationTest_invalid_1", "declarationTest_invalid_2", "declarationTest_invalid_3", "declarationTest_invalid_4"));
    }

    @Test
    public void basic_M1RC() {
        initTestCaseDirs(SUITE_NAME, "M1RC");
        convertXsd2XDefNoRef("M1RC", Collections.singletonList("M1RC"), null);
    }

    @Test
    public void basic_M1RT() {
        initTestCaseDirs(SUITE_NAME, "M1RT");
        convertXsd2XDefNoRef("M1RT", Collections.singletonList("M1RT"), null);
    }

    @Test
    public void basic_DefaultValue() {
        initTestCaseDirs(SUITE_NAME, "defaultValue1");
        convertXsd2XDefNoRef("defaultValue1", Collections.singletonList("defaultValue1_valid_1"), null);
    }

}
