package uk.ac.ebi.spot.ols.loader;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;

/**
 * 
 * @author Henriette Harmse
 * @date 2019-05-10
 *
 * Samples, Phenotypes and Ontologies Team 
 * EMBL-EBI
 */
class Neo4JIndexerConstants {
	
    static final Label obsoleteLabel = DynamicLabel.label("Obsolete");
    
    // this represents a unique term
    static final Label mergedClassLabel = DynamicLabel.label("Resource");
    
    // define a node labels for ontology terms, properties and individuals
    static final Label nodeLabel = DynamicLabel.label("Class");
    static final Label relationLabel = DynamicLabel.label("Property");
    static final Label instanceLabel = DynamicLabel.label("Individual");
    static final Label rootLabel = DynamicLabel.label("Root");
    static final Label _nodeLabel = DynamicLabel.label("_Class");
    static final Label _relationLabel = DynamicLabel.label("_Property");
    static final Label _instanceLabel = DynamicLabel.label("_Individual");
    static final Label preferredRootTermLabel = DynamicLabel.label("PreferredRootTerm");

    // create relationship types
    static final RelationshipType refersTo = DynamicRelationshipType.withName("REFERSTO");
    static final RelationshipType isa = DynamicRelationshipType.withName("SUBCLASSOF");
    static final RelationshipType subpropertyof = DynamicRelationshipType.withName("SUBPROPERTYOF");
    static final RelationshipType typeOf = DynamicRelationshipType.withName("INSTANCEOF");
    static final RelationshipType related = DynamicRelationshipType.withName("Related");
    static final RelationshipType relatedIndividual = DynamicRelationshipType.withName("RelatedIndividual");
    static final RelationshipType treeRelation = DynamicRelationshipType.withName("RelatedTree");

	private Neo4JIndexerConstants() {
	}

}
