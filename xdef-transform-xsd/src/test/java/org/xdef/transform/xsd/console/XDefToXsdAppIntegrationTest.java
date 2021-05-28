package org.xdef.transform.xsd.console;

import com.ginsberg.junit.exit.ExpectSystemExitWithStatus;
import org.junit.jupiter.api.Test;
import test.resource.TestResourceUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class XDefToXsdAppIntegrationTest {

    @Test
    @ExpectSystemExitWithStatus(1)
    public void help() {
        XDefToXsdApp.main();
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

        XDefToXsdApp.main(new String[]{
                "-i", TestResourceUtil.getDirResourceAbsolutePath("xd2schema/input/xdef-basic/t000"),
                "-o", outputDir.toAbsolutePath().toString()
        });
    }

}