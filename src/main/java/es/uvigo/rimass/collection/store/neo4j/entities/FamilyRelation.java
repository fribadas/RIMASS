package es.uvigo.rimass.collection.store.neo4j.entities;

import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;

@RelationshipEntity(type="PARENT_OF")
public class FamilyRelation {

	@StartNode
	TermNode term;
	
	@EndNode
	TermNode descendant;
	public TermNode getTerm() {
		return term;
	}
	public void setTerm(TermNode term) {
		this.term = term;
	}
	public TermNode getDescendant() {
		return descendant;
	}
	public void setDescendant(TermNode descendant) {
		this.descendant = descendant;
	}
}
