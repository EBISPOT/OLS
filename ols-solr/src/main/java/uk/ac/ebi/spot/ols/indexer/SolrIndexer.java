package uk.ac.ebi.spot.ols.indexer;

import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import uk.ac.ebi.spot.ols.loader.OntologyLoader;
import uk.ac.ebi.spot.ols.model.SuggestDocument;
import uk.ac.ebi.spot.ols.model.TermDocument;
import uk.ac.ebi.spot.ols.model.TermDocumentBuilder;
import uk.ac.ebi.spot.ols.util.TermType;
import uk.ac.ebi.spot.ols.model.OntologyIndexer;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author Simon Jupp
 * @date 30/01/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Component
public class SolrIndexer implements OntologyIndexer {

    private Logger log = LoggerFactory.getLogger(getClass());
    private int batchSize = 1000;

    public Logger getLog() {
        return log;
    }

    public OntologySolrRepository getOntologySolrRepository() {
        return ontologySolrRepository;
    }

    @Autowired
    OntologySolrRepository ontologySolrRepository;

    @Autowired
    OntologySuggestRepository ontologySuggestRepository;

    @Override
    public void createIndex(Collection<OntologyLoader> loaders) {

        getLog().info("Creating index for " + loaders.size() + " loaders");


        for (OntologyLoader loader : loaders) {


            getLog().info("Creating new index for " + loader.getOntologyName());
            long startTime = System.currentTimeMillis();

            getLog().info("Number of classes to index: " + loader.getAllClasses().size());
            getLog().info("Number of object properties to index: " + loader.getAllObjectPropertyIRIs().size());
            getLog().info("Number of annotation properties to index: " + loader.getAllAnnotationPropertyIRIs().size());
            getLog().info("Number of individuals to index: " + loader.getAllIndividualIRIs().size());

            List<TermDocument> documents = new ArrayList<TermDocument>();
            List<SuggestDocument> suggestDocuments = new ArrayList<>();

            for (IRI classTerm : loader.getAllClasses()) {

                TermDocumentBuilder builder = extractFeatures(loader, classTerm);
                builder.setType(TermType.CLASS.toString().toLowerCase());
                documents.add(builder.createTermDocument());

                if (documents.size() == 10000) {
                    getLog().debug("Max reached - indexing terms");
                    index(documents);
                    documents = new ArrayList<>();
                }

                // get labels and synonyms for suggest index
                suggestDocuments.add(new SuggestDocument(loader.getTermLabels().get(classTerm), loader.getOntologyName()));
                if (loader.getTermSynonyms().containsKey(classTerm)) {
                    for (String syn : loader.getTermSynonyms().get(classTerm)) {
                        suggestDocuments.add(new SuggestDocument(syn, loader.getOntologyName()));
                    }
                }
                if (suggestDocuments.size() > 10000) {
                    indexSuggest(suggestDocuments);
                    suggestDocuments = new ArrayList<>();
                }

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

                if (documents.size() == 10000) {
                    getLog().debug("Max reached - indexing terms");
                    index(documents);
                    documents = new ArrayList<>();
                }
            }

            // index ontology meta data
            TermDocumentBuilder builder = extractOntologyFeature(loader);
            documents.add(builder.createTermDocument());


            long endTime = System.currentTimeMillis();
            long duration = (endTime - startTime) / 1000; // time in seconds
            index(documents);
            indexSuggest(suggestDocuments);
            getLog().info("Solr index for " + loader.getOntologyName() + " completed in " + duration + " seconds");

        }



    }

    private void indexSuggest(List<SuggestDocument> suggestDocuments) {
        // save suggest index

        int numDocuments = suggestDocuments.size();
        getLog().debug("Extracted {} documents", numDocuments);

        // Index documents in batches
        int count = 0;
        while (count < numDocuments) {
            int end = count + getBatchSize();
            if (end > numDocuments) {
                end = numDocuments;
            }

            ontologySuggestRepository.save(suggestDocuments.subList(count, end));

            count = end;
            getLog().debug("Indexed {} / {} entries", count, numDocuments);
        }
    }

    private void index (List<TermDocument> documents) {
        int numDocuments = documents.size();
        getLog().debug("Extracted {} documents", numDocuments);

        // Index documents in batches
        int count = 0;
        while (count < numDocuments) {
            int end = count + getBatchSize();
            if (end > numDocuments) {
                end = numDocuments;
            }

            ontologySolrRepository.save(documents.subList(count, end));

            count = end;
            getLog().debug("Indexed {} / {} entries", count, numDocuments);
        }
    }

    @Override
    public void createIndex(OntologyLoader loader) {
        createIndex(Collections.singleton(loader));
    }

    @Override
    public void dropIndex(OntologyLoader loader) {
        Iterable<TermDocument> documents = ontologySolrRepository.findByOntologyName(loader.getOntologyName());

        if (documents.iterator().hasNext()) {
            getLog().info("Deleting solr index for " + loader.getOntologyName());
            long startTime = System.currentTimeMillis();
            ontologySolrRepository.delete(documents);
            long endTime = System.currentTimeMillis();
            long duration = (endTime - startTime) / 1000; // time in seconds
            getLog().info(loader.getOntologyName() + " removed from solr in " + duration + " seconds");
        }
    }

    private TermDocumentBuilder extractOntologyFeature (OntologyLoader loader) {

        TermDocumentBuilder builder = new TermDocumentBuilder();
        builder.setOntologyName(loader.getOntologyName())
                .setOntologyPrefix(loader.getPreferredPrefix())
                .setOntologyUri(loader.getOntologyIRI().toString())
                .setId(generateId(loader.getOntologyName(), ""))
                .setUri(loader.getOntologyIRI().toString())
                .setUri_key(generateAnnotationId(loader.getOntologyName()).hashCode())
                .setLabel(loader.getTitle())
                .setDescription(Collections.singleton(loader.getOntologyDescription()))
                .setShortForm(loader.getOntologyName())
                .setType("ontology");

        return builder;

    }

    private TermDocumentBuilder extractFeatures(OntologyLoader loader, IRI termIRI) {

        TermDocumentBuilder builder = new TermDocumentBuilder();

        builder.setOntologyName(loader.getOntologyName())
                .setOntologyPrefix(loader.getPreferredPrefix())
                .setOntologyUri(loader.getOntologyIRI().toString())
                .setId(generateId(loader.getOntologyName(), termIRI.toString()))
                .setUri(termIRI.toString())
                .setUri_key(generateAnnotationId(loader.getOntologyName() + termIRI.toString()).hashCode())
                .setIsDefiningOntology(loader.isLocalTerm(termIRI))
                .setIsObsolete(loader.isObsoleteTerm(termIRI))
                .setShortForm(loader.getShortForm(termIRI))
                .setOboId(loader.getOboId(termIRI))
                .setHasChildren(loader.getDirectChildTerms().containsKey(termIRI))
                .setSubsets(new ArrayList<>(loader.getSubsets(termIRI)));

        if (!loader.getTermLabels().containsKey(termIRI)) {
            builder.setLabel(loader.getShortForm(termIRI));
        }
        else  {
            builder.setLabel(loader.getTermLabels().get(termIRI));
        }


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

        Collection<String> directParentTerms = new HashSet<>();
        if (loader.getDirectParentTerms().containsKey(termIRI)) {
            directParentTerms = loader.getDirectParentTerms().get(termIRI).stream().map(IRI::toString).collect(Collectors.toSet());
            builder.setParentUris(directParentTerms);
        }
        else {
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

        // set hierarchical parents and children
        Collection<String> directHierarchicalParents = loader.getRelatedParentTerms(termIRI).values().stream().flatMap(Collection::stream).map(IRI::toString).collect(Collectors.toSet());
        // add direct superclasses
        directHierarchicalParents.addAll(directParentTerms);

        if (!directHierarchicalParents.isEmpty()) {
            builder.setHierarchicalParentUris(directHierarchicalParents);
        }

        // get all transitive hierarchical parents
        Collection<String> allHierarchicalParents = loader.getAllRelatedParentTerms(termIRI).stream().map(IRI::toString).collect(Collectors.toSet());
        if (!allHierarchicalParents.isEmpty()) {
            builder.setHierarchicalAncestorUris(allHierarchicalParents);
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

    public int getBatchSize() {
        return batchSize;
    }
}
