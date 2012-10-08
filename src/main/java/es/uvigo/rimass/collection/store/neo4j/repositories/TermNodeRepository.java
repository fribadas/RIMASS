package es.uvigo.rimass.collection.store.neo4j.repositories;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.stereotype.Repository;

import es.uvigo.rimass.collection.store.neo4j.entities.TermNode;

@Repository
public interface TermNodeRepository extends GraphRepository<TermNode>{
	
	@Query("START term=node:termIndex(label = {0})" +
			"RETURN term " +
			"LIMIT 1")
	Iterable<TermNode> findByLabel(String label);
	
	@Query("START term=node:termIndex(label = {0})" +
			"MATCH term<-[:PARENT_OF]-term2 " +
			"RETURN term2 ")
	Iterable<TermNode> findAscendantNodes(String label);

	@Query("START term=node:termIndex(label = {0})" +
			"MATCH term-[:PARENT_OF]->term2 " +
			"RETURN term2 ")
	Iterable<TermNode> findDescendantNodes(String term);
}
