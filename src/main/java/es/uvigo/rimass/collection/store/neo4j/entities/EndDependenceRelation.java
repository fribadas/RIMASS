package es.uvigo.rimass.collection.store.neo4j.entities;

import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;

@RelationshipEntity(type="ENDS_WITH")
public class EndDependenceRelation {

	@StartNode
	DependenceNode dependence;
	
	@EndNode
	RelationNode relation;

	public DependenceNode getDependence() {
		return dependence;
	}

	public void setDependence(DependenceNode dependence) {
		this.dependence = dependence;
	}

	public RelationNode getRelation() {
		return relation;
	}

	public void setRelation(RelationNode relation) {
		this.relation = relation;
	}
}
