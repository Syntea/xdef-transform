package test.schema2xd.suite;

import org.junit.jupiter.api.Test;
import test.schema2xd.AbstractSchema2XdTransformSuite;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author smid
 * @since 2021-05-28
 */
public class SingleExtendedSchema2XdTest extends AbstractSchema2XdTransformSuite {

    public static final String SUITE_NAME = "single-extended";

    @Test
    public void extended_00015() {
        initTestCaseDirs(SUITE_NAME, "test_00015");
        convertXsd2XDefNoRef("test_00015", Collections.singletonList("test_00015_data"), null);
    }

    @Test
    public void extended_SchemaTypes() {
        initTestCaseDirs(SUITE_NAME, "typeTestSchema");
        convertXsd2XDefNoRef("typeTestSchema", Collections.singletonList("typeTest_valid_1"), null);
    }

    @Test
    public void extended_SimpleModel() {
        initTestCaseDirs(SUITE_NAME, "simpleModelTest");
        convertXsd2XDefNoRef(
                "simpleModelTest",
                Arrays.asList("simpleModelTest_valid_1", "simpleModelTest_valid_2"/*, "simpleModelTest_valid_3"*/, "simpleModelTest_valid_5", "simpleModelTest_valid_5"),
                null);
    }

    @Test
    public void extended_B1_common() {
        initTestCaseDirs(SUITE_NAME, "B1_common");
        convertXsd2XDefNoRef(
                "B1_common",
                Arrays.asList("B1_Common_valid_1", "B1_Common_valid_2"),
                null);
    }

    @Test
    public void extended_D1A() {
        initTestCaseDirs(SUITE_NAME, "D1A");
        convertXsd2XDefNoRef("D1A", Collections.singletonList("D1A"), null);
    }

    @Test
    public void extended_D2A() {
        initTestCaseDirs(SUITE_NAME, "D2A");
        convertXsd2XDefNoRef("D2A", Collections.singletonList("D2A"), null);
    }

    @Test
    public void extended_D3A() {
        initTestCaseDirs(SUITE_NAME, "D3A");
        convertXsd2XDefNoRef("D3A", Collections.singletonList("D3A"), null);
    }

    @Test
    public void extended_D5() {
        initTestCaseDirs(SUITE_NAME, "D5");
        convertXsd2XDefNoRef("D5", Collections.singletonList("D5"), null);
    }

    @Test
    public void extended_L1A() {
        initTestCaseDirs(SUITE_NAME, "L1A");
        convertXsd2XDefNoRef("L1A", Collections.singletonList("L1A"), null);
    }

    @Test
    public void extended_M1RN() {
        initTestCaseDirs(SUITE_NAME, "M1RN");
        convertXsd2XDefNoRef("M1RN", Collections.singletonList("M1RN"), null);
    }

    @Test
    public void extended_M1RS() {
        initTestCaseDirs(SUITE_NAME, "M1RS");
        convertXsd2XDefNoRef("M1RS", Collections.singletonList("M1RS"), null);
    }

    @Test
    public void extended_M1RV() {
        initTestCaseDirs(SUITE_NAME, "M1RV");
        convertXsd2XDefNoRef("M1RV", Collections.singletonList("M1RV"), null);
    }

    @Test
    public void extended_P1A() {
        initTestCaseDirs(SUITE_NAME, "P1A");
        convertXsd2XDefNoRef("P1A", Collections.singletonList("P1A"), null);
    }
    
}
