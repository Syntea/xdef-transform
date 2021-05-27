package org.xdef.transform.xsd.console;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdef.transform.xsd.console.impl.DefaultCommandLineProcessor;
import org.xdef.transform.xsd.console.impl.DefaultXDefAdapter;
import org.xdef.transform.xsd.console.impl.XDefAdapterConfig;
import org.xdef.transform.xsd.console.impl.XDefAdapterConfigFactory;
import org.xdef.transform.xsd.console.impl.XDefToXsdOptions;

public class XDefToXsdApp {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultCommandLineProcessor.class);

    public static void main(String... args) {
        Options cmdOptions = null;

        try {
            cmdOptions = XDefToXsdOptions.cli();

            final DefaultCommandLineProcessor defaultCommandLineProcessor = new DefaultCommandLineProcessor();
            final CommandLine commandLine = defaultCommandLineProcessor.parse(cmdOptions, args);
            defaultCommandLineProcessor.validatePaths(commandLine);

            final XDefAdapterConfig xdefAdapterConfig = new XDefAdapterConfigFactory().create(commandLine);

            final DefaultXDefAdapter xDefAdapter = new DefaultXDefAdapter(xdefAdapterConfig);
            xDefAdapter.transform();
        } catch (MissingOptionException ex) {
            printHelp(cmdOptions);
        } catch (Exception ex) {
            LOG.error("Exception occurs.", ex);
            printHelp(cmdOptions);
        }
    }

    private static void printHelp(final Options cmdOptions) {
        if (cmdOptions == null) {
            LOG.error("Application command line options are not initialized.");
        } else {
            new HelpFormatter().printHelp("X-definition to XML Schema conversion", cmdOptions);
        }

        System.exit(1);
    }

}
