package es.uvigo.rimass.collection.store.neo4j.entities;

import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;

@RelationshipEntity(type="SIBLING_OF")
public class RelatedTermRelation {

	@StartNode
	TermNode term;
	
	@EndNode
	TermNode related;
	
	public TermNode getTerm() {
		return term;
	}
	public void setTerm(TermNode term) {
		this.term = term;
	}
	public TermNode getRelated() {
		return related;
	}
	public void setRelated(TermNode related) {
		this.related = related;
	}
}
