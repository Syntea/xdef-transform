package test.xdutils;

import org.xdef.sys.ReportWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public abstract class TesterXdSchema extends XDTester {

    static protected boolean PRINT_OUTPUT_TO_CONSOLE = false;
    static protected boolean WRITE_OUTPUT_INTO_FILE = true;
    static protected boolean VALIDATE_XML_AGAINST_REF_FILE = true;
    static protected boolean PRINT_XML_VALIDATION_ERRORS = true;

    protected File _inputFilesRoot;
    protected File _refFilesRoot;
    protected File _dataFilesRoot;
    protected File _outputFilesRoot;
    protected ReportWriter _repWriter;

    protected File initFolder(final File dataDir, final String folderPath) {
        File folder = new File(dataDir.getAbsolutePath(), folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            throw new RuntimeException("Directory " + folderPath + " does not exists!");
        }

        return folder;
    }

    protected File getFile(final String path, final String fileName, final String fileExt) throws FileNotFoundException {
        File xdFile = new File(path, fileName + fileExt);
        if (xdFile == null || !xdFile.exists() || !xdFile.isFile()) {
            throw new FileNotFoundException("Path: " + path + "\\" + fileName + fileExt);
        }

        return xdFile;
    }

    protected static Reader createFileReader(final String file) throws FileNotFoundException {
        return new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
    }

    protected Reader createFileReader(final String filePath, final String fileName, final String fileExt) throws FileNotFoundException {
        return createFileReader(filePath + "\\" + fileName + fileExt);
    }

    protected Reader createInputFileReader(final String fileName, final String fileExt) throws FileNotFoundException {
        return createFileReader(_inputFilesRoot.getAbsolutePath() + "\\" + fileName, fileName, fileExt);
    }

    protected Reader createRefFileReader(final String fileName, final String fileExt) throws FileNotFoundException {
        return createFileReader(_refFilesRoot.getAbsolutePath() + "\\" + fileName, fileName, fileExt);
    }

    protected Reader createOutputFileReader(final String fileName, final String fileExt) throws FileNotFoundException {
        return createFileReader(_outputFilesRoot.getAbsolutePath(), fileName, fileExt);
    }

    protected File getXmlDataFile(final String testCase, final String fileName) throws FileNotFoundException {
        return getFile(_dataFilesRoot.getAbsolutePath() + "\\" + testCase + "\\data", fileName, ".xml");
    }

    protected Writer createFileWriter(String file) throws FileNotFoundException {
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
    }
}
