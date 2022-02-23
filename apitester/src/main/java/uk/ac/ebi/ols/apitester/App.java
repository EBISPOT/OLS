
package uk.ac.ebi.ols.apitester;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class App 
{
    public static void main( String[] args )
    {
	Options options = new Options();

        Option input = new Option(null, "instance1", true, "First instance URL e.g. https://www.ebi.ac.uk/ols");
        input.setRequired(true);
        options.addOption(input);

        Option output = new Option(null, "instance2", true, "Second instance URL e.g. https://www.ebi.ac.uk/ols");
        output.setRequired(true);
        options.addOption(output);

        Option ontId = new Option(null, "ontologyId", true, "ID of the ontology to check e.g. efo");
        ontId.setRequired(true);
        options.addOption(ontId);

        Option sampleSize = new Option(null, "sampleSize", true, "Number of terms to request");
        sampleSize.setRequired(false);
        options.addOption(sampleSize);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);

            System.exit(1);
        }

        String url1 = cmd.getOptionValue("instance1");
        String url2 = cmd.getOptionValue("instance2");
	String ontology = cmd.getOptionValue("ontologyId");
	String size = cmd.getOptionValue("sampleSize");

	System.exit(
		(new OlsApiTester(url1, url2, ontology, size != null ? Integer.parseInt(size) : 5)).test() ? 0 : 1
	);
    }
}
