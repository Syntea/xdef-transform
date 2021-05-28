package test.xd2schema.suite;

import org.junit.jupiter.api.Test;
import test.xd2schema.AbstractXd2SchemaTransformSuite;

import java.util.Collections;

/**
 * @author smid
 * @since 2021-05-28
 */
public class XDefBasicXd2SchemaTest extends AbstractXd2SchemaTransformSuite {

    public static final String SUITE_NAME = "xdef-basic";
    
    @Test
    public void basic_t000() {
        initCaseDirs(SUITE_NAME, "t000");
        convertXd2Schema("t000", Collections.singletonList("t000"), null);
    }

    @Test
    public void basic_t001() {
        initCaseDirs(SUITE_NAME, "t001");
        convertXd2Schema("t001", Collections.singletonList("t001"), null);
    }

    @Test
    public void basic_t002() {
        initCaseDirs(SUITE_NAME, "t002");
        convertXd2Schema("t002", Collections.singletonList("t002"), null);
    }

    @Test
    public void basic_t003() {
        initCaseDirs(SUITE_NAME, "t003");
        convertXd2Schema("t003", Collections.singletonList("t003"), null);
    }

    @Test
    public void basic_t004() {
        initCaseDirs(SUITE_NAME, "t004");
        convertXd2Schema("t004", Collections.singletonList("t004"), null);
    }

    @Test
    public void basic_t005() {
        initCaseDirs(SUITE_NAME, "t005");
        convertXd2Schema("t005", Collections.singletonList("t005"), null);
    }

    @Test
    public void basic_t007() {
        initCaseDirs(SUITE_NAME, "t007");
        convertXd2Schema("t007", Collections.singletonList("t007"), null);
    }

    @Test
    public void basic_t009() {
        initCaseDirs(SUITE_NAME, "t009");
        convertXd2Schema("t009", Collections.singletonList("t009"), null);
    }

    @Test
    public void basic_t010() {
        initCaseDirs(SUITE_NAME, "t010");
        convertXd2Schema("t010", Collections.singletonList("t010"), null);
    }

    @Test
    public void basic_t016() {
        initCaseDirs(SUITE_NAME, "t016");
        convertXd2Schema("t016", Collections.singletonList("t016"), Collections.singletonList("t016e"));
    }

    @Test
    public void basic_t019() {
        initCaseDirs(SUITE_NAME, "t019");
        convertXd2Schema("t019", Collections.singletonList("t019"), null);
    }

    @Test
    public void basic_t020() {
        initCaseDirs(SUITE_NAME, "t020");
        convertXd2Schema("t020", Collections.singletonList("t020"), null);
    }

    @Test
    public void basic_longRestriction() {
        initCaseDirs(SUITE_NAME, "longRestriction");
        convertXd2Schema("longRestriction", null, null);
    }

    @Test
    public void basic_defaultValue_1() {
        initCaseDirs(SUITE_NAME, "defaultValue1");
        convertXd2SchemaNoRef("defaultValue1", Collections.singletonList("defaultValue1_valid_1"), null);
    }

    @Test
    public void basic_defaultValue_2() {
        initCaseDirs(SUITE_NAME, "defaultValue2");
        convertXd2SchemaNoRef("defaultValue2", Collections.singletonList("defaultValue2_valid_1"), null);
    }

}
