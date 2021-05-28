package test.xd2schema.suite;

import org.junit.jupiter.api.Test;
import test.xd2schema.AbstractXd2SchemaTransformSuite;

import java.util.Collections;

/**
 * @author smid
 * @since 2021-05-28
 */
public class ImportXd2SchemaTest extends AbstractXd2SchemaTransformSuite {

    public static final String SUITE_NAME = "import";

    @Test
    public void importLocal_1() {
        initCaseDirs(SUITE_NAME, "importLocal01");
        convertXd2SchemaNoRef("importLocal01", Collections.singletonList("importLocal01_valid01"), null);
    }

    @Test
    public void importLocal_2() {
        initCaseDirs(SUITE_NAME, "importLocal02_A");
        convertXd2SchemaNoRef("importLocal02_A", Collections.singletonList("importLocal02_A_valid01"), null);
    }

    @Test
    public void importLocal_3() {
        initCaseDirs(SUITE_NAME, "importLocal02_B");
        convertXd2SchemaNoRef("importLocal02_B", Collections.singletonList("importLocal02_B_valid01"), null);
    }

    @Test
    public void importLocal_4() {
        initCaseDirs(SUITE_NAME, "importLocal02_C");
        convertXd2SchemaNoRef("importLocal02_C", Collections.singletonList("importLocal02_C_valid01"), null);
    }

    @Test
    public void importLocal_5() {
        initCaseDirs(SUITE_NAME, "importLocal02_D");
        convertXd2SchemaNoRef("importLocal02_D", Collections.singletonList("importLocal02_D_valid01"), null);
    }

    @Test
    public void importLocal_6() {
        initCaseDirs(SUITE_NAME, "importLocal02_E");
        convertXd2SchemaNoRef("importLocal02_E", Collections.singletonList("importLocal02_E_valid01"), null);
    }

}
