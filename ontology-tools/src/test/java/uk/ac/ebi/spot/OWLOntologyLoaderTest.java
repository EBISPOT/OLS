package uk.ac.ebi.spot;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import uk.ac.ebi.spot.config.OntologyResourceConfig;
import uk.ac.ebi.spot.exception.OntologyLoadingException;
import uk.ac.ebi.spot.loader.HermitOWLOntologyLoader;
import uk.ac.ebi.spot.loader.OntologyLoader;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * @author Simon Jupp
 * @date 06/02/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public class OWLOntologyLoaderTest {

    public static void main(String[] args) {

        OntologyResourceConfig.OntologyResourceConfigBuilder builder =
                new OntologyResourceConfig.OntologyResourceConfigBuilder(
                        "http://www.ebi.ac.uk/ols/test1.owl",
                        "Test 1",
                        "TEST1",
                        URI.create("file:/Users/jupp/Dropbox/dev/ols/ontology-tools/src/test/resources/test1.owl")

                );

        builder.setDefinitionProperties(Collections.singleton(URI.create("http://www.ebi.ac.uk/ols/test1.owl/TEST_0000012")));
        builder.setSynonymProperties(Collections.singleton(URI.create("http://www.ebi.ac.uk/ols/test1.owl/TEST_0000011")));
        builder.setHiddenProperties(Collections.singleton(URI.create("http://www.ebi.ac.uk/ols/test1.owl/TEST_0000013")));
        builder.setBaseUris(Collections.singleton("http://www.ebi.ac.uk/ols/test1.owl/TEST_"));

        OntologyResourceConfig config= builder.build();

        OntologyLoader loader = null;
        try {
            loader = new HermitOWLOntologyLoader(config);
        } catch (OntologyLoadingException e) {
            e.printStackTrace();
            System.exit(0);
        }
        Collection<IRI> terms = loader.getAllClasses();

        terms.addAll(loader.getAllObjectPropertyIRIs());
        for (IRI iri: terms) {
            System.out.println(iri + " -> label: " + loader.getTermLabels().get(iri));
            for (String acc : loader.getAccessions(iri)) {
                System.out.println(iri + " -> accession: " + acc);
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

}
