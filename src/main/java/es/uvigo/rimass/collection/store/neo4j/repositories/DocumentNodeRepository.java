package es.uvigo.rimass.collection.store.neo4j.repositories;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.stereotype.Repository;

import es.uvigo.rimass.collection.store.neo4j.entities.DocumentNode;

@Repository
public interface DocumentNodeRepository extends GraphRepository<DocumentNode>{

	@Query("START rel=node({0}) " +
			"MATCH rel<-[c:CONTAINS]-doc " +
			"WITH doc, sum(c.occurences) as occ " +
			"RETURN doc ORDER BY occ ")
	Iterable<DocumentNode> findDocsByRelIds(List<Long> ids);
	
}
