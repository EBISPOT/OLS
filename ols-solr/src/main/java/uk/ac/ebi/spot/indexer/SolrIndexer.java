package uk.ac.ebi.spot.indexer;

import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import uk.ac.ebi.spot.loader.OntologyLoader;
import uk.ac.ebi.spot.neo4j.model.TermDocument;
import uk.ac.ebi.spot.neo4j.model.TermDocumentBuilder;
import uk.ac.ebi.spot.util.TermType;
import uk.ac.ebi.spot.ols.model.OntologyIndexer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Simon Jupp
 * @date 30/01/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Component
public class SolrIndexer implements OntologyIndexer {

    private Logger log = LoggerFactory.getLogger(getClass());

    public Logger getLog() {
        return log;
    }

    public OntologySolrRepository getOntologySolrRepository() {
        return ontologySolrRepository;
    }

    @Autowired
    OntologySolrRepository ontologySolrRepository;

    @Override
    public void createIndex(Collection<OntologyLoader> loaders) {

        getLog().info("Creating index for " + loaders.size() + " loaders");

        for (OntologyLoader loader : loaders) {

            getLog().info("Creating index for " + loader.getOntologyName());

            Collection<TermDocument> documents = new HashSet<TermDocument>();


            for (IRI classTerm : loader.getAllClasses()) {

                TermDocumentBuilder builder = extractFeatures(loader, classTerm);
                builder.setType(TermType.CLASS.toString().toLowerCase());
                documents.add(builder.createTermDocument());
            }

            for (IRI classTerm : loader.getAllObjectPropertyIRIs()) {
                TermDocumentBuilder builder = extractFeatures(loader, classTerm);
                builder.setType(TermType.PROPERTY.toString().toLowerCase());
                documents.add(builder.createTermDocument());
            }

            for (IRI classTerm : loader.getAllAnnotationPropertyIRIs()) {
                TermDocumentBuilder builder = extractFeatures(loader, classTerm);
                builder.setType(TermType.PROPERTY.toString().toLowerCase());
                documents.add(builder.createTermDocument());
            }

            for (IRI classTerm : loader.getAllIndividualIRIs()) {
                TermDocumentBuilder builder = extractFeatures(loader, classTerm);
                builder.setType(TermType.INDIVIDUAL.toString().toLowerCase());
                documents.add(builder.createTermDocument());
            }

            getLog().info("Number of classes to index: " + loader.getAllClasses().size());
            getLog().info("Number of object properties to index: " + loader.getAllObjectPropertyIRIs().size());
            getLog().info("Number of annotation properties to index: " + loader.getAllAnnotationPropertyIRIs().size());
            getLog().info("Number of individuals to index: " + loader.getAllIndividualIRIs().size());

            getLog().info("Preparing to save documents...");
            ontologySolrRepository.save(documents);
            getLog().info("Indexing " +loader.getOntologyName()+ " complete!");

        }



    }

    @Override
    public void createIndex(OntologyLoader loader) {
        createIndex(Collections.singleton(loader));
    }

    @Override
    public void dropIndex(OntologyLoader loader) {
        TermDocument documents = ontologySolrRepository.findByOntologyName(loader.getOntologyName());
        if (documents != null) {
            ontologySolrRepository.delete(documents);
        }
    }

    private TermDocumentBuilder extractFeatures(OntologyLoader loader, IRI termIRI) {

        TermDocumentBuilder builder = new TermDocumentBuilder();

        builder.setOntologyName(loader.getOntologyName())
                .setOntologyUri(loader.getOntologyIRI().toString())
                .setId(generateId(loader.getOntologyName(), termIRI.toString()))
                .setUri(termIRI.toString())
                .setUri_key(generateAnnotationId(loader.getOntologyName() + termIRI.toString()).hashCode())
                .setIsDefiningOntology(loader.isLocalTerm(termIRI))
                .setIsObsolete(loader.isObsoleteTerm(termIRI))
                .setShortForm(loader.getAccessions(termIRI))
                .setHasChildren(loader.getDirectChildTerms().containsKey(termIRI))
                .setSubsets(new ArrayList<>(loader.getSubsets(termIRI)))
                .setLabel(loader.getTermLabels().get(termIRI));

        // index all annotations
        if (!loader.getAnnotations(termIRI).isEmpty()) {
            Map<String, Collection<String>> relatedTerms = new HashMap<>();

            for (IRI relation : loader.getAnnotations(termIRI).keySet()) {
                String labelName = loader.getTermLabels().get(relation) + "_annotation";
                if (!relatedTerms.containsKey(labelName)) {
                    relatedTerms.put(labelName, new HashSet<>());
                }
                relatedTerms.get(labelName).addAll(
                        loader.getAnnotations(termIRI).get(relation));

            }
            builder.setAnnotation(relatedTerms);
        }

        if (loader.getTermSynonyms().containsKey(termIRI)) {
            builder.setSynonyms(loader.getTermSynonyms().get(termIRI));
        }

        if (loader.getTermDefinitions().containsKey(termIRI)) {
            builder.setDescription(loader.getTermDefinitions().get(termIRI));
        }

        if (loader.getDirectParentTerms().containsKey(termIRI)) {
            builder.setParentUris(loader.getDirectParentTerms().get(termIRI).stream().map(IRI::toString).collect(Collectors.toSet()));
        }
        else {
            getLog().debug("Setting root " + termIRI);
            builder.setIsRoot(true);
        }


        if (loader.getAllParentTerms().containsKey(termIRI)) {
            builder.setAncestorUris(loader.getAllParentTerms().get(termIRI).stream().map(IRI::toString).collect(Collectors.toSet()));
        }

        if (loader.getDirectChildTerms().containsKey(termIRI)) {
            builder.setChildUris(loader.getDirectChildTerms().get(termIRI).stream().map(IRI::toString).collect(Collectors.toSet()));
        }

        if (loader.getAllChildTerms().containsKey(termIRI)) {
            builder.setDescendantUris(loader.getAllChildTerms().get(termIRI).stream().map(IRI::toString).collect(Collectors.toSet()));
        }

        if (!loader.getRelatedTerms(termIRI).isEmpty())    {
            Map<String, Collection<String>> relatedTerms = new HashMap<>();

            for (IRI relation : loader.getRelatedTerms(termIRI).keySet()) {
                String labelName = loader.getTermLabels().get(relation) + "_related";
                if (!relatedTerms.containsKey(labelName)) {
                    relatedTerms.put(labelName, new HashSet<>());
                }
                relatedTerms.get(labelName).addAll(
                        loader.getRelatedTerms(termIRI).get(relation).stream().map(IRI::toString).collect(Collectors.toSet()));

            }
            builder.setRelatedTerms(relatedTerms);
        }

        if (loader.getEquivalentTerms().containsKey(termIRI))    {
            builder.setEquivalentUris(loader.getEquivalentTerms().get(termIRI).stream().map(IRI::toString).collect(Collectors.toSet()));
        }

        Collection<String> logicalDescriptions = new HashSet<>();
        if (loader.getLogicalSuperClassDescriptions().containsKey(termIRI)) {
            logicalDescriptions.addAll(loader.getLogicalSuperClassDescriptions().get(termIRI));
        }
        if (loader.getLogicalEquivalentClassDescriptions().containsKey(termIRI)) {
            logicalDescriptions.addAll(loader.getLogicalEquivalentClassDescriptions().get(termIRI));
        }
        if (!logicalDescriptions.isEmpty()) {
            builder.setLogicalDescription(logicalDescriptions);
        }

        return builder;
    }

    private String generateAnnotationId(String uri) {
    	return DigestUtils.md5DigestAsHex(uri.getBytes());
    }

    private String generateId(String ontologyName, String iri) {
        return ontologyName.toLowerCase() + ":" + iri;
    }
}
