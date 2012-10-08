package es.uvigo.rimass.collection.store.neo4j.entities;

import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;

@RelationshipEntity(type="STARTS_WITH")
public class StartDependenceRelation {

	@StartNode
	DependenceNode dependence;
	
	@EndNode
	TermNode term;

	public DependenceNode getDependence() {
		return dependence;
	}

	public void setDependence(DependenceNode dependence) {
		this.dependence = dependence;
	}

	public TermNode getTerm() {
		return term;
	}

	public void setTerm(TermNode term) {
		this.term = term;
	}
}
