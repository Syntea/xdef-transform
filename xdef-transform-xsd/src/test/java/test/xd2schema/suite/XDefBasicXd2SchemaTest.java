package test.xd2schema.suite;

import org.junit.jupiter.api.Test;
import org.xdef.transform.xsd.xd2schema.def.Xd2XsdFeature;
import test.xd2schema.AbstractXd2SchemaTransformSuite;

import java.util.Collections;
import java.util.EnumSet;

/**
 * @author smid
 * @since 2021-05-28
 */
public class XDefBasicXd2SchemaTest extends AbstractXd2SchemaTransformSuite {

    public static final String SUITE_NAME = "xdef-basic";
    
    @Test
    public void basic_t000() {
        initTestCaseDirs(SUITE_NAME, "t000");
        transformXd2Schema("t000", Collections.singletonList("t000"), null);
    }

    @Test
    public void basic_t001() {
        initTestCaseDirs(SUITE_NAME, "t001");
        transformXd2Schema("t001", Collections.singletonList("t001"), null);
    }

    @Test
    public void basic_t002() {
        initTestCaseDirs(SUITE_NAME, "t002");
        transformXd2Schema("t002", Collections.singletonList("t002"), null);
    }

    @Test
    public void basic_t003() {
        initTestCaseDirs(SUITE_NAME, "t003");
        transformXd2Schema("t003", Collections.singletonList("t003"), null);
    }

    @Test
    public void basic_t004() {
        initTestCaseDirs(SUITE_NAME, "t004");
        transformXd2Schema("t004", Collections.singletonList("t004"), null);
    }

    @Test
    public void basic_t005() {
        initTestCaseDirs(SUITE_NAME, "t005");
        transformXd2Schema("t005", Collections.singletonList("t005"), null);
    }

    @Test
    public void basic_t007() {
        initTestCaseDirs(SUITE_NAME, "t007");
        transformXd2Schema("t007", Collections.singletonList("t007"), null);
    }

    @Test
    public void basic_t007b() {
        initTestCaseDirs(SUITE_NAME, "t007b");
        transformXd2Schema(
                "t007",
                Collections.singletonList("t007"),
                null,
                true,
                null,
                false,
                EnumSet.of(Xd2XsdFeature.XSD_ELEMENT_NO_SIMPLE_TYPE));
    }

    @Test
    public void basic_t007c() {
        initTestCaseDirs(SUITE_NAME, "t007c");
        transformXd2Schema(
                "t007",
                Collections.singletonList("t007"),
                null,
                true,
                null,
                false,
                EnumSet.of(Xd2XsdFeature.XSD_ELEMENT_NO_SIMPLE_TYPE));
    }

    @Test
    public void basic_t009() {
        initTestCaseDirs(SUITE_NAME, "t009");
        transformXd2Schema("t009", Collections.singletonList("t009"), null);
    }

    @Test
    public void basic_t010() {
        initTestCaseDirs(SUITE_NAME, "t010");
        transformXd2Schema("t010", Collections.singletonList("t010"), null);
    }

    @Test
    public void basic_t016() {
        initTestCaseDirs(SUITE_NAME, "t016");
        transformXd2Schema("t016", Collections.singletonList("t016"), Collections.singletonList("t016e"));
    }

    @Test
    public void basic_t019() {
        initTestCaseDirs(SUITE_NAME, "t019");
        transformXd2Schema("t019", Collections.singletonList("t019"), null);
    }

    @Test
    public void basic_t020() {
        initTestCaseDirs(SUITE_NAME, "t020");
        transformXd2Schema("t020", Collections.singletonList("t020"), null);
    }

    @Test
    public void basic_longRestriction() {
        initTestCaseDirs(SUITE_NAME, "longRestriction");
        transformXd2Schema("longRestriction", null, null);
    }

    @Test
    public void basic_defaultValue_1() {
        initTestCaseDirs(SUITE_NAME, "defaultValue1");
        transformXd2SchemaNoRef("defaultValue1", Collections.singletonList("defaultValue1_valid_1"), null);
    }

    @Test
    public void basic_defaultValue_2() {
        initTestCaseDirs(SUITE_NAME, "defaultValue2");
        transformXd2SchemaNoRef("defaultValue2", Collections.singletonList("defaultValue2_valid_1"), null);
    }

}
