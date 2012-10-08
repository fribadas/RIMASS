package es.uvigo.rimass.collection.store.neo4j.repositories;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.stereotype.Repository;

import es.uvigo.rimass.collection.store.neo4j.entities.RelationNode;

@Repository
public interface RelationNodeRepository extends GraphRepository<RelationNode>{
	
	@Query("START relation=node:relationIndex(label = {0}) " +
			"RETURN relation " +
			"LIMIT 1")
	Iterable<RelationNode> findByLabel(String label);
}
