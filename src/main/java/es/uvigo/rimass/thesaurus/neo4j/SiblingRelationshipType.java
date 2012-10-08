package es.uvigo.rimass.thesaurus.neo4j;


public class SiblingRelationshipType implements org.neo4j.graphdb.RelationshipType{

	public String name() {
		return "SIBLING_OF";
	}
}
