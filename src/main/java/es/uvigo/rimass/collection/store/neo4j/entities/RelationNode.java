package es.uvigo.rimass.collection.store.neo4j.entities;

import java.util.List;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.data.neo4j.support.index.IndexType;

@NodeEntity
public class RelationNode {
	@GraphId 
	Long id;
	
	@Indexed(indexName="relationIndex", unique=true, indexType=IndexType.SIMPLE)
	String label;
	
	@RelatedTo(type="WITH_RELATION", direction=Direction.INCOMING)
	List<DependenceNode> dependence;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public List<DependenceNode> getDependence() {
		return dependence;
	}

	public void setDependence(List<DependenceNode> dependence) {
		this.dependence = dependence;
	}
}
