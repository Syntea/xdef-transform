package org.xdef.transform.xsd.console;

import com.ginsberg.junit.exit.ExpectSystemExitWithStatus;
import org.junit.jupiter.api.Test;
import test.resource.TestResourceUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class XDefToSchemaAppIntegrationTest {

    @Test
    @ExpectSystemExitWithStatus(1)
    public void help() {
        XDefToSchemaApp.main();
    }

    @Test
    public void basicIT_Ok() throws IOException {
        Path outputDir = TestResourceUtil.getResourcePath("xd2schema/output/it/t000");
        if (!Files.isDirectory(outputDir) && Files.exists(outputDir)) {
            Files.deleteIfExists(outputDir);
        }

        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        XDefToSchemaApp.main(new String[]{
                "-i", TestResourceUtil.getDirResourceAbsolutePath("xd2schema/input/xdef-basic/t000"),
                "-o", outputDir.toAbsolutePath().toString()
        });
    }

    @Test
    public void basicIT_WithFeatures_Ok() throws IOException {
        Path outputDir = TestResourceUtil.getResourcePath("xd2schema/output/it/t000");
        if (!Files.isDirectory(outputDir) && Files.exists(outputDir)) {
            Files.deleteIfExists(outputDir);
        }

        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        XDefToSchemaApp.main(new String[]{
                "-i", TestResourceUtil.getDirResourceAbsolutePath("xd2schema/input/xdef-basic/t000"),
                "-o", outputDir.toAbsolutePath().toString(),
                "-f", "a ds"
        });
    }

}