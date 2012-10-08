package es.uvigo.rimass.thesaurus.neo4j;

import org.neo4j.graphdb.RelationshipType;

public class ParentRelationshipType implements RelationshipType{

	public String name() {
		return "PARENT_OF";
	}

}
