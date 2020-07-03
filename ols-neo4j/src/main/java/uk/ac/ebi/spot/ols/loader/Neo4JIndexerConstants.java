package uk.ac.ebi.spot.ols.loader;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
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
	
    static final Label obsoleteLabel = Label.label("Obsolete");
    
    // this represents a unique term
    static final Label mergedClassLabel = Label.label("Resource");
    
    // define a node labels for ontology terms, properties and individuals
    static final Label nodeLabel = Label.label("Class");
    static final Label relationLabel = Label.label("Property");
    static final Label instanceLabel = Label.label("Individual");
    static final Label rootLabel = Label.label("Root");
    static final Label _nodeLabel = Label.label("_Class");
    static final Label _relationLabel = Label.label("_Property");
    static final Label _instanceLabel = Label.label("_Individual");
    static final Label preferredRootTermLabel = Label.label("PreferredRootTerm");

    // create relationship types
    static final RelationshipType refersTo = RelationshipType.withName("REFERSTO");
    static final RelationshipType isa = RelationshipType.withName("SUBCLASSOF");
    static final RelationshipType subpropertyof = RelationshipType.withName("SUBPROPERTYOF");
    static final RelationshipType typeOf = RelationshipType.withName("INSTANCEOF");
    static final RelationshipType related = RelationshipType.withName("Related");
    static final RelationshipType relatedIndividual = RelationshipType.withName("RelatedIndividual");
    static final RelationshipType treeRelation = RelationshipType.withName("RelatedTree");

	private Neo4JIndexerConstants() {
	}

}
