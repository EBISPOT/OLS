package uk.ac.ebi.spot.ols;

import org.semanticweb.owlapi.model.IRI;
import uk.ac.ebi.spot.ols.config.OntologyResourceConfig;
import uk.ac.ebi.spot.ols.exception.OntologyLoadingException;
import uk.ac.ebi.spot.ols.loader.HermitOWLOntologyLoader;
import uk.ac.ebi.spot.ols.loader.OntologyLoader;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Simon Jupp
 * @date 06/02/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public class OWLOntologyLoaderEFOTest {

    public static void main(String[] args) {

        System.setProperty("entityExpansionLimit", "10000000");
        OntologyResourceConfig.OntologyResourceConfigBuilder builder =
                new OntologyResourceConfig.OntologyResourceConfigBuilder(
                        "http://www.ebi.ac.uk/efo",
                        "Experimental Factor Ontology",
                        "EFO",
                        URI.create("http://www.ebi.ac.uk/efo/efo.owl")

                );

        builder.setDefinitionProperties(Collections.singleton(URI.create("http://www.ebi.ac.uk/efo/definition")));
        builder.setSynonymProperties(Collections.singleton(URI.create("http://www.ebi.ac.uk/efo/alternative_term")));
        builder.setHiddenProperties(Collections.singleton(URI.create("http://www.ebi.ac.uk/efo/has_flag")));
        builder.setBaseUris(Collections.singleton("http://www.ebi.ac.uk/ebi/EFO_"));

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

            System.out.println(iri + " -> accession: " + loader.getShortForm(iri));
            System.out.println(iri + " -> oboid: " + loader.getOboId(iri));

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

            if (!loader.getAnnotations(iri).isEmpty())    {
                for (IRI propertyIri : loader.getAnnotations(iri).keySet()  ) {
                    for (String relatedTerm : loader.getAnnotations(iri).get(propertyIri)) {
                        System.out.println(iri + " -> " + loader.getTermLabels().get(propertyIri) + " : " + relatedTerm);
                    }

                }
            }

            if (!loader.getRelatedTerms(iri).isEmpty())    {
                for (IRI propertyIri : loader.getRelatedTerms(iri).keySet()  ) {
                    for (IRI relatedTerm : loader.getRelatedTerms(iri).get(propertyIri)) {
                        System.out.println(iri + " -> " + loader.getTermLabels().get(propertyIri) + " : " + loader.getTermLabels().get(relatedTerm));
                    }

                }
            }

            if (loader.getLogicalSuperClassDescriptions().containsKey(iri)) {
                for (String desc : loader.getLogicalSuperClassDescriptions().get(iri)) {
                    System.out.println(iri + " -> " + desc);
                }
            }


        }
    }

}
