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
import static uk.ac.ebi.spot.ols.neo4j.model.Neo4JNodePropertyNameConstants.*;
import uk.ac.ebi.spot.ols.neo4j.model.Neo4JNodePropertyNameConstants;

/**
 * 
 * @author Henriette Harmse
 * @date 2019-05-10
 *
 * Samples, Phenotypes and Ontologies Team 
 * EMBL-EBI
 */
class NodeCreator {
	
	protected static Logger logger = LoggerFactory.getLogger(NodeCreator.class);
	
	protected NodeCreator() {
	}

    static Long getOrCreateNode(BatchInserter inserter, Map<String, Long> nodeMap, 
    		OntologyLoader loader, IRI classIri, Collection<Label> nodeLabels) {
    	
        if (!nodeMap.containsKey(classIri.toString())) {
            Map<String, Object> nodeProperties = new HashMap<>();
            
            nodeProperties.put(OLS_ID, generateOlsId(loader.getOntologyName(), classIri));
            nodeProperties.put(Neo4JNodePropertyNameConstants.IRI, classIri.toString());
            
            if (!addPropertiesForTopLevelTerms(classIri, nodeProperties)) {
                addAppropriateLabelProperty(loader, classIri, nodeProperties);
            }

            nodeProperties.put(ONTOLOGY_NAME, loader.getOntologyName());
            nodeProperties.put(ONTOLOGY_PREFIX, loader.getPreferredPrefix());
            nodeProperties.put(ONTOLOGY_IRI, loader.getOntologyIRI().toString());
            nodeProperties.put(IS_OBSOLETE, loader.isObsoleteTerm(classIri));
            nodeProperties.put(IS_DEFINING_ONTOLOGY, loader.isLocalTerm(classIri));

            addAppropriateHasChildrenProperty(loader, classIri, nodeLabels, nodeProperties);

            addAppropriateIsRootProperty(loader, classIri, nodeProperties);          
            addPreferredRootPropertyConditionally(loader, classIri, nodeLabels, nodeProperties);
            
            addShortFormPropertyConditionally(loader, classIri, nodeProperties);
            addOboIdPropertyConditionally(loader, classIri, nodeProperties);
            addSynonymsPropertyConditionally(loader, classIri, nodeProperties);
            addSubsetsPropertyConditionally(loader, classIri, nodeProperties);
            addDescriptionPropertyConditionally(loader, classIri, nodeProperties);
            addSuperClassDescriptionPropertyConditionally(loader, classIri, nodeProperties);
            addEquivalentClassDescriptionPropertyConditionally(loader, classIri, nodeProperties);
            addAnnotationPropertiesConditionally(loader, classIri, nodeProperties);
            addTermReplacedByPropertyConditionally(loader, classIri, nodeProperties);
            addOboRelatedPropertiesConditionally(loader, classIri, nodeProperties);
            addObsoleteLabelConditionally(loader, classIri, nodeLabels);
            
        	Label labelArray[] = nodeLabels.toArray(new Label[nodeLabels.size()]);
        	logger.debug("classIri = " + classIri);
        	logger.debug("nodeLabels = " + nodeLabels);
        	logger.debug("inserter = " + inserter);

			long classNode = 0;
        	try {
				classNode = inserter.createNode(nodeProperties, labelArray);
			} catch (Throwable t) {
        		logger.error(t.getMessage(), t);
			}

            nodeMap.put(classIri.toString(), classNode);
        }
        return nodeMap.get(classIri.toString());
    }

	protected static void addPreferredRootPropertyConditionally(OntologyLoader loader, IRI classIri,
			Collection<Label> nodeLabels, Map<String, Object> nodeProperties) {
		if (loader.getPreferredRootTerms().contains(classIri)) {
			nodeProperties.put("is_preferred_root", loader.getPreferredRootTerms().contains(classIri));
			logger.debug("About to add preferredRootTermLabel");
			nodeLabels.add(preferredRootTermLabel);
		}
	}

	protected static Collection<Label> addObsoleteLabelConditionally(OntologyLoader loader, IRI classIri,
			Collection<Label> nodeLabels) {
		if (loader.isObsoleteTerm(classIri)) {
			logger.debug("About to add obsolete label");
			nodeLabels.add(obsoleteLabel);
		}
		return nodeLabels;
	}

	protected static void addOboRelatedPropertiesConditionally(OntologyLoader loader, IRI classIri,
			Map<String, Object> nodeProperties) {
		ObjectMapper mapper = new ObjectMapper();            
		addOboDefinitionCitationPropertyConditionally(mapper, loader, classIri, nodeProperties);
		addOboSynonymsPropertyConditionally(mapper, loader, classIri, nodeProperties);
		addOboXRefsConditionally(mapper, loader, classIri, nodeProperties);
	}

	protected static void addOboXRefsConditionally(ObjectMapper mapper, OntologyLoader loader, 
			IRI classIri, Map<String, Object> nodeProperties) {
		Collection<OBOXref> xrefs = loader.getOBOXrefs(classIri);
		if (!xrefs.isEmpty()) {
		    List<String> refs = new ArrayList<>();
		    for (OBOXref xref : xrefs) {
		        try {
		            refs.add(mapper.writeValueAsString(xref));
		        } catch (JsonProcessingException e) {
		        }
		    }
		    nodeProperties.put(OBO_XREF, refs.toArray(new String [refs.size()]));
		}
	}

	protected static void addOboSynonymsPropertyConditionally(ObjectMapper mapper, OntologyLoader loader,
			IRI classIri, Map<String, Object> nodeProperties) {
		
		Collection<OBOSynonym> oboSynonyms = loader.getOBOSynonyms(classIri);
		if (!oboSynonyms.isEmpty()) {
		    List<String> syns = new ArrayList<>();
		    for (OBOSynonym synonym : oboSynonyms) {
		        try {
		            syns.add(mapper.writeValueAsString(synonym));
		        } catch (JsonProcessingException e) {
		        }
		    }
		    nodeProperties.put(OBO_SYNONYM, syns.toArray(new String [syns.size()]));
		}
	}

	protected static void addOboDefinitionCitationPropertyConditionally(ObjectMapper mapper, 
			OntologyLoader loader, IRI classIri, Map<String, Object> nodeProperties) {
		
		Collection<OBODefinitionCitation> definitionCitations = loader.getOBODefinitionCitations(classIri);
		if (!definitionCitations.isEmpty()) {
		    List<String> mappedToStringDefinitionCitations = new ArrayList<>();
		    for (OBODefinitionCitation citation : definitionCitations) {
		        try {
		            mappedToStringDefinitionCitations.add(mapper.writeValueAsString(citation));
		        } catch (JsonProcessingException e) {
		        	logger.debug("Citation '" + citation + "' could not be mapped to String.");
		        }
		    }
		    nodeProperties.put(OBO_DEFINITION_CITATION,  mappedToStringDefinitionCitations.toArray(
		    		new String [mappedToStringDefinitionCitations.size()]));
		}
	}


	protected static void addTermReplacedByPropertyConditionally(OntologyLoader loader, IRI classIri,
			Map<String, Object> nodeProperties) {
		if (loader.getTermReplacedBy(classIri) != null) {
		    nodeProperties.put(TERM_REPLACED_BY, loader.getTermReplacedBy(classIri).toString());
		}
	}


	protected static void addAnnotationPropertiesConditionally(OntologyLoader loader, IRI classIri,
			Map<String, Object> nodeProperties) {
		Map<IRI, Collection<String>> annotations = loader.getAnnotations(classIri);
		if (!annotations.isEmpty()) {
		    for (IRI keys : annotations.keySet()) {
		        String annotationLabel = loader.getTermLabels().get(keys);
		        String [] value = annotations.get(keys).toArray(new String [annotations.get(keys).size()]);
		        nodeProperties.put(ANNOTATION_DESIGNATION + annotationLabel, value);
		    }
		}
	}

	protected static void addEquivalentClassDescriptionPropertyConditionally(OntologyLoader loader, 
			IRI classIri, Map<String, Object> nodeProperties) {
		if (loader.getLogicalEquivalentClassDescriptions().containsKey(classIri)) {
		    String [] descriptions = loader.getLogicalEquivalentClassDescriptions()
		    		.get(classIri).toArray(new String 
		    				[loader.getLogicalEquivalentClassDescriptions().get(classIri).size()]);
		    nodeProperties.put(EQUIVALENT_CLASS_DESCRIPTION, descriptions);
		}
	}


	protected static void addSuperClassDescriptionPropertyConditionally(OntologyLoader loader, IRI classIri,
			Map<String, Object> nodeProperties) {
		if (loader.getLogicalSuperClassDescriptions().containsKey(classIri)) {
		    String [] descriptions = loader.getLogicalSuperClassDescriptions().get(classIri)
		    		.toArray(new String [loader.getLogicalSuperClassDescriptions().get(classIri).size()]);
		    nodeProperties.put(SUPER_CLASS_DESCRIPTION, descriptions);
		}
	}

	protected static void addDescriptionPropertyConditionally(OntologyLoader loader, IRI classIri,
			Map<String, Object> nodeProperties) {
		if (loader.getTermDefinitions().containsKey(classIri)) {
		    String [] definition = loader.getTermDefinitions().get(classIri)
		    		.toArray(new String [loader.getTermDefinitions().get(classIri).size()]);
		    nodeProperties.put(DESCRIPTION, definition);
		}
	}

	protected static void addSubsetsPropertyConditionally(OntologyLoader loader, IRI classIri,
			Map<String, Object> nodeProperties) {
		if (!loader.getSubsets(classIri).isEmpty()) {
		    String [] subsets = loader.getSubsets(classIri).toArray(
		    		new String [loader.getSubsets(classIri).size()]);
		    nodeProperties.put(IN_SUBSET, subsets);
		}
	}

	protected static void addSynonymsPropertyConditionally(OntologyLoader loader, IRI classIri,
			Map<String, Object> nodeProperties) {
		if (loader.getTermSynonyms().containsKey(classIri)) {
		    String [] synonyms = loader.getTermSynonyms().get(classIri).
		    		toArray(new String [loader.getTermSynonyms().get(classIri).size()]);
		    nodeProperties.put(SYNONYM, synonyms);
		}
	}

	protected static void addOboIdPropertyConditionally(OntologyLoader loader, IRI classIri,
			Map<String, Object> nodeProperties) {
		if (loader.getOboId(classIri) != null) {
		    nodeProperties.put(OBO_ID, loader.getOboId(classIri));
		}
	}

	protected static void addShortFormPropertyConditionally(OntologyLoader loader, IRI classIri,
			Map<String, Object> nodeProperties) {
		if (loader.getShortForm(classIri) != null) {
		    nodeProperties.put(SHORT_FORM, loader.getShortForm(classIri));
		}
	}


	protected static void addAppropriateIsRootProperty(OntologyLoader loader, IRI classIri,
			Map<String, Object> nodeProperties) {
		
		nodeProperties.put(IS_ROOT, 
				loader.getDirectParentTerms(classIri).isEmpty() && 
					loader.getRelatedParentTerms(classIri).isEmpty());
	}


    /**
     * Sets {@link Neo4JNodePropertyNameConstants#HAS_CHILDREN} property to true for terms that has 
     * direct children or terms that are considered to be child-like relations (e.g. part-of, 
     * develops-from etc.). For terms that are labelled as instances 
     * {@link Neo4JNodePropertyNameConstants#HAS_CHILDREN} is set to false.
     * 
     * @param loader
     * @param classIri
     * @param nodeLabels
     * @param nodeProperties
     */
	protected static void addAppropriateHasChildrenProperty(OntologyLoader loader, IRI classIri,
			Collection<Label> nodeLabels, Map<String, Object> nodeProperties) {
		if (nodeLabels.contains(instanceLabel)) {
		    nodeProperties.put(HAS_CHILDREN, false );
		}
		else  {
		    nodeProperties.put(HAS_CHILDREN, (!loader.getDirectChildTerms(classIri).isEmpty() || 
		    		!loader.getRelatedChildTerms(classIri).isEmpty()) );
		}
	}

    /**
     * Adds a {@link Neo4JNodePropertyNameConstants#LABEL} property for the node. Ontology designers 
     * can define custom terms that they use for labelling their terms. This method only uses the 
     * short form of classIri if no custom label terms have been defined for this classIri.
     * 
     * @param loader
     * @param classIri
     * @param nodeProperties
     */
	protected static void addAppropriateLabelProperty(OntologyLoader loader, IRI classIri,
			Map<String, Object> nodeProperties) {
		if (!loader.getTermLabels().containsKey(classIri)) {
		    nodeProperties.put(Neo4JNodePropertyNameConstants.LABEL, 
		    		loader.getShortForm(classIri));
		} else  {
		    nodeProperties.put(Neo4JNodePropertyNameConstants.LABEL, 
		    		loader.getTermLabels().get(classIri));
		}
	}

	protected static String generateOlsId(String ontologyName, IRI classIri) {
		return ontologyName.toLowerCase() + ":" + classIri.toString();
	}
	
	/**
	 * Deals with the top level OWL class or OWL object property. Note that OWL data properties are 
	 * not considered.
	 * 
	 * @param classIri
	 * @param nodeProperties
	 * @return true if classIri represents a top-level OWL class or OWL object property, else false.
	 *  
	 */
	protected static boolean addPropertiesForTopLevelTerms(IRI classIri, 
			Map<String, Object> nodeProperties) {
		
		boolean isTopLevelTerm = false;
		
        if (classIri.toString().equals(THING)) {
        	isTopLevelTerm = true;
            nodeProperties.put(Neo4JNodePropertyNameConstants.LABEL, SHORT_THING);
            nodeProperties.put(HAS_CHILDREN, isTopLevelTerm);
            
        }
        else if (classIri.toString().equals(TOP_OBJECT_PROPERTY)) {
        	isTopLevelTerm = true;
            nodeProperties.put(Neo4JNodePropertyNameConstants.LABEL, SHORT_TOP_OBJECT_PROPERTY);
            nodeProperties.put(HAS_CHILDREN, isTopLevelTerm);
        }		
        return isTopLevelTerm;
	}

//    static Long getOrCreateNodeDeprecated(BatchInserter inserter, Map<String, Long> nodeMap, 
//    		OntologyLoader loader, IRI classIri, Collection<Label> nodeLabels) {
//    	
//        if (!nodeMap.containsKey(classIri.toString())) {
//            Map<String, Object> properties = new HashMap<>();
//            properties.put("olsId", loader.getOntologyName().toLowerCase() + ":" + classIri.toString());
//            properties.put("iri", classIri.toString());
//            if (classIri.toString().equals("http://www.w3.org/2002/07/owl#Thing")) {
//                properties.put("label", "Thing");
//                properties.put("has_children", true );
//
//            }
//            else if (classIri.toString().equals("http://www.w3.org/2002/07/owl#TopObjectProperty")) {
//                properties.put("label", "TopObjectProperty");
//                properties.put("has_children", true );
//            }
//            else {
//                if (!loader.getTermLabels().containsKey(classIri)) {
//                    properties.put("label", loader.getShortForm(classIri));
//                } else  {
//                    properties.put("label", loader.getTermLabels().get(classIri));
//                }
//            }
//
//            properties.put("ontology_name", loader.getOntologyName());
//            properties.put("ontology_prefix", loader.getPreferredPrefix());
//            properties.put("ontology_iri", loader.getOntologyIRI().toString());
//            properties.put("is_obsolete", loader.isObsoleteTerm(classIri));
//            properties.put("is_defining_ontology", loader.isLocalTerm(classIri));
//
//            // if it's an individual, it can't have children, but it may be punned URI to a class 
//            // to we need to check
//            if (nodeLabels.contains(instanceLabel)) {
//                properties.put("has_children", false );
//            }
//            else  {
//                properties.put("has_children", (!loader.getDirectChildTerms(classIri).isEmpty() || 
//                		!loader.getRelatedChildTerms(classIri).isEmpty()) );
//            }
//            properties.put("is_root", 
//            		loader.getDirectParentTerms(classIri).isEmpty() && 
//            			loader.getRelatedParentTerms(classIri).isEmpty());          
//            if (loader.getPreferredRootTerms().contains(classIri)) {
//            	properties.put("is_preferred_root", loader.getPreferredRootTerms().contains(classIri));
//            	logger.debug("About to add preferredRootTermLabel");
//            	nodeLabels.add(preferredRootTermLabel);
//            }
//            
//            // add shortforms
//            if (loader.getShortForm(classIri) != null) {
//                properties.put("short_form", loader.getShortForm(classIri));
//            }
//
//            // add oboid
//            if (loader.getOboId(classIri) != null) {
//                properties.put("obo_id", loader.getOboId(classIri));
//            }
//
//            // add synonyms
//            if (loader.getTermSynonyms().containsKey(classIri)) {
//                String [] synonyms = loader.getTermSynonyms().get(classIri).
//                		toArray(new String [loader.getTermSynonyms().get(classIri).size()]);
//                properties.put("synonym", synonyms);
//            }
//
//            // add subsets
//            if (!loader.getSubsets(classIri).isEmpty()) {
//                String [] subsets = loader.getSubsets(classIri).toArray(
//                		new String [loader.getSubsets(classIri).size()]);
//                properties.put("in_subset", subsets);
//            }
//
//            // add definitions
//            if (loader.getTermDefinitions().containsKey(classIri)) {
//                String [] definition = loader.getTermDefinitions().get(classIri)
//                		.toArray(new String [loader.getTermDefinitions().get(classIri).size()]);
//                properties.put("description", definition);
//            }
//
//            if (loader.getLogicalSuperClassDescriptions().containsKey(classIri)) {
//                String [] descriptions = loader.getLogicalSuperClassDescriptions().get(classIri)
//                		.toArray(new String [loader.getLogicalSuperClassDescriptions().get(classIri).size()]);
//                properties.put("superClassDescription", descriptions);
//            }
//
//            if (loader.getLogicalEquivalentClassDescriptions().containsKey(classIri)) {
//                String [] descriptions = loader.getLogicalEquivalentClassDescriptions()
//                		.get(classIri).toArray(new String 
//                				[loader.getLogicalEquivalentClassDescriptions().get(classIri).size()]);
//                properties.put("equivalentClassDescription", descriptions);
//            }
//
//            // add annotations
//            Map<IRI, Collection<String>> annotations = loader.getAnnotations(classIri);
//            if (!annotations.isEmpty()) {
//                for (IRI keys : annotations.keySet()) {
//                    String annotationLabel = loader.getTermLabels().get(keys);
//                    String [] value = annotations.get(keys).toArray(new String [annotations.get(keys).size()]);
//                    properties.put("annotation-" + annotationLabel, value);
//                }
//            }
//
//            if (loader.getTermReplacedBy(classIri) != null) {
//                properties.put("term_replaced_by", loader.getTermReplacedBy(classIri).toString());
//            }
//
//            ObjectMapper mapper = new ObjectMapper();
//            Collection<OBODefinitionCitation> definitionCitations = loader.getOBODefinitionCitations(classIri);
//            if (!definitionCitations.isEmpty()) {
//                List<String> defs = new ArrayList<>();
//                for (OBODefinitionCitation citation : definitionCitations) {
//                    try {
//                        defs.add(mapper.writeValueAsString(citation));
//                    } catch (JsonProcessingException e) {
//
//                    }
//                }
//                properties.put("obo_definition_citation",  defs.toArray(new String [defs.size()]));
//            }
//
//            Collection<OBOSynonym> oboSynonyms = loader.getOBOSynonyms(classIri);
//            if (!oboSynonyms.isEmpty()) {
//                List<String> syns = new ArrayList<>();
//                for (OBOSynonym synonym : oboSynonyms) {
//                    try {
//                        syns.add(mapper.writeValueAsString(synonym));
//                    } catch (JsonProcessingException e) {
//                    }
//                }
//                properties.put("obo_synonym", syns.toArray(new String [syns.size()]));
//            }
//
//            Collection<OBOXref> xrefs = loader.getOBOXrefs(classIri);
//            if (!xrefs.isEmpty()) {
//                List<String> refs = new ArrayList<>();
//                for (OBOXref xref : xrefs) {
//                    try {
//                        refs.add(mapper.writeValueAsString(xref));
//                    } catch (JsonProcessingException e) {
//                    }
//                }
//                properties.put("obo_xref", refs.toArray(new String [refs.size()]));
//            }
//
//
//            logger.debug("ClassIRI = " + classIri);
//            logger.debug("nodeLabels = " + nodeLabels);
//            if (loader.isObsoleteTerm(classIri)) {
//            	logger.debug("About to add obsolete label");
//            	nodeLabels.add(obsoleteLabel);
//            }
//            
//        	Label labelArray[] = nodeLabels.toArray(new Label[nodeLabels.size()]);
//        	long classNode = inserter.createNode(properties, labelArray);           
//
//            nodeMap.put(classIri.toString(), classNode);
//        }
//        return nodeMap.get(classIri.toString());
//    }
    
}
