package org.xdef.transform.xsd.console.impl;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdef.transform.xsd.console.CommandLineProcessor;
import org.xdef.transform.xsd.console.XDefToXsdOptionsConst;
import org.xdef.transform.xsd.error.FormattedRuntimeException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author smid
 * @since 2021-05-13
 */
public class DefaultCommandLineProcessor implements CommandLineProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultCommandLineProcessor.class);

    public void validatePaths(final CommandLine cmd) throws RuntimeException {
        LOG.info("Validating command line specified paths ...");

        Path path = Paths.get(cmd.getOptionValue(XDefToXsdOptionsConst.INPUT_DIR));

        if (!Files.isDirectory(path)) {
            throw new FormattedRuntimeException("Input directory '{}' does not exist!", path);
        }

        LOG.debug("Input directory: '{}'", path.toAbsolutePath());

        path = Paths.get(cmd.getOptionValue(XDefToXsdOptionsConst.OUTPUT_DIR));
        if (!Files.isDirectory(path)) {
            throw new FormattedRuntimeException("Output directory '{}' does not exist!", path);
        }

        LOG.debug("Output directory: '{}'", path.toAbsolutePath());

        if (cmd.hasOption(XDefToXsdOptionsConst.VALIDATE_POSITIVE_CASE)) {
            for (String positiveCaseDataFile : cmd.getOptionValues(XDefToXsdOptionsConst.VALIDATE_POSITIVE_CASE)) {
                path = Paths.get(positiveCaseDataFile);
                if (!Files.isRegularFile(path)) {
                    throw new FormattedRuntimeException("Input testing (positive) data file '{}' does not exist!", path);
                }
            }
        }

        if (cmd.hasOption(XDefToXsdOptionsConst.VALIDATE_NEGATIVE_CASE)) {
            for (String negativeCaseDataFile : cmd.getOptionValues(XDefToXsdOptionsConst.VALIDATE_NEGATIVE_CASE)) {
                path = Paths.get(negativeCaseDataFile);
                if (!Files.isRegularFile(path)) {
                    throw new FormattedRuntimeException("Input testing (negative) data file '{}' does not exist!", path);
                }
            }
        }

        LOG.info("Command line specified paths have been successfully validated.");
    }

    public CommandLine parse(final Options options, String... args) throws MissingOptionException, RuntimeException {
        LOG.info("Parsing command line ...");

        final CommandLineParser parser = new DefaultParser();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (MissingOptionException ex) {
            throw ex;
        } catch (ParseException ex) {
            throw new RuntimeException("Error occurs while parsing input command line arguments.", ex);
        }

        LOG.info("Command line has been successfully parsed.");
        return cmd;
    }

}
