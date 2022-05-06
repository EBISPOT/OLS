package uk.ac.ebi.spot.ols.indexer;

import org.mockito.internal.debugging.Localized;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import uk.ac.ebi.spot.ols.exception.IndexingException;
import uk.ac.ebi.spot.ols.loader.OntologyLoader;
import uk.ac.ebi.spot.ols.model.SuggestDocument;
import uk.ac.ebi.spot.ols.model.TermDocument;
import uk.ac.ebi.spot.ols.model.TermDocumentBuilder;
import uk.ac.ebi.spot.ols.util.LocalizedStrings;
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
            getLog().info("Number of data properties to index: " + loader.getAllDataPropertyIRIs().size());
            getLog().info("Number of annotation properties to index: " + loader.getAllAnnotationPropertyIRIs().size());
            getLog().info("Number of individuals to index: " + loader.getAllIndividualIRIs().size());

            List<TermDocument> documents = new ArrayList<TermDocument>();
            List<SuggestDocument> suggestDocuments = new ArrayList<>();

            for (IRI classTerm : loader.getAllClasses()) {
                getLog().trace("solr indexing " + classTerm.toString());

                TermDocumentBuilder builder = extractTermFeatures(loader, classTerm);
                builder.setType(TermType.CLASS.toString().toLowerCase());
                builder.setId(generateId(loader.getOntologyName(), "class", classTerm.toString()));
                builder.setUri_key(generateAnnotationId(loader.getOntologyName() + classTerm.toString() + "class").hashCode());

                documents.addAll(builder.createTermDocuments());

                if (documents.size() == 10000) {
                    getLog().debug("Max reached - indexing terms");
                    index(documents);
                    documents = new ArrayList<>();
                }

                // get labels and synonyms for suggest index
                LocalizedStrings labels = loader.getTermLabels().get(classTerm);
                if(labels != null) {
                    for (String language : labels.getLanguages()) {
                        Collection<String> labelStrings = labels.getStrings(language);
                        for (String label : labelStrings) {
                            suggestDocuments.add(new SuggestDocument(label, loader.getOntologyName(), language));
                            if (suggestDocuments.size() > 10000) {
                                indexSuggest(suggestDocuments);
                                suggestDocuments = new ArrayList<>();
                            }
                        }
                    }
                }

                LocalizedStrings synonyms = loader.getTermSynonyms().get(classTerm);
                if(synonyms != null) {
                    for (String language : synonyms.getLanguages()) {
                        Collection<String> synonymStrings = synonyms.getStrings(language);
                        for (String synonym : synonymStrings) {
                            suggestDocuments.add(new SuggestDocument(synonym, loader.getOntologyName(), language));
                            if (suggestDocuments.size() > 10000) {
                                indexSuggest(suggestDocuments);
                                suggestDocuments = new ArrayList<>();
                            }
                        }
                    }
                }

            }

            for (IRI classTerm : loader.getAllObjectPropertyIRIs()) {
                TermDocumentBuilder builder = extractTermFeatures(loader, classTerm);
                builder.setType(TermType.PROPERTY.toString().toLowerCase());
                builder.setId(generateId(loader.getOntologyName(), "property", classTerm.toString()));
                builder.setUri_key(generateAnnotationId(loader.getOntologyName() + classTerm.toString() + "property").hashCode());

                documents.addAll(builder.createTermDocuments());
            }

            for (IRI classTerm : loader.getAllDataPropertyIRIs()) {
                TermDocumentBuilder builder = extractTermFeatures(loader, classTerm);
                builder.setType(TermType.PROPERTY.toString().toLowerCase());
                builder.setId(generateId(loader.getOntologyName(), "property", classTerm.toString()));
                builder.setUri_key(generateAnnotationId(loader.getOntologyName() + classTerm.toString() + "property").hashCode());

                documents.addAll(builder.createTermDocuments());
            }

            for (IRI classTerm : loader.getAllAnnotationPropertyIRIs()) {
                TermDocumentBuilder builder = extractTermFeatures(loader, classTerm);
                builder.setType(TermType.PROPERTY.toString().toLowerCase());
                builder.setId(generateId(loader.getOntologyName(), "property", classTerm.toString()));
                builder.setUri_key(generateAnnotationId(loader.getOntologyName() + classTerm.toString() + "property").hashCode());
                documents.addAll(builder.createTermDocuments());
            }

            for (IRI classTerm : loader.getAllIndividualIRIs()) {
                TermDocumentBuilder builder = extractTermFeatures(loader, classTerm);
                builder.setType(TermType.INDIVIDUAL.toString().toLowerCase());
                builder.setId(generateId(loader.getOntologyName(), "individual", classTerm.toString()));
                builder.setUri_key(generateAnnotationId(loader.getOntologyName() + classTerm.toString() + "individual").hashCode());
                documents.addAll(builder.createTermDocuments());


                if (documents.size() == 10000) {
                    getLog().debug("Max reached - indexing terms");
                    index(documents);
                    documents = new ArrayList<>();
                }
            }

            // index ontology meta data
            TermDocumentBuilder builder = extractOntologyFeatures(loader);
            documents.addAll(builder.createTermDocuments());


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
    public void dropIndex(OntologyLoader loader) throws IndexingException {
        dropIndex(loader.getOntologyName());
    }

    @Override
    public void dropIndex(String ontologyId) {
        Iterable<TermDocument> documents = ontologySolrRepository.findByOntologyName(ontologyId);

        if (documents.iterator().hasNext()) {
            getLog().info("Deleting solr index for " + ontologyId);
            long startTime = System.currentTimeMillis();
            ontologySolrRepository.delete(documents);
            long endTime = System.currentTimeMillis();
            long duration = (endTime - startTime) / 1000; // time in seconds
            getLog().info(ontologyId + " removed from solr in " + duration + " seconds");
        }
    }

    private TermDocumentBuilder extractOntologyFeatures(OntologyLoader loader) {

        TermDocumentBuilder builder = new TermDocumentBuilder();
        builder.setOntologyName(loader.getOntologyName())
                .setOntologyPrefix(loader.getPreferredPrefix())
                .setOntologyUri(loader.getOntologyIRI().toString())
                .setDescriptions(new LocalizedStrings(loader.getLocalizedDescriptions()))
                .setOntologyTitles(loader.getLocalizedTitles())
                .setLabels(new LocalizedStrings(loader.getLocalizedTitles()))
                .setId(generateOntologyId(loader.getOntologyName(), ""))
                .setUri(loader.getOntologyIRI().toString())
                .setUri_key(generateAnnotationId(loader.getOntologyName()).hashCode())
                .setShortForm(loader.getOntologyName())
                .setType("ontology");

        return builder;

    }

    private TermDocumentBuilder extractTermFeatures(OntologyLoader loader, IRI termIRI) {

        TermDocumentBuilder builder = new TermDocumentBuilder();

        builder.setOntologyName(loader.getOntologyName())
                .setOntologyPrefix(loader.getPreferredPrefix())
                .setOntologyUri(loader.getOntologyIRI().toString())
                .setOntologyTitles(loader.getLocalizedTitles())
                .setUri(termIRI.toString())
                .setIsDefiningOntology(loader.isLocalTerm(termIRI))
                .setIsObsolete(loader.isObsoleteTerm(termIRI))
                .setShortForm(loader.getShortForm(termIRI))
                .setOboId(loader.getOboId(termIRI))
                .setHasChildren(loader.getDirectChildTerms().containsKey(termIRI))
                .setSubsets(new ArrayList<>(loader.getSubsets(termIRI)));

        if (!loader.getTermLabels().containsKey(termIRI)) {
            LocalizedStrings languageToLabels = new LocalizedStrings();
            languageToLabels.addString("en", loader.getShortForm(termIRI));
            builder.setLabels(languageToLabels);
        }
        else  {
            builder.setLabels(loader.getTermLabels().get(termIRI));
        }


        // index all annotations
        if (!loader.getAnnotations(termIRI).isEmpty()) {

            // Map<language, Map<key, value>>
            Map<String, Map<String, List<String>>> relatedTerms = new HashMap<>();
            
            Map<IRI, LocalizedStrings> annotations = loader.getAnnotations(termIRI);

            for (IRI relation : annotations.keySet()) {

                LocalizedStrings annos = annotations.get(relation);

                for(String lang : annos.getLanguages()) {

                    Map<String,List<String>> related = relatedTerms.get(lang);

                    if (related == null) {
                        related = new HashMap<>();
                        relatedTerms.put(lang, related);
                    }

                    LocalizedStrings labels = loader.getTermLabels().get(relation);

                    String label = labels.getFirstString(lang);

                    if(label == null)
                        label = labels.getFirstString("en");

                    String labelName = label + "_annotation";

                    List<String> values = related.get(labelName);

                    if(values == null) {
                        values = new ArrayList<String>();
                        related.put(labelName, values);
                    }

                    values.addAll(loader.getAnnotations(termIRI).get(relation).getStrings(lang));
                }

            }

            builder.setAnnotations(relatedTerms);
        }

        if (loader.getTermSynonyms().containsKey(termIRI)) {
            LocalizedStrings languageToSynonyms = loader.getTermSynonyms().get(termIRI);

            //builder.setSynonyms(loader.getTermSynonyms().get(termIRI));
        }

        if (loader.getTermDefinitions().containsKey(termIRI)) {
            builder.setDescriptions(loader.getTermDefinitions().get(termIRI));
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

    private String generateOntologyId(String ontologyName, String iri) {
        return ontologyName.toLowerCase() +":" + iri;
    }
    private String generateId(String ontologyName, String type, String iri) {
        return ontologyName.toLowerCase() +  ":" + type  +":" + iri;
    }

    public int getBatchSize() {
        return batchSize;
    }
}
