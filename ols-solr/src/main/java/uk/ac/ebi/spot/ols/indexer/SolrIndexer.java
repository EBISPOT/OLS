package uk.ac.ebi.spot.ols.indexer;

import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import uk.ac.ebi.spot.ols.loader.OntologyLoader;
import uk.ac.ebi.spot.ols.model.TermDocument;
import uk.ac.ebi.spot.ols.model.TermDocumentBuilder;
import uk.ac.ebi.spot.ols.util.TermType;
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
    private int batchSize = 1000;

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

            getLog().info("Creating new index for " + loader.getOntologyName());
            long startTime = System.currentTimeMillis();

            getLog().info("Number of classes to index: " + loader.getAllClasses().size());
            getLog().info("Number of object properties to index: " + loader.getAllObjectPropertyIRIs().size());
            getLog().info("Number of annotation properties to index: " + loader.getAllAnnotationPropertyIRIs().size());
            getLog().info("Number of individuals to index: " + loader.getAllIndividualIRIs().size());

            List<TermDocument> documents = new ArrayList<TermDocument>();

            for (IRI classTerm : loader.getAllClasses()) {

                TermDocumentBuilder builder = extractFeatures(loader, classTerm);
                builder.setType(TermType.CLASS.toString().toLowerCase());
                documents.add(builder.createTermDocument());

                if (documents.size() == 10000) {
                    getLog().info("Max reached - indexing terms");
                    index(documents);
                    documents = new ArrayList<>();
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
            }


            long endTime = System.currentTimeMillis();
            long duration = (endTime - startTime) / 1000; // time in seconds
            getLog().info("Reading " + loader.getOntologyName() + " completed in " + duration + " seconds");

            index(documents);
            getLog().info("Saving indexing " +loader.getOntologyName()+ " completed");

        }



    }

    private void index (List<TermDocument> documents) {
        long startTime = System.currentTimeMillis();

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
            getLog().info("Indexed {} / {} entries", count, numDocuments);
        }
        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime) / 1000; // time in seconds
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


//        try {
//
//
//            //Set json bbop sibling graph string directly (the json graph is not saved to a file it is in memory
//            // in the ByteArrayOutputStream object).
//            ByteArrayOutputStream outStream=new ByteArrayOutputStream();
//            SiblingGraphCreator siblingGraphCreator = new SiblingGraphCreator();
//            //Get the jsonGenerator object for this termIRI
//            siblingGraphCreator.buildBpopGraph(loader, termIRI, outStream);
//            //Set the builder bbobSiblingGraph document.
//            builder.setBbopSibblingGraph(outStream.toString().intern());
//
////            //Set path to file containing json bbop sibling graph.
////            String termId = termIRI.toString().substring(termIRI.toString().lastIndexOf('/') + 1, termIRI.toString().length());
////            String filePath = "/Users/catherineleroy/Documents/json-graphs/" + termId + ".json";
////            File file = new File(filePath);
////            OutputStream outputStream = new FileOutputStream(file);
////            SiblingGraphCreator siblingGraphCreator = new SiblingGraphCreator();
////            siblingGraphCreator.buildBpopGraph(loader, termIRI, outputStream);
////            builder.setBbopSibblingGraph(filePath);
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


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

    public int getBatchSize() {
        return batchSize;
    }
}
