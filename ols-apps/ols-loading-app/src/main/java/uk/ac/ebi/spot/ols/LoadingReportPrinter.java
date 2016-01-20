package uk.ac.ebi.spot.ols;

/**
 * @author Simon Jupp
 * @date 19/01/2016
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public class LoadingReportPrinter {

    public static String getMessage (LoadingReport loadingReport) {

        StringBuilder builder = new StringBuilder();
        builder.append("OLS loading complete with the following messages:\n\n");

        if (loadingReport.getUpdatedOntologies().isEmpty()) {
            builder.append("No ontologies were updated\n");
            builder.append("---------------------------\n");
        }
        else  {
            builder.append("The following ontologies were sucessfully updated\n");
            builder.append("-------------------------------------------------\n");

            for (String name: loadingReport.getUpdatedOntologies())  {
                builder.append(name);
                            builder.append("\n");
            }
        }

        builder.append("\n\n");

        if (!loadingReport.getFailingOntologies().isEmpty()) {
            builder.append("The following ontologies failed\n");
            builder.append("--------------------------------\n");

            for (String name : loadingReport.getFailingOntologies().keySet()) {
                builder.append(name);
                builder.append("\n");
                builder.append(loadingReport.getFailingOntologies().get(name));
                builder.append("\n");
                builder.append("\n");
            }
        }

        if (!loadingReport.getExpections().equals("")) {
            builder.append("Additional errors\n");
            builder.append("---------------------\n");
            builder.append(loadingReport.getExpections());
        }

        return builder.toString();
    }
}
