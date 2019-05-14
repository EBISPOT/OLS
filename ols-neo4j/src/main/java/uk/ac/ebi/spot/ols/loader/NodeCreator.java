package uk.ac.ebi.spot.ols.loader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Label;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.ebi.spot.ols.util.OBODefinitionCitation;
import uk.ac.ebi.spot.ols.util.OBOSynonym;
import uk.ac.ebi.spot.ols.util.OBOXref;

import static uk.ac.ebi.spot.ols.loader.Neo4JIndexerConstants.*;
import static uk.ac.ebi.spot.ols.config.OntologyDefaults.*;

/**
 * 
 * @author Henriette Harmse
 * @date 2019-05-10
 *
 * Samples, Phenotypes and Ontologies Team 
 * EMBL-EBI
 */
class NodeCreator {
	
	private static Logger logger = LoggerFactory.getLogger(NodeCreator.class);
	
	private NodeCreator() {
	}

	
    static Long getOrCreateNode(BatchInserter inserter, Map<String, Long> nodeMap, 
    		OntologyLoader loader, IRI classIri, Collection<Label> nodeLabels) {
    	
        if (!nodeMap.containsKey(classIri.toString())) {
            Map<String, Object> properties = new HashMap<>();
            properties.put("olsId", loader.getOntologyName().toLowerCase() + ":" + classIri.toString());
            properties.put("iri", classIri.toString());
            if (classIri.toString().equals("http://www.w3.org/2002/07/owl#Thing")) {
                properties.put("label", "Thing");
                properties.put("has_children", true );

            }
            else if (classIri.toString().equals("http://www.w3.org/2002/07/owl#TopObjectProperty")) {
                properties.put("label", "TopObjectProperty");
                properties.put("has_children", true );
            }
            else {
                if (!loader.getTermLabels().containsKey(classIri)) {
                    properties.put("label", loader.getShortForm(classIri));
                } else  {
                    properties.put("label", loader.getTermLabels().get(classIri));
                }
            }

            properties.put("ontology_name", loader.getOntologyName());
            properties.put("ontology_prefix", loader.getPreferredPrefix());
            properties.put("ontology_iri", loader.getOntologyIRI().toString());
            properties.put("is_obsolete", loader.isObsoleteTerm(classIri));
            properties.put("is_defining_ontology", loader.isLocalTerm(classIri));

            // if it's an individual, it can't have children, but it may be punned URI to a class 
            // to we need to check
            if (nodeLabels.contains(instanceLabel)) {
                properties.put("has_children", false );
            }
            else  {
                properties.put("has_children", (!loader.getDirectChildTerms(classIri).isEmpty() || 
                		!loader.getRelatedChildTerms(classIri).isEmpty()) );
            }
            properties.put("is_root", 
            		loader.getDirectParentTerms(classIri).isEmpty() && 
            			loader.getRelatedParentTerms(classIri).isEmpty());          
            if (loader.getPreferredRootTerms().contains(classIri)) {
            	properties.put("is_preferred_root", loader.getPreferredRootTerms().contains(classIri));
            	logger.debug("About to add preferredRootTermLabel");
            	nodeLabels.add(preferredRootTermLabel);
            }
            
            // add shortforms
            if (loader.getShortForm(classIri) != null) {
                properties.put("short_form", loader.getShortForm(classIri));
            }

            // add oboid
            if (loader.getOboId(classIri) != null) {
                properties.put("obo_id", loader.getOboId(classIri));
            }

            // add synonyms
            if (loader.getTermSynonyms().containsKey(classIri)) {
                String [] synonyms = loader.getTermSynonyms().get(classIri).
                		toArray(new String [loader.getTermSynonyms().get(classIri).size()]);
                properties.put("synonym", synonyms);
            }

            // add subsets
            if (!loader.getSubsets(classIri).isEmpty()) {
                String [] subsets = loader.getSubsets(classIri).toArray(
                		new String [loader.getSubsets(classIri).size()]);
                properties.put("in_subset", subsets);
            }

            // add definitions
            if (loader.getTermDefinitions().containsKey(classIri)) {
                String [] definition = loader.getTermDefinitions().get(classIri)
                		.toArray(new String [loader.getTermDefinitions().get(classIri).size()]);
                properties.put("description", definition);
            }

            if (loader.getLogicalSuperClassDescriptions().containsKey(classIri)) {
                String [] descriptions = loader.getLogicalSuperClassDescriptions().get(classIri)
                		.toArray(new String [loader.getLogicalSuperClassDescriptions().get(classIri).size()]);
                properties.put("superClassDescription", descriptions);
            }

            if (loader.getLogicalEquivalentClassDescriptions().containsKey(classIri)) {
                String [] descriptions = loader.getLogicalEquivalentClassDescriptions()
                		.get(classIri).toArray(new String 
                				[loader.getLogicalEquivalentClassDescriptions().get(classIri).size()]);
                properties.put("equivalentClassDescription", descriptions);
            }

            // add annotations
            Map<IRI, Collection<String>> annotations = loader.getAnnotations(classIri);
            if (!annotations.isEmpty()) {
                for (IRI keys : annotations.keySet()) {
                    String annotationLabel = loader.getTermLabels().get(keys);
                    String [] value = annotations.get(keys).toArray(new String [annotations.get(keys).size()]);
                    properties.put("annotation-" + annotationLabel, value);
                }
            }

            if (loader.getTermReplacedBy(classIri) != null) {
                properties.put("term_replaced_by", loader.getTermReplacedBy(classIri).toString());
            }

            ObjectMapper mapper = new ObjectMapper();
            Collection<OBODefinitionCitation> definitionCitations = loader.getOBODefinitionCitations(classIri);
            if (!definitionCitations.isEmpty()) {
                List<String> defs = new ArrayList<>();
                for (OBODefinitionCitation citation : definitionCitations) {
                    try {
                        defs.add(mapper.writeValueAsString(citation));
                    } catch (JsonProcessingException e) {

                    }
                }
                properties.put("obo_definition_citation",  defs.toArray(new String [defs.size()]));
            }

            Collection<OBOSynonym> oboSynonyms = loader.getOBOSynonyms(classIri);
            if (!oboSynonyms.isEmpty()) {
                List<String> syns = new ArrayList<>();
                for (OBOSynonym synonym : oboSynonyms) {
                    try {
                        syns.add(mapper.writeValueAsString(synonym));
                    } catch (JsonProcessingException e) {
                    }
                }
                properties.put("obo_synonym", syns.toArray(new String [syns.size()]));
            }

            Collection<OBOXref> xrefs = loader.getOBOXrefs(classIri);
            if (!xrefs.isEmpty()) {
                List<String> refs = new ArrayList<>();
                for (OBOXref xref : xrefs) {
                    try {
                        refs.add(mapper.writeValueAsString(xref));
                    } catch (JsonProcessingException e) {
                    }
                }
                properties.put("obo_xref", refs.toArray(new String [refs.size()]));
            }


            logger.debug("ClassIRI = " + classIri);
            logger.debug("nodeLabels = " + nodeLabels);
            if (loader.isObsoleteTerm(classIri)) {
            	logger.debug("About to add obsolete label");
            	nodeLabels.add(obsoleteLabel);
            }
            
        	Label labelArray[] = nodeLabels.toArray(new Label[nodeLabels.size()]);
        	long classNode = inserter.createNode(properties, labelArray);           

            nodeMap.put(classIri.toString(), classNode);
        }
        return nodeMap.get(classIri.toString());
    }

    static Long getOrCreateNodeDeprecated(BatchInserter inserter, Map<String, Long> nodeMap, 
    		OntologyLoader loader, IRI classIri, Collection<Label> nodeLabels) {
    	
        if (!nodeMap.containsKey(classIri.toString())) {
            Map<String, Object> properties = new HashMap<>();
            properties.put("olsId", loader.getOntologyName().toLowerCase() + ":" + classIri.toString());
            properties.put("iri", classIri.toString());
            if (classIri.toString().equals("http://www.w3.org/2002/07/owl#Thing")) {
                properties.put("label", "Thing");
                properties.put("has_children", true );

            }
            else if (classIri.toString().equals("http://www.w3.org/2002/07/owl#TopObjectProperty")) {
                properties.put("label", "TopObjectProperty");
                properties.put("has_children", true );
            }
            else {
                if (!loader.getTermLabels().containsKey(classIri)) {
                    properties.put("label", loader.getShortForm(classIri));
                } else  {
                    properties.put("label", loader.getTermLabels().get(classIri));
                }
            }

            properties.put("ontology_name", loader.getOntologyName());
            properties.put("ontology_prefix", loader.getPreferredPrefix());
            properties.put("ontology_iri", loader.getOntologyIRI().toString());
            properties.put("is_obsolete", loader.isObsoleteTerm(classIri));
            properties.put("is_defining_ontology", loader.isLocalTerm(classIri));

            // if it's an individual, it can't have children, but it may be punned URI to a class 
            // to we need to check
            if (nodeLabels.contains(instanceLabel)) {
                properties.put("has_children", false );
            }
            else  {
                properties.put("has_children", (!loader.getDirectChildTerms(classIri).isEmpty() || 
                		!loader.getRelatedChildTerms(classIri).isEmpty()) );
            }
            properties.put("is_root", 
            		loader.getDirectParentTerms(classIri).isEmpty() && 
            			loader.getRelatedParentTerms(classIri).isEmpty());          
            if (loader.getPreferredRootTerms().contains(classIri)) {
            	properties.put("is_preferred_root", loader.getPreferredRootTerms().contains(classIri));
            	logger.debug("About to add preferredRootTermLabel");
            	nodeLabels.add(preferredRootTermLabel);
            }
            
            // add shortforms
            if (loader.getShortForm(classIri) != null) {
                properties.put("short_form", loader.getShortForm(classIri));
            }

            // add oboid
            if (loader.getOboId(classIri) != null) {
                properties.put("obo_id", loader.getOboId(classIri));
            }

            // add synonyms
            if (loader.getTermSynonyms().containsKey(classIri)) {
                String [] synonyms = loader.getTermSynonyms().get(classIri).
                		toArray(new String [loader.getTermSynonyms().get(classIri).size()]);
                properties.put("synonym", synonyms);
            }

            // add subsets
            if (!loader.getSubsets(classIri).isEmpty()) {
                String [] subsets = loader.getSubsets(classIri).toArray(
                		new String [loader.getSubsets(classIri).size()]);
                properties.put("in_subset", subsets);
            }

            // add definitions
            if (loader.getTermDefinitions().containsKey(classIri)) {
                String [] definition = loader.getTermDefinitions().get(classIri)
                		.toArray(new String [loader.getTermDefinitions().get(classIri).size()]);
                properties.put("description", definition);
            }

            if (loader.getLogicalSuperClassDescriptions().containsKey(classIri)) {
                String [] descriptions = loader.getLogicalSuperClassDescriptions().get(classIri)
                		.toArray(new String [loader.getLogicalSuperClassDescriptions().get(classIri).size()]);
                properties.put("superClassDescription", descriptions);
            }

            if (loader.getLogicalEquivalentClassDescriptions().containsKey(classIri)) {
                String [] descriptions = loader.getLogicalEquivalentClassDescriptions()
                		.get(classIri).toArray(new String 
                				[loader.getLogicalEquivalentClassDescriptions().get(classIri).size()]);
                properties.put("equivalentClassDescription", descriptions);
            }

            // add annotations
            Map<IRI, Collection<String>> annotations = loader.getAnnotations(classIri);
            if (!annotations.isEmpty()) {
                for (IRI keys : annotations.keySet()) {
                    String annotationLabel = loader.getTermLabels().get(keys);
                    String [] value = annotations.get(keys).toArray(new String [annotations.get(keys).size()]);
                    properties.put("annotation-" + annotationLabel, value);
                }
            }

            if (loader.getTermReplacedBy(classIri) != null) {
                properties.put("term_replaced_by", loader.getTermReplacedBy(classIri).toString());
            }

            ObjectMapper mapper = new ObjectMapper();
            Collection<OBODefinitionCitation> definitionCitations = loader.getOBODefinitionCitations(classIri);
            if (!definitionCitations.isEmpty()) {
                List<String> defs = new ArrayList<>();
                for (OBODefinitionCitation citation : definitionCitations) {
                    try {
                        defs.add(mapper.writeValueAsString(citation));
                    } catch (JsonProcessingException e) {

                    }
                }
                properties.put("obo_definition_citation",  defs.toArray(new String [defs.size()]));
            }

            Collection<OBOSynonym> oboSynonyms = loader.getOBOSynonyms(classIri);
            if (!oboSynonyms.isEmpty()) {
                List<String> syns = new ArrayList<>();
                for (OBOSynonym synonym : oboSynonyms) {
                    try {
                        syns.add(mapper.writeValueAsString(synonym));
                    } catch (JsonProcessingException e) {
                    }
                }
                properties.put("obo_synonym", syns.toArray(new String [syns.size()]));
            }

            Collection<OBOXref> xrefs = loader.getOBOXrefs(classIri);
            if (!xrefs.isEmpty()) {
                List<String> refs = new ArrayList<>();
                for (OBOXref xref : xrefs) {
                    try {
                        refs.add(mapper.writeValueAsString(xref));
                    } catch (JsonProcessingException e) {
                    }
                }
                properties.put("obo_xref", refs.toArray(new String [refs.size()]));
            }


            logger.debug("ClassIRI = " + classIri);
            logger.debug("nodeLabels = " + nodeLabels);
            if (loader.isObsoleteTerm(classIri)) {
            	logger.debug("About to add obsolete label");
            	nodeLabels.add(obsoleteLabel);
            }
            
        	Label labelArray[] = nodeLabels.toArray(new Label[nodeLabels.size()]);
        	long classNode = inserter.createNode(properties, labelArray);           

            nodeMap.put(classIri.toString(), classNode);
        }
        return nodeMap.get(classIri.toString());
    }
    
}
