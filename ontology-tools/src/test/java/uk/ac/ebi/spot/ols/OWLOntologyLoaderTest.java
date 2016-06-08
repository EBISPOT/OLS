package uk.ac.ebi.spot.ols;

import org.semanticweb.owlapi.model.IRI;
import uk.ac.ebi.spot.ols.loader.*;
import uk.ac.ebi.spot.ols.config.OntologyResourceConfig;
import uk.ac.ebi.spot.ols.exception.OntologyLoadingException;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Simon Jupp
 * @date 06/02/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public class OWLOntologyLoaderTest {

    public static void main(String[] args) {

        OntologyResourceConfig.OntologyResourceConfigBuilder builder =
                new OntologyResourceConfig.OntologyResourceConfigBuilder(
                        "http://www.bio.ntnu.no/ontology/ReXO/rexo.owl",
                        "rexo",
                        "REXO",
                        URI.create("http://www.bio.ntnu.no/ontology/ReXO/rexo.owl")

                );

        builder.setDefinitionProperties(Collections.singleton(URI.create("http://www.ebi.ac.uk/ols/test1.owl/TEST_0000012")));
        builder.setSynonymProperties(Collections.singleton(URI.create("http://www.ebi.ac.uk/ols/test1.owl/TEST_0000011")));
        builder.setHiddenProperties(Collections.singleton(URI.create("http://www.ebi.ac.uk/ols/test1.owl/TEST_0000013")));
        builder.setBaseUris(Collections.singleton("http://www.ebi.ac.uk/ols/test1.owl/TEST_"));

        OntologyResourceConfig config= builder.build();

        OntologyLoader loader = null;
        try {
            loader = new StructuralOWLOntologyLoader(config);
        } catch (OntologyLoadingException e) {
            e.printStackTrace();
            System.exit(0);
        }
        Collection<IRI> terms = loader.getAllClasses();

        terms.addAll(loader.getAllObjectPropertyIRIs());
        for (IRI iri: terms) {
            System.out.println(iri + " -> label: " + loader.getTermLabels().get(iri));
            String acc = loader.getShortForm(iri);
            System.out.println(iri + " -> accession: " + acc);

            String oboId = loader.getOboId(iri);
            if (oboId != null) {
                System.out.println(iri + " -> oboId: " + oboId);
            }


            if (loader.getTermSynonyms().containsKey(iri)) {
                for (String syns : loader.getTermSynonyms().get(iri)) {
                                System.out.println(iri + " -> synonyms: " + syns);
                            }
            }
            if (loader.getTermDefinitions().containsKey(iri)) {
                for (String syns : loader.getTermDefinitions().get(iri)) {
                                System.out.println(iri + " -> definition: " + syns);
                            }
            }

            if (!loader.getRelatedTerms(iri).isEmpty())    {
                for (IRI propertyIri : loader.getRelatedTerms(iri).keySet()  ) {
                                for (IRI relatedTerm : loader.getRelatedTerms(iri).get(propertyIri)) {
                                    System.out.println(iri + " -> " + loader.getTermLabels().get(propertyIri) + " : " + loader.getTermLabels().get(relatedTerm));
                                }

            }


            }

        }
    }

    public static void testOboVersionIriDate ( ) {

        String versionsIri = "http://purl.obolibrary.org/obo/obi/2009-11-06/obi.owl";

        String version = AbstractOWLOntologyLoader.parseOboVersion(IRI.create(versionsIri));

        System.out.println(version);


    }

}
